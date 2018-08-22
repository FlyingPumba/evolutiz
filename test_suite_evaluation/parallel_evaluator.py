import multiprocessing.dummy as mp
import time

from dependency_injection.required_feature import RequiredFeature
from devices.mapper_on_devices import MapperOnDevices
from util import logger
from util.pickable import pickable_function


class ParallelEvaluator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.budget_manager = RequiredFeature('budget_manager').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.toolbox = RequiredFeature('toolbox').request()

    def evaluate(self, individuals):
        logger.log_progress("\nEvaluating in parallel " + str(len(individuals)) + " individuals")

        mapper = MapperOnDevices(self.test_suite_evaluator.evaluate,
                                 items_to_map=individuals,
                                 idle_devices_only=True)

        individuals_evaluated = mapper.run()

        return individuals_evaluated
