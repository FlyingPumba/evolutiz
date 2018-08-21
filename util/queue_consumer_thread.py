import threading
from Queue import Empty


class QueueConsumerThread(threading.Thread):
    """We assume all items in the queue are loaded before running the thread."""

    def __init__(self, queue, func, args=(), kwargs=None):
        threading.Thread.__init__(self)

        self.queue = queue
        self.func = func
        self.args = args
        if kwargs is None:
            self.kwargs = {}
        else:
            self.kwargs = kwargs

    def run(self):
        while True:
            item = None
            try:
                item = self.queue.get_nowait()
                self.func(item, *self.args, **self.kwargs)

            except Empty as e:
                # It looks like there are no more items to process
                self.queue.task_done()
                return

            except Exception as e:
                if item is not None:
                    # There was an error processing the item, put it back in the queue
                    self.queue.put_nowait(item)
