import time
import threading

import settings
from dependency_injection.required_feature import RequiredFeature
from util import logger
from concurrency.multiple_queue_consumer_thread import MultipleQueueConsumerThread
from concurrency.thread_hung_exception import ThreadHungException


class WatchDogThread(threading.Thread):

    def __init__(self, queue_to_join, output_queue, expected_output_count):
        super().__init__(name="WatchDogThread")

        self.queue_to_join = queue_to_join
        self.output_queue = output_queue
        self.expected_output_count = expected_output_count

        self.stop_event = threading.Event()
        self.successful_finish = False

    def stop(self):
        self.stop_event.set()

    def finished(self):
        return not self.is_alive()

    def finished_successfully(self):
        return self.successful_finish

    def run(self):
        verbose_level = RequiredFeature('verbose_level').request()
        budget_manager = RequiredFeature('budget_manager').request()

        # watchdog the threads while we are not asked to stop, there is budget and items available to consume
        budget_available = True
        stopped_threads = False
        while not self.stop_event.is_set() and \
                not self.output_queue.qsize() >= self.expected_output_count and \
                budget_available:

            budget_available = budget_manager.time_budget_available()

            # check that every MultipleQueueConsumerThread is not hung
            threads_to_check = [thread for thread in threading.enumerate() if
                                type(thread) is MultipleQueueConsumerThread]

            if verbose_level > 1:
                logger.log_progress("\nWatchDog thread is about to check %d MultipleQueueConsumer threads" % len(threads_to_check))

            if len(threads_to_check) == 0:
                break

            for thread in threads_to_check:
                start_time = thread.get_item_processing_start_time()
                elapsed_time = time.time() - start_time

                if not budget_available:
                    # ask nicely
                    logger.log_progress("\nTime budget run out, finishing thread: " + thread.name)
                    thread.stop()
                    stopped_threads = True

                # check if this thread has been processing a thread for more than 250 seconds,
                # this time should be more than enough to process a test case of 500 events.
                if elapsed_time > settings.TEST_CASE_EVAL_TIMEOUT + 50:
                    # this thread is presumably hung: no more mr. nice guy
                    # raising the exception only once will cause the current item to stop being processed
                    logger.log_progress("\nThread " + thread.name + " has been processing the same item for more than "
                                                                    "200 seconds, finishing item processing.")
                    thread.raiseExc(ThreadHungException)
                    # give time to the thread to process the exception
                    time.sleep(5)

            time.sleep(2)

        self.successful_finish = not stopped_threads
        if not stopped_threads:
            # only join the consumable queue if we are sure threads were able to run until the end
            # and were not preemptively stopped.
            self.queue_to_join.join()
