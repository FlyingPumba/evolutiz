import time
import traceback
from queue import Empty
from threading import Event

from dependency_injection.required_feature import RequiredFeature
from devices.device_state import State
from util import logger
from concurrency.killable_thread import KillableThread


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
                 fail_times_limit=1, default_output=None, name=None):
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
                        self.output_queue.put(result)

                except Exception as e:
                    self.log_exception(e, traceback.format_exc(), device=device)

                    if device is not None:
                        self.register_device_failure(device)

                        if item is not None:
                            self.register_item_failure_in_device(item, device)

                finally:
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
            while True:
                item = self.fetch_item()
                if item is None:
                    queue_run_out = True
                else:
                    # if the items_queue is being used, the device selected for one item
                    # has to take into account previous failures of that item
                    if self.devices_queue is not None:
                        device = self.fetch_device_for_item(item)
                        if device is None:
                            # No matching device for this item right now
                            # Put back the item at the end of the queue, and fetch another one.
                            self.items_queue.put(item)
                            time.sleep(1)
                            continue
                        else:
                            args.append(device)

                    # item goes in args after device, just by convention inside Evolutiz.
                    args.append(item)
                break
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
            item = self.items_queue.pop()

            # init failures for this item
            if not hasattr(item, 'devices_used'):
                item.devices_used = []

            return item
        except Empty as e:
            return None

    def fetch_device_for_item(self, item):
        devices_blacklisted = item.devices_used
        device = self.devices_queue.pop_with_blacklist(devices_blacklisted)

        if device is None:
            # If this device is None, it means that all devices currently in the queue are blacklisted.
            # Since we ensured that fail_times_limit <= len(devices), we know that at some point in the future
            # another device will become available to process this item.
            return None

        if device.state != State.ready_idle:
            # This device is probably rebooting or being set up.
            # Don't use it and put it back in the queue
            self.devices_queue.put(device)
            return None

        return device

    def fetch_device(self):
        try:
            device = self.devices_queue.pop()

            return device
        except Empty as e:
            return None

    def put_back_recyclables(self, device, item):
        if not self.items_are_consumable:
            self.items_queue.put(item)
        if not self.devices_are_consumable:
            self.devices_queue.put(device)

    def register_device_failure(self, device):
        # set state ready_idle in device that might be still in state ready_working
        device.mark_work_stop()
        device.register_failure()

    def register_item_failure_in_device(self, item, device):
        item.devices_used.append(device)

        if len(item.devices_used) < self.fail_times_limit:
            # Put the consumable items back in their respective queue
            if self.items_are_consumable:
                self.items_queue.put(item)

            if self.devices_are_consumable:
                self.devices_queue.put(device)
        else:
            if self.output_queue is not None:
                self.output_queue.put(self.default_output)
