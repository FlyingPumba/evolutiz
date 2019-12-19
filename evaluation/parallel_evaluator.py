from typing import List

from concurrency.mapper_on_devices import MapperOnDevices
from dependency_injection.required_feature import RequiredFeature
from generation.individual import Individual
from util import logger


class ParallelEvaluator(object):
    """This class provides functionality to evaluate a list of individuals in a parallel manner.
    """

    def __init__(self) -> None:
        self.device_manager = RequiredFeature('device_manager').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.toolbox = RequiredFeature('toolbox').request()

    def evaluate(self, individuals: List[Individual]) -> bool:
        """Evaluate the inviduals with invalid fitness.
        Each individual is evaluated at most 3 times (in case of failure).

        :param individuals
        :returns True if the time or evaluation budget was enough to evaluate all individuals with invalid fitness.
        """
        invalid_ind = [ind for ind in individuals if not ind.fitness.valid]
        logger.log_progress("\nEvaluating {0} (out of {1}) individuals in parallel"
                            .format(len(invalid_ind), len(individuals)))

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
