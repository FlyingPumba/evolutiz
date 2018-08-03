import multiprocessing as mp
import time

from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.pickable import pickable_function


class ParallelEvaluator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.toolbox = RequiredFeature('toolbox').request()

        # attributes for mp callback
        self.idle_devices = []
        self.total_individuals = 0
        self.remaining_index_to_evaluate = []
        self.individuals_evaluated = []

    def process_results(self, data):
        individual_index, fitness, device = data
        if not fitness:
            self.remaining_index_to_evaluate.append(individual_index)
        else:
            self.individuals_evaluated.append((individual_index, fitness))
            self.idle_devices.append(device)

            logger.log_progress("\rEvaluating in parallel: " +
                                str(len(self.individuals_evaluated)) + "/" + str(self.total_individuals))
            logger.log_fitness_result(fitness)

    def evaluation_wrapper(self, individual, device, gen, pop):
        try:
            print "starting eval_suite_parallel_wrapper for individual ", pop
            start_time = time.time()

            result = self.test_suite_evaluator.evaluate(individual, device, gen, pop)

            elapsed_time = time.time() - start_time
            print "Elapsed seconds to evaluate individual was ", elapsed_time

            return result
        except Exception as e:
            logger.log_progress("\nThere was an error evaluating individual in parallel on device:" +
                                device.name + "\n")
            print e
            return pop, False, device

    def evaluate(self, individuals, gen):
        if len(self.individuals_evaluated) > 0:
            self.individuals_evaluated = []

        if len(self.idle_devices) > 0:
            self.idle_devices = []

        self.total_individuals = len(individuals)

        logger.log_progress("\nEvaluating in parallel: " +
                            str(len(self.individuals_evaluated)) + "/" + str(self.total_individuals))

        # get idle devices
        self.idle_devices.extend(self.device_manager.get_devices())

        remaining_index_to_evaluate = [i for i in range(0, self.total_individuals)]

        # 2. aissign tasks to devices
        pool = mp.Pool(processes=len(self.idle_devices))
        time_out = False
        while len(self.individuals_evaluated) > 0:

            if not self.toolbox.time_budget_available():
                print "Time budget run out, exiting parallel evaluation"
                time_out = True
                break

            if len(remaining_index_to_evaluate) > 0 and len(self.idle_devices) > 0:
                device = self.idle_devices.pop(0)
                index = remaining_index_to_evaluate.pop(0)

                pool.apply_async(pickable_function,
                                 args=(self, 'evaluation_wrapper', (individuals[index], device, gen, index,)),
                                 callback=self.process_results)
            else:
                time.sleep(2)

        pool.close()  # prevent any more tasks from being submitted to the pool
        pool.terminate()  # stops the worker processes immediately without completing outstanding work
        pool.join()  # wait for the worker processes to exit

        if not time_out:
            # assign results
            while len(self.individuals_evaluated) > 0:
                i, fitness = self.individuals_evaluated.pop(0)
                individuals[i].fitness.values = fitness

        return not time_out
