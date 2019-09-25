import threading
import time

import settings
from concurrency.multiple_queue_consumer_thread import MultipleQueueConsumerThread
from concurrency.queue import Queue
from concurrency.thread_hung_exception import ThreadHungException
from dependency_injection.required_feature import RequiredFeature
from util import logger


class WatchDogThread(threading.Thread):

    def __init__(self, output_queue: Queue, expected_output_count: int) -> None:
        super().__init__(name="WatchDogThread")

        self.output_queue = output_queue
        self.expected_output_count = expected_output_count

        self.stop_event = threading.Event()
        self.successful_finish = False
        self.watchdog_timeout = settings.TEST_CASE_EVAL_TIMEOUT + 100

    def stop(self):
        self.stop_event.set()

    def finished(self) -> bool:
        return not self.is_alive()

    def finished_successfully(self) -> bool:
        return self.successful_finish

    def run(self):
        verbose_level = RequiredFeature('verbose_level').request()
        budget_manager = RequiredFeature('budget_manager').request()

        # watchdog the threads while we are not asked to stop, there is budget and items available to consume
        budget_available = True
        stopped_threads = False
        while not self.stop_event.is_set() and \
                self.output_queue.size() < self.expected_output_count and \
                budget_available:

            budget_available = budget_manager.is_budget_available()

            # check that every MultipleQueueConsumerThread is not hung
            threads_to_check = [thread for thread in threading.enumerate() if
                                type(thread) is MultipleQueueConsumerThread]

            if verbose_level > 1:
                logger.log_progress("\nWatchDog thread is about to check %d MultipleQueueConsumer threads" %
                                    len(threads_to_check))
                logger.log_progress("\nCurrent output count is %d of %d" % (self.output_queue.size(),
                                                                            self.expected_output_count))

            if len(threads_to_check) == 0:
                break

            for thread in threads_to_check:
                start_time = thread.get_item_processing_start_time()
                if start_time is None:
                    # this thread is not processing an item right now
                    continue

                elapsed_time = time.time() - start_time

                if not budget_available:
                    # ask nicely
                    logger.log_progress("\nBudget ran out, finishing thread: " + thread.name)
                    thread.stop()
                    stopped_threads = True

                # check if this thread has been processing a thread for more than watchdog_timeout seconds,
                # this time should be more than enough to process a test case of 500 events.
                if elapsed_time > self.watchdog_timeout:
                    # this thread is presumably hung: no more mr. nice guy
                    # raising the exception only once will cause the current item to stop being processed
                    logger.log_progress("\nThread %s has been processing the same item for more than %d seconds, "
                                        "finishing item processing." % (thread.name, self.watchdog_timeout))
                    thread.raiseExc(ThreadHungException)
                    # give time to the thread to process the exception
                    time.sleep(1)

            time.sleep(1)

        self.successful_finish = not stopped_threads
