# coding=utf-8
import random
import time
from typing import Any, List

from algorithms.genetic_algorithm import GeneticAlgorithm
from dependency_injection.required_feature import RequiredFeature
from util import logger


class Standard(GeneticAlgorithm):
    """Implements the Standard Evolutionary Algorithm as described in [CamposGFEA17]_.

    It starts by creating an initial random population of size p_n. Then, a pair of individuals is selected from the
    population using a strategy s_f, such as rank-based, elitism or tournament selection. Next, both selected
    individuals are recombined using crossover c_f (e.g., single point, multiple-point) with a probability of c_p to
    produce two new offspring o_1, o_2. Afterwards, mutation is applied on both offspring, independently changing the
    genes with a probability of m_p, which usually is equal to 1/n, where n is the number of genes in a chromosome.
    The two mutated offspring are then included in the next population. At the end of each iteration the fitness value
    of all individuals is computed.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self) -> None:
        super(Standard, self).__init__()

    def evolve(self) -> List[Any]:
        verbose_level: bool = RequiredFeature('verbose_level').request()

        for gen in range(1, self.max_generations):

            if not self.budget_manager.is_budget_available():
                print("Budget ran out, exiting evolve")
                break

            logger.log_progress(f"\n---> Starting generation {str(gen)} "
                                f"at {str(self.budget_manager.get_time_budget_used())}")

            # create new population, starting with elitism
            new_population: List[Any] = self.toolbox.selectBest(self.population, self.elitism_size)
            while len(new_population) < self.population_size:
                # select parents
                parents: List[Any] = self.toolbox.select(self.population, 2)

                # generate offspring
                needed_offspring = min(self.population_size - len(new_population), 2)
                offspring = self.crossover(parents, gen, needed_offspring, base_index_in_generation=len(new_population))
                self.mutation(offspring)

                # add offspring to new population
                new_population.extend(offspring)

            # evaluate new
            success = self.parallel_evaluator.evaluate(new_population)

            if not success:
                print("Budget ran out during parallel evaluation, exiting evolve")
                break

            self.population = new_population.copy()

            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress(f"\nFinished generation {str(gen)} "
                                    f"at {str(self.budget_manager.get_time_budget_used())}")

        return self.population

    def crossover(
            self,
            parents: List[Any],
            gen: int,
            offspring_number: int,
            base_index_in_generation: int = 0
    ) -> List[Any]:

        offspring = []
        for index_in_generation in range(0, offspring_number, 2):
            ind1, ind2 = map(self.toolbox.clone, random.sample(parents, 2))
            ind1, ind2 = self.toolbox.mate(ind1, ind2)

            del ind1.fitness.values
            ind1.index_in_generation = base_index_in_generation + index_in_generation
            ind1.generation = gen
            ind1.creation_finish_timestamp = time.time()
            ind1.creation_elapsed_time = 0

            offspring.append(ind1)

            if len(offspring) < offspring_number:
                del ind2.fitness.values
                ind2.index_in_generation = base_index_in_generation + index_in_generation + 1
                ind2.generation = gen
                ind2.creation_finish_timestamp = time.time()
                ind2.creation_elapsed_time = 0

                offspring.append(ind2)

        return offspring

    def mutation(self, offspring) -> None:
        for index_in_generation in range(len(offspring)):
            ind = offspring[index_in_generation]
            self.toolbox.mutate(ind)
