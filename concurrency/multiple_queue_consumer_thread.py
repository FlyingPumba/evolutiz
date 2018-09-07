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
    """

    def __init__(self, func, recyclable_items_queues=None, consumable_items_queues=None, extra_args=(),
                 extra_kwargs=None, output_queue=None, name=None):
        super().__init__(name=name)

        if recyclable_items_queues is None and consumable_items_queues is None:
            raise ValueError("recyclable_items_queues and consumable_items_queues can not be both None")

        self.recyclable_items_queues = recyclable_items_queues
        self.consumable_items_queues = consumable_items_queues

        self.func = func
        self.extra_args = extra_args
        if extra_kwargs is None:
            self.extra_kwargs = {}
        else:
            self.extra_kwargs = extra_kwargs

        self.output_queue = output_queue

        self.stop_event = Event()
        self.item_processing_start_time = None

    def stop(self):
        self.stop_event.set()

    def get_item_processing_start_time(self):
        return self.item_processing_start_time

    def run(self):
        try:
            while not self.stop_event.is_set():
                try:
                    self.item_processing_start_time = time.time()

                    recyclable_items = self.get_items_from_list_of_queues(self.recyclable_items_queues)
                    consumable_items = self.get_items_from_list_of_queues(self.consumable_items_queues)

                    if recyclable_items is not None and consumable_items is not None:
                        args = []
                        args.extend(recyclable_items)
                        args.extend(consumable_items)
                        args.extend(self.extra_args)
                        args = tuple(args)

                        result = self.func(*args, **self.extra_kwargs)
                        if self.output_queue is not None:
                            self.output_queue.put_nowait(result)

                except Exception as e:
                    # find out if there is a device we where working on
                    items = []
                    if recyclable_items is not None:
                        items.extend(recyclable_items)
                    if consumable_items is not None:
                        items.extend(consumable_items)
                    devices = [item for item in items if issubclass(type(item), Device)]

                    # log accordingly
                    if len(devices) > 0:
                        self.log_exception(e, device=devices[0])
                    else:
                        self.log_exception(e)

                    # There was an error processing the items, put the consumable items back in their respective queue
                    for index, item in enumerate(consumable_items):
                        queue = self.consumable_items_queues[index]
                        queue.put_nowait(item)
                finally:
                    self.mark_used_items(recyclable_items, self.recyclable_items_queues)
                    self.mark_used_items(consumable_items, self.consumable_items_queues)

                    # Put the recyclable items back in their respective queue
                    for index, item in enumerate(recyclable_items):
                        queue = self.recyclable_items_queues[index]
                        queue.put_nowait(item)

                    if recyclable_items is not None and consumable_items is not None:
                        # pause this thread for a second to give time for items just put in queues to be "gettable"
                        time.sleep(1)
                    else:
                        # there is nothing else to do, finish thread
                        return

        except Exception as e:
            print(e)
            return

    def log_exception(self, e, verbose_level, device=None):
        verbose_level = RequiredFeature('verbose_level').request()
        template_base = "\nAn error occurred when calling func in MultipleQueueConsumerThread"

        if device is None:
            if verbose_level > 1:
                template = template_base + ": \n%s"
                formatted_string = template % (traceback.format_exc())
                logger.log_progress(formatted_string)

            elif verbose_level > 0:
                logger.log_progress(template_base + ".\n")

        else:
            if verbose_level > 1:
                template = template_base + " on device %s: \n%s"
                formatted_string = template % (device, traceback.format_exc())
                logger.log_progress(formatted_string)

            elif verbose_level > 0:
                template = template_base + " on device %s.\n"
                formatted_string = template % device
                logger.log_progress(formatted_string)

    def get_items_from_list_of_queues(self, list_of_queues):
        """Try to get one item from each queue of the provided list.
        If at least one item of the queue is empty, return None."""
        items = []

        if list_of_queues is None:
            return items

        for queue in list_of_queues:
            try:
                item = queue.get_nowait()
                if item is not None:
                    items.append(item)
            except Empty as e:
                return None
        return items

    def mark_used_items(self, items, list_of_queues):
        """Mark task_done() once for each item used in all queues"""
        if items is not None and list_of_queues is not None:
            for index in range(0, len(items)):
                list_of_queues[index].task_done()
