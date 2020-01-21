from typing import List

from deap import creator

import settings
from algorithms.strategy import Strategy
from dependency_injection.required_feature import RequiredFeature
from generation.individual import Individual
from util import logger


class GeneticAlgorithm(Strategy):

    def __init__(self) -> None:
        super(GeneticAlgorithm, self).__init__()
        self.crossover_probability: float = settings.CXPB
        self.mutation_probability: float = settings.MUTPB
        self.max_generations: int = settings.GENERATION
        self.population_size: int = settings.POPULATION_SIZE
        self.offspring_size: int = settings.OFFSPRING_SIZE
        self.elitism_size: int = settings.ELITISM_SIZE

        self.population: List[Individual] = []
        self.package_name: str = ""

        assert (self.crossover_probability + self.mutation_probability) <= 1.0, (
            "The sum of the crossover and mutation "
            "probabilities must be smaller or equal to 1.0.")

    def run(self) -> List[Individual]:
        self.package_name = RequiredFeature('compiled_package_name').request()

        success = self.initPopulation()
        if not success:
            logger.log_progress("\nThere was an error initializing population for app.")
            raise Exception("There was an error initializing population for app.")

        return self.evolve()

    def initPopulation(self) -> bool:
        verbose_level: bool = RequiredFeature('verbose_level').request()

        if verbose_level > 0:
            logger.log_progress(f"\n---> Starting to generate initial population "
                                f"at {str(self.budget_manager.get_time_budget_used())}")

        self.population = self.population_generator.generate(self.population_size, gen=0)
        if self.population is None or len(self.population) < self.population_size:
            logger.log_progress("\nFailed to initialise population with proper size, exiting setup")
            return False

        if verbose_level > 0:
            logger.log_progress(f"\n---> Starting to evaluate initial population "
                                f"at {str(self.budget_manager.get_time_budget_used())}")

        success = self.parallel_evaluator.evaluate(self.population)

        if not success:
            logger.log_progress("\nBudget ran out during parallel evaluation, exiting setup")
            return False

        self.parallel_evaluator.test_suite_evaluator.update_logbook(0, self.population)

        history = RequiredFeature('history').request()
        history.update(self.population)

        if verbose_level > 0:
            logger.log_progress(f"\n---> Finished creating initial population "
                                f"at {str(self.budget_manager.get_time_budget_used())}")

        return True

    def evolve(self) -> List[Individual]:
        raise NotImplementedError


