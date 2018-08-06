import pickle
import random

from deap import tools

import settings
from dependency_injection.required_feature import RequiredFeature
from plot import two_d_line
from test_suite_evaluation.parallel_evaluator import ParallelEvaluator
from util import logger


class MuPlusLambda(object):

    def __init__(self):
        self.cxpb = settings.CXPB
        self.mutpb = settings.MUTPB
        self.ngen = settings.GENERATION
        self.mu = settings.POPULATION_SIZE
        self._lambda = settings.OFFSPRING_SIZE

        self.population = None
        self.device_manager = RequiredFeature('device_manager').request()
        self.budget_manager = RequiredFeature('budget_manager').request()
        self.result_dir = RequiredFeature('result_dir').request()
        self.population_generator = RequiredFeature('population_generator').request()
        self.parallel_evaluator = ParallelEvaluator()

        self.toolbox = RequiredFeature('toolbox').request()
        self.stats = RequiredFeature('stats').request()

        self.logbook = tools.Logbook()
        self.logbook.header = ['gen'] + (self.stats.fields if self.stats else [])

        assert (self.cxpb + self.mutpb) <= 1.0, ("The sum of the crossover and mutation "
                                                 "probabilities must be smaller or equal to 1.0.")

    def run(self):
        success = self.initPopulation()
        if not success:
            logger.log_progress("\nThere was an error initializing pupulation for app.")
            return False

        return self.evolve()

    def initPopulation(self):
        self.population = self.population_generator.generate(n=settings.POPULATION_SIZE)
        if len(self.population) < settings.POPULATION_SIZE:
            logger.log_progress("\nFailed to initialise population with proper size, exiting setup")
            return False

        # Evaluate the individuals with an invalid fitness
        invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
        individuals_evaluated = self.parallel_evaluator.evaluate(invalid_ind, 0)

        if individuals_evaluated is None:
            logger.log_progress("\nTime budget run out during parallel evaluation, exiting setup")
            return False

        self.population = individuals_evaluated[:]

        self.device_manager.log_devices_battery(0, self.result_dir)

        self.update_logbook(0)

        return True

    def evolve(self):
        for gen in range(1, self.ngen + 1):

            if not self.budget_manager.time_budget_available():
                print "Time budget run out, exiting evolve"
                break

            logger.log_progress("\n---> Starting generation " + str(gen))

            # Vary the population
            offspring = self.varOr(self.population)

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in offspring if not ind.fitness.valid]

            # this function will eval and match each invalid_ind to its fitness
            individuals_evaluated = self.parallel_evaluator.evaluate(invalid_ind, gen)

            if individuals_evaluated is None:
                print "Time budget run out during parallel evaluation, exiting evolve"
                break

            self.device_manager.log_devices_battery(gen, self.result_dir)

            # Select the next generation population
            self.population[:] = self.toolbox.select(self.population + offspring, self.mu)

            self.update_logbook(gen)

        return self.population

    def varOr(self, population):

        offspring = []
        for _ in xrange(self._lambda):
            op_choice = random.random()
            if op_choice < self.cxpb:  # Apply crossover
                ind1, ind2 = map(self.toolbox.clone, random.sample(population, 2))
                ind1, ind2 = self.toolbox.mate(ind1, ind2)
                del ind1.fitness.values
                offspring.append(ind1)
            elif op_choice < self.cxpb + self.mutpb:  # Apply mutation
                ind = self.toolbox.clone(random.choice(population))
                ind, = self.toolbox.mutate(ind)
                del ind.fitness.values
                offspring.append(ind)
            else:  # Apply reproduction
                offspring.append(random.choice(population))

        return offspring

    def dump_logbook_to_file(self):
        logbook_file = open(self.result_dir + "/logbook.pickle", 'wb')
        pickle.dump(self.logbook, logbook_file)
        logbook_file.close()

        # draw graph
        two_d_line.plot(self.logbook, 0, self.result_dir)
        two_d_line.plot(self.logbook, 1, self.result_dir)
        two_d_line.plot(self.logbook, 2, self.result_dir)

    def update_logbook(self, gen):
        record = self.stats.compile(self.population) if self.stats is not None else {}
        self.logbook.record(gen=gen, **record)
        self.show_best_historic_fitness()

    def show_best_historic_fitness(self):
        min_fitness_values_per_generation = self.logbook.select("min")
        max_fitness_values_per_generation = self.logbook.select("max")

        max_coverage = max(max_fitness_values_per_generation, key=lambda fit: fit[0])
        min_length = max(min_fitness_values_per_generation, key=lambda fit: fit[1])
        max_crashes = max(max_fitness_values_per_generation, key=lambda fit: fit[2])

        # CAUTION: these min and max are from different individuals
        logger.log_progress("\n- Best historic coverage: " + str(max_coverage))
        logger.log_progress("\n- Best historic crashes: " + str(max_crashes))
        if max_crashes > 0:
            logger.log_progress("\n- Best historic length: " + str(min_length))
