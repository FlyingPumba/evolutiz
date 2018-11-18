# coding=utf-8
import time

import random

from algorithms.genetic_algorithm import GeneticAlgorithm
from dependency_injection.required_feature import RequiredFeature
from util import logger


class SteadyState(GeneticAlgorithm):
    """Implements the Steady State Evolutionary Algorithm as described in [CamposGFEA17]_.

    The Steady State version of the Standard GA uses the same replacement strategy as the Monotonic GA, but instead of
    creating a new population of offspring, the offspring replace the parents from the current population immediately
    after the mutation phase.

    This implementation generalizes the typical implementation of Steady State that uses only 2 parents to generate 2
    offspring per cycle. In this implementation we use _n_ parents to generate _n_ offspring, where _n_ is the number of
    devices available. This allows us to retain the "steady" nature of this EA while also leveraging the parallelism
    available.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self):
        super(SteadyState, self).__init__()

    def evolve(self):
        verbose_level = RequiredFeature('verbose_level').request()
        device_manager = RequiredFeature('device_manager').request()
        self.offspring_size = len(device_manager.get_devices())

        if self.offspring_size < 2:
            raise Exception("Steady State EA needs at least 2 devices to work.")

        for gen in range(1, self.max_generations + 1):

            if not self.budget_manager.time_budget_available():
                print("Time budget run out, exiting evolve")
                break

            logger.log_progress("\n---> Starting generation " + str(gen) + " at " +
                                str(self.budget_manager.get_time_budget_used()))

            parents = self.toolbox.select(self.population, self.offspring_size)
            offspring = self.generate_offspring(parents, gen)

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
            success = self.parallel_evaluator.evaluate(invalid_ind)

            if not success:
                print("Time budget run out during parallel evaluation, exiting evolve")
                break

            best_ind, = self.toolbox.select(offspring + parents, 1)

            if best_ind in offspring:
                # only update population if the best individual among parents and offspring is a new one
                self.population = [ind for ind in self.population if ind not in parents] + offspring

            self.device_manager.log_devices_battery(gen, self.result_dir)
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress("\nFinished generation " + str(gen) + " at " +
                                    str(self.budget_manager.get_time_budget_used()))

        return self.population

    def generate_offspring(self, parents, gen):
        offspring = self.crossover(parents, gen)
        self.mutation(offspring)

        return offspring

    def crossover(self, parents, gen):
        offspring = []
        for index_in_generation in range(0, self.offspring_size, 2):
            ind1, ind2 = map(self.toolbox.clone, random.sample(parents, 2))
            ind1, ind2 = self.toolbox.mate(ind1, ind2)

            del ind1.fitness.values
            ind1.index_in_generation = index_in_generation
            ind1.generation = gen
            ind1.creation_finish_timestamp = time.time()
            ind1.creation_elapsed_time = 0

            offspring.append(ind1)

            del ind2.fitness.values
            ind2.index_in_generation = index_in_generation + 1
            ind2.generation = gen
            ind2.creation_finish_timestamp = time.time()
            ind2.creation_elapsed_time = 0

            offspring.append(ind2)

        return offspring

    def mutation(self, offspring):
        for index_in_generation in range(len(offspring)):
            ind = offspring[index_in_generation]
            self.toolbox.mutate(ind)
