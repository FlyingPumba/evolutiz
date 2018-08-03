import multiprocessing as mp
import time

from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.pickable import pickable_function


class ParallelEvaluator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.budget_manager = RequiredFeature('budget_manager').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.toolbox = RequiredFeature('toolbox').request()

        # attributes for mp callback
        self.idle_devices = []
        self.total_individuals = 0
        self.remaining_index_to_evaluate = []
        self.individuals_evaluated = []

    def process_results(self, data):
        individual_evaluated, individual_index, device, success = data
        if not success:
            self.remaining_index_to_evaluate.append(individual_index)
        else:
            self.individuals_evaluated.append(individual_evaluated)
            self.idle_devices.append(device)
            logger.log_progress("\rEvaluating in parallel: " +
                                str(len(self.individuals_evaluated)) + "/" + str(self.total_individuals))

    def evaluate(self, individuals, gen):
        self.idle_devices = []
        self.individuals_evaluated = []
        self.total_individuals = len(individuals)

        logger.log_progress("\nEvaluating in parallel: " +
                            str(len(self.individuals_evaluated)) + "/" + str(self.total_individuals))

        # get idle devices
        self.idle_devices.extend(self.device_manager.get_devices())

        remaining_individuals_index_to_evaluate = [i for i in range(0, self.total_individuals)]

        # 2. aissign tasks to devices
        pool = mp.Pool(processes=len(self.idle_devices))
        time_out = False
        while len(self.individuals_evaluated) < self.total_individuals:

            if not self.budget_manager.time_budget_available():
                print "Time budget run out, exiting parallel evaluation"
                time_out = True
                break

            if len(remaining_individuals_index_to_evaluate) > 0 and len(self.idle_devices) > 0:
                device = self.idle_devices.pop(0)
                individual_index = remaining_individuals_index_to_evaluate.pop(0)

                pool.apply_async(pickable_function,
                                 args=(self.test_suite_evaluator, 'evaluate',
                                       (individuals[individual_index], device, gen, individual_index,)),
                                 callback=self.process_results)
            else:
                time.sleep(2)

        pool.close()  # prevent any more tasks from being submitted to the pool
        pool.terminate()  # stops the worker processes immediately without completing outstanding work
        pool.join()  # wait for the worker processes to exit

        if not time_out:
            return self.individuals_evaluated
        else:
            return None
