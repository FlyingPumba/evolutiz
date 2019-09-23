from concurrency.mapper_on_devices import MapperOnDevices
from dependency_injection.required_feature import RequiredFeature
from util import logger


class ParallelEvaluator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.toolbox = RequiredFeature('toolbox').request()

    def evaluate(self, individuals):
        invalid_ind = [ind for ind in individuals if not ind.fitness.valid]
        logger.log_progress("\nEvaluating " + str(len(invalid_ind)) + " (out of " + str(len(individuals)) + ") individuals in parallel")

        mapper = MapperOnDevices(self.test_suite_evaluator.evaluate,
                                 items_to_map=invalid_ind,
                                 fail_times_limit=3,
                                 idle_devices_only=True)
        try:
            mapper.run()

            # make sure that there are no individuals with invalid fitness
            for individual in individuals:
                if not individual.fitness.valid:
                    self.test_suite_evaluator.set_empty_fitness(individual)

            return True
        except TimeoutError:
            return False
