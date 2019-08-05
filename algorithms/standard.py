# coding=utf-8
import time

import random

from algorithms.genetic_algorithm import GeneticAlgorithm
from concurrency.mapper_on_devices import MapperOnDevices
from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.integer import Integer


class Standard(GeneticAlgorithm):
    """Implements the Standard Evolutionary Algorithm as described in [CamposGFEA17]_.

    It starts by creating an initial random population of size p_n. Then, a pair of individuals is selected from the
    population using a strategy s_f, such as rank-based, elitism or tournament selection. Next, both selected
    individuals are recombined using crossover c_f (e.g., single point, multiple-point) with a probability of c_p to
    produce two new offspring o_1, o_2. Afterwards, mutation is applied on both offspring, independently changing the
    genes with a probability of m_p, which usually is equal to 1/n, where n is the number of genes in a chromosome.
    The two mutated offspring are then included in the next population. At the end of each iteration the fitness value
    of all individuals is computed.

    This implementation generalizes the typical implementation of Standard EA that uses only 2 parents to generate 2
    offspring per cycle. In this implementation we use _n_ parents to generate _n_ offspring, where _n_ is the number of
    devices available. This allows us to retain the "standard" nature of this EA while also leveraging the parallelism
    available.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self):
        super(Standard, self).__init__()

        device_manager = RequiredFeature('device_manager').request()
        self.offspring_size = len(device_manager.get_devices())
        self.parents_size = self.offspring_size if self.offspring_size > 2 else 2

    def evolve(self):
        verbose_level = RequiredFeature('verbose_level').request()

        for gen in range(1, self.max_generations + 1):

            if not self.budget_manager.is_budget_available():
                print("Budget ran out, exiting evolve")
                break

            logger.log_progress("\n---> Starting generation " + str(gen) + " at " +
                                str(self.budget_manager.get_time_budget_used()))

            # create new population
            new_population = []
            while len(new_population) < self.population_size:
                # calculate number of offspring to generate
                needed_offspring = self.population_size - len(new_population)
                offspring_number = self.offspring_size
                if offspring_number > needed_offspring:
                    offspring_number = needed_offspring

                # generate offspring
                parents = self.toolbox.select(self.population, self.parents_size)
                offspring = self.generate_offspring(parents, gen, offspring_number,
                                                    base_index_in_generation=len(new_population))

                # add offspring to new population
                new_population.extend(offspring)

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in new_population if not ind.fitness.valid]
            success = self.parallel_evaluator.evaluate(invalid_ind)

            if not success:
                print("Budget ran out during parallel evaluation, exiting evolve")
                break

            self.population = new_population.copy()

            self.device_manager.log_devices_battery(gen, self.result_dir)
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress("\nFinished generation " + str(gen) + " at " +
                                    str(self.budget_manager.get_time_budget_used()))

        return self.population

    def generate_offspring(self, parents, gen, offspring_number, base_index_in_generation=0):
        offspring = self.crossover(parents, gen, offspring_number, base_index_in_generation=base_index_in_generation)
        self.mutation(offspring)

        return offspring

    def crossover(self, parents, gen, offspring_number, base_index_in_generation=0):
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

    def mutation(self, offspring):
        for index_in_generation in range(len(offspring)):
            ind = offspring[index_in_generation]
            self.toolbox.mutate(ind)
