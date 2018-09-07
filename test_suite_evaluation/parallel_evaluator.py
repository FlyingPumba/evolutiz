from dependency_injection.required_feature import RequiredFeature
from concurrency.mapper_on_devices import MapperOnDevices
from util import logger


class ParallelEvaluator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.budget_manager = RequiredFeature('budget_manager').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.toolbox = RequiredFeature('toolbox').request()

    def evaluate(self, individuals):
        logger.log_progress("\nEvaluating " + str(len(individuals)) + " individuals in parallel ")

        mapper = MapperOnDevices(self.test_suite_evaluator.evaluate,
                                 items_to_map=individuals,
                                 idle_devices_only=True)

        individuals_evaluated = mapper.run()

        return True
