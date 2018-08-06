import random

from algorithms.genetic_algorithm import GeneticAlgorithm
from util import logger


class MuPlusLambda(GeneticAlgorithm):

    def __init__(self):
        super(MuPlusLambda, self).__init__()

    def evolve(self):
        for gen in range(1, self.max_generations + 1):

            if not self.budget_manager.time_budget_available():
                print "Time budget run out, exiting evolve"
                break

            logger.log_progress("\n---> Starting generation " + str(gen))

            offspring = self.generate_offspring(self.population)

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
            individuals_evaluated = self.parallel_evaluator.evaluate(invalid_ind, gen)

            if individuals_evaluated is None:
                print "Time budget run out during parallel evaluation, exiting evolve"
                break

            self.population[:] = self.toolbox.select(self.population + offspring, self.population_size)

            self.device_manager.log_devices_battery(gen, self.result_dir)
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen)

        return self.population

    def generate_offspring(self, population):

        offspring = []
        for _ in xrange(self.offspring_size):
            op_choice = random.random()
            if op_choice < self.crossover_probability:  # Apply crossover
                ind1, ind2 = map(self.toolbox.clone, random.sample(population, 2))
                ind1, ind2 = self.toolbox.mate(ind1, ind2)
                del ind1.fitness.values
                offspring.append(ind1)
            elif op_choice < self.crossover_probability + self.mutation_probability:  # Apply mutation
                ind = self.toolbox.clone(random.choice(population))
                ind, = self.toolbox.mutate(ind)
                del ind.fitness.values
                offspring.append(ind)
            else:  # Apply reproduction
                offspring.append(random.choice(population))

        return offspring
