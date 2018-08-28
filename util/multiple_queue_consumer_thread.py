import threading
import time
import traceback
from Queue import Empty

from util import logger


class MultipleQueueConsumerThread(threading.Thread):
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

    def __init__(self, func, recyclable_items_queues=None, consumable_items_queues=None, extra_args=(), extra_kwargs=None, output_queue=None):
        threading.Thread.__init__(self)

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

    def run(self):
        try:
            while True:
                recyclable_items = self.get_items_from_list_of_queues(self.recyclable_items_queues)
                consumable_items = self.get_items_from_list_of_queues(self.consumable_items_queues)

                try:
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
                    logger.log_progress("An error occurred when calling func in MultipleQueueConsumerThread:\n" + traceback.format_exc())

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

                    # pause this thread for a second to give time for items just put in queues to be "gettable"
                    time.sleep(1)

        except Exception as e:
            print e
            return

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
            for index in xrange(0, len(items)):
                list_of_queues[index].task_done()