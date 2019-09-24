# coding=utf-8
import time

import random
from typing import Any, List

from algorithms.genetic_algorithm import GeneticAlgorithm
from dependency_injection.required_feature import RequiredFeature
from util import logger


class MuPlusLambda(GeneticAlgorithm):
    """Implements the μ + λ Evolutionary Algorithm as described in [CamposGFEA17]_.

    The μ + λ Evolutionary Algorithm (EA) is a mutation-based algorithm. As its name suggests, the number of parents and
    offspring are restricted to μ and λ, respectively. Each gene is mutated independently with probability 1/n . After
    mutation, the generated offspring are compared with each parent, aiming to preserve so-far best individual including
    parents; that is, parents are replaced once a better offspring is found. Among the different (μ + λ) EA versions,
    two common settings are (1 + λ) EA and (1 + 1) EA, where the population size is 1, and the number of offspring is
    also limited to 1 for the (1 + 1) EA.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self) -> None:
        super(MuPlusLambda, self).__init__()

        # offpsring_size (Lambda) must be multiple of population_size (Mu)
        assert self.offspring_size % self.population_size == 0

    def evolve(self):
        verbose_level = RequiredFeature('verbose_level').request()

        for gen in range(1, self.max_generations):

            if not self.budget_manager.is_budget_available():
                print("Budget ran out, exiting evolve")
                break

            logger.log_progress("\n---> Starting generation " + str(gen) + " at " + str(self.budget_manager.get_time_budget_used()))

            # create and evaluate offspring
            offspring = self.generate_offspring(self.population, gen)
            success = self.parallel_evaluator.evaluate(offspring)
            if not success:
                print("Budget ran out during parallel evaluation, exiting evolve")
                break

            self.population[:] = self.toolbox.selectBest(self.population + offspring, self.population_size)

            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress("\nFinished generation " + str(gen) + " at " + str(self.budget_manager.get_time_budget_used()))

        return self.population

    def generate_offspring(self, population, gen) -> List[Any]:
        offspring = []
        index_in_generation = 0
        for p in population:
            for i in range(int(self.offspring_size / self.population_size)):
                ind = self.toolbox.clone(p)

                op_choice = random.random()
                if op_choice < self.mutation_probability:
                    ind, = self.toolbox.mutate([ind])
                    del ind.fitness.values

                ind.index_in_generation = index_in_generation
                ind.generation = gen
                ind.creation_finish_timestamp = time.time()
                ind.creation_elapsed_time = 0

                offspring.append(ind)
                index_in_generation += 1

        return offspring
