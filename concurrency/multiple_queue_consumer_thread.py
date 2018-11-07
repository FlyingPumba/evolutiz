import time
import traceback
from queue import Empty
from threading import Event

from dependency_injection.required_feature import RequiredFeature
from devices.device import Device
from util import logger
from concurrency.killable_thread import KillableThread
from concurrency.thread_hung_exception import ThreadHungException


class MultipleQueueConsumerThread(KillableThread):
    """Provides a thread where a function can be called.
    The arguments of this function are fetched from different queues.
    These queues are of two types:
    - Recyclable items queues: each item fetched from these queues will be put again in their respective queue after a
        call of the function (regardless of the successful completion or not).
    - Consumable items queues: each item fetched from these queues will only be put again their respective queue if the
        call of the function raised an Exception. Otherwise, the item will be treated as successfully consumed.
    We assume items in all queues are loaded before running the thread.

    Attributes:
        func                        The function to be run. It's arity should be
                                        func(recyclable_item_1, ..., recyclable_item_p,
                                            consumable_item_1, ..., consumable_item_p,
                                            arg_1, ..., arg_n,
                                            karg_1, ..., karg_m)
        recyclable_items_queues     List of queues from where each recyclable item will be fetched.
        consumable_items_queues     List of queues from where each consumable item will be fetched..
        extra_args                  The extra arguments to be applied to the function: arg_1, ..., arg_n
        extra_kwargs                The extra keyword arguments to be applied to the function: karg_1, ..., karg_m
        output_queue                Optional queue to store the results of the function call.
        fail_times_limit            Maximum number of times an item can fail before being discarded.
        default_output              Default output in case an item fails more than the allowed number of times.
        name                        Name of the thread.
    """

    def __init__(self, func,
                 items_queue=None, devices_queue=None,
                 items_are_consumable=True, devices_are_consumable=False,
                 extra_args=(), extra_kwargs=None, output_queue=None,
                 fail_times_limit=3, default_output=None, name=None):
        super().__init__(name=name)

        if items_queue is None and devices_queue is None:
            raise ValueError("items_queue and devices_queue can not be both None")

        self.items_queue = items_queue
        self.devices_queue = devices_queue

        self.items_are_consumable = items_are_consumable
        self.devices_are_consumable = devices_are_consumable

        self.func = func
        self.extra_args = extra_args
        if extra_kwargs is None:
            self.extra_kwargs = {}
        else:
            self.extra_kwargs = extra_kwargs

        self.output_queue = output_queue
        self.default_output = default_output
        self.fail_times_limit = fail_times_limit

        self.devices_used_by_item = {}
        self.failures_by_device = {}

        self.stop_event = Event()
        self.item_processing_start_time = None

    def stop(self):
        self.stop_event.set()

    def get_item_processing_start_time(self):
        return self.item_processing_start_time

    def run(self):
        try:
            while not self.stop_event.is_set():
                # prepare args for calling func
                args, device, item, queue_run_out = self.build_arguments()

                if queue_run_out:
                    # no more work to do
                    return

                try:
                    self.item_processing_start_time = time.time()
                    result = self.func(*args, **self.extra_kwargs)
                    self.item_processing_start_time = None

                    if self.output_queue is not None:
                        self.output_queue.put_nowait(result)

                except Exception as e:
                    self.log_exception(e, traceback.format_exc(), device=device)

                    if device is not None:
                        self.register_device_failure(device)

                        if item is not None:
                            self.register_item_failure_in_device(item, device)

                finally:
                    self.mark_task_done(item, self.items_queue)
                    self.mark_task_done(device, self.devices_queue)

                    # Put the recyclable items back in their respective queue
                    self.put_back_recyclables(device, item)

        except Exception as e:
            print(traceback.format_exc())
            return

    def build_arguments(self):
        queue_run_out = False
        item = None
        device = None
        args = []

        if self.items_queue is not None:
            item = self.fetch_item()
            if item is None:
                queue_run_out = True
            else:
                # if the items_queue is being used, the device selected for one item
                # has to take into account previous failures of that item
                if self.devices_queue is not None:
                    device = self.fetch_device_for_item(item)
                    if device is None:
                        queue_run_out = True
                    else:
                        args.append(device)

                # item goes in args after device, just by convention inside Evolutiz.
                args.append(item)
        else:
            # if the items_queue is disabled, we can use any device
            device = self.fetch_device()
            if device is None:
                queue_run_out = True
            else:
                args.append(device)

        args.extend(self.extra_args)
        args = tuple(args)
        return args, device, item, queue_run_out

    def log_exception(self, e, stack_trace, device=None):
        verbose_level = RequiredFeature('verbose_level').request()
        if verbose_level == 0:
            return

        template_base = "\nAn error occurred when calling func in MultipleQueueConsumerThread"

        if device is None:
            if verbose_level > 0:
                template = template_base + ": \n%s"
                formatted_string = template % stack_trace
                logger.log_progress(formatted_string)

            else:
                logger.log_progress(template_base + ".\n")

        else:
            if verbose_level > 0:
                template = template_base + " on device %s: \n%s"
                formatted_string = template % (device, traceback.format_exc())
                logger.log_progress(formatted_string)

            else:
                template = template_base + " on device %s.\n"
                formatted_string = template % device
                logger.log_progress(formatted_string)

    def fetch_item(self):
        try:
            item = self.items_queue.get_nowait()

            # init failures for this item
            if str(item) not in self.devices_used_by_item:
                self.devices_used_by_item[str(item)] = []

            return item
        except Empty as e:
            return None

    def fetch_device_for_item(self, item):
        devices_blacklisted = self.devices_used_by_item[str(item)]

        try:
            # draw from the devices_queue until we find a device which is not blacklisted
            device = self.devices_queue.get_nowait()

            # FIX: el siguiente código puede hacer que, dados dos threads concurrentes,
            # uno desencola todos los devices, el otro ve que no hay más devices se cierra,
            # y luego el primero vuelve a encolar los devices

            popped_devices = []
            while device in devices_blacklisted:
                popped_devices.append(device)
                device = self.devices_queue.get_nowait()

            # return to the queue the devices that are not going to be used
            for device in popped_devices:
                self.devices_queue.put_nowait(device)

            # init failures for this device
            if device not in self.failures_by_device:
                self.failures_by_device[device] = 0
            
            return device
        except Empty as e:
            return None

    def fetch_device(self):
        try:
            device = self.devices_queue.get_nowait()

            # init failures for this device
            if device not in self.failures_by_device:
                self.failures_by_device[device] = 0

            return device
        except Empty as e:
            return None

    def mark_task_done(self, element, queue):
        if element is not None and queue is not None:
            queue.task_done()

    def put_back_recyclables(self, device, item):
        if not self.items_are_consumable:
            self.items_queue.put_nowait(item)
        if not self.devices_are_consumable:
            self.devices_queue.put_nowait(device)

    def register_device_failure(self, device):
        # set state ready_idle in device that might be still in state ready_working
        device.mark_work_stop()

        self.failures_by_device[device] += 1
        if self.failures_by_device[device] > 5:
            device.flag_as_malfunctioning()

    def register_item_failure_in_device(self, item, device):
        self.devices_used_by_item[str(item)].append(device)

        if len(self.devices_used_by_item[str(item)]) < self.fail_times_limit:
            # Put the consumable items back in their respective queue
            if self.items_are_consumable:
                self.items_queue.put_nowait(item)

            if self.devices_are_consumable:
                self.devices_queue.put_nowait(device)
        else:
            if self.output_queue is not None:
                self.output_queue.put_nowait(self.default_output)
