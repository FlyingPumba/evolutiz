import os
import pickle
import random
import sys

from deap import tools
from test_suite_evaluation.parallel_evaluator import ParallelEvaluator

import settings
from dependency_injection.required_feature import RequiredFeature
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
        self.toolbox = RequiredFeature('toolbox').request()
        self.result_dir = RequiredFeature('result_dir').request()
        self.population_generator = RequiredFeature('population_generator').request()
        self.parallel_evaluator = ParallelEvaluator()

        self.best_historic_crashes = 0
        self.best_historic_length = sys.maxint
        self.best_historic_coverage = 0

        assert (self.cxpb + self.mutpb) <= 1.0, ("The sum of the crossover and mutation "
                                                 "probabilities must be smaller or equal to 1.0.")

    def setup(self, stats=None, verbose=False):
        self.stats = stats
        self.verbose = verbose

        self.targets_historic_log_file = self.toolbox.get_result_dir() + "/targets-historic.log"
        self.setup_log_best_historic_objectives_achieved()

    def run(self):
        success = self.initPopulation()
        if not success:
            logger.log_progress("\nThere was an error initializing pupulation for app.")
            return False

        return self.evolve()

    def initPopulation(self):
        self.population = self.toolbox.population(n=settings.POPULATION_SIZE)
        if (len(self.population) < settings.POPULATION_SIZE):
            logger.log_progress("\nFailed to initialise population with proper size, exiting setup")
            return False

        # Evaluate the individuals with an invalid fitness
        invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
        completed_evaluation = self.parallel_evaluator.evaluate(invalid_ind, 0)

        if not completed_evaluation:
            logger.log_progress("\nTime budget run out durring parallel evaluation, exiting setup")
            return False

        # discard invalid population individual
        for i in range(len(self.population) - 1, -1, -1):
            if not self.population[i].fitness.valid:
                del self.population[i]

        self.update_best_historic_objectives_achieved(self.population, 0)

        self.device_manager.log_devices_battery(0, self.result_dir)
        return True

    def evolve(self):
        # record first population in logbook
        logbook = tools.Logbook()
        logbook.header = ['gen', 'nevals'] + (self.stats.fields if self.stats else [])

        if self.population is None:
            return [], logbook

        record = self.stats.compile(self.population) if self.stats is not None else {}
        invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
        logbook.record(gen=0, nevals=len(invalid_ind), **record)

        # Begin the generational process
        for gen in range(1, self.ngen + 1):

            if not self.budget_manager.time_budget_available():
                print "Time budget run out, exiting evolve"
                break

            print "Starting generation ", gen
            logger.log_progress("\n---> Starting generation " + str(gen))

            # Vary the population
            offspring = self.varOr(self.population)

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in offspring if not ind.fitness.valid]

            # this function will eval and match each invalid_ind to its fitness
            completed_evaluation = self.parallel_evaluator.evaluate(invalid_ind, gen)

            if not completed_evaluation:
                print "Time budget run out durring parallel evaluation, exiting evolve"
                break

            # discard invalid offspring individual
            for i in range(len(offspring) - 1, -1, -1):
                if not offspring[i].fitness.valid:
                    print "### Warning: Invalid Fitness"
                    del offspring[i]

            self.update_best_historic_objectives_achieved(offspring, gen)

            self.device_manager.log_devices_battery(gen, self.result_dir)

            # Select the next generation population
            self.population[:] = self.toolbox.select(self.population + offspring, self.mu)

            # Update the statistics with the new population
            record = self.stats.compile(self.population) if self.stats is not None else {}
            logbook.record(gen=gen, nevals=len(invalid_ind), **record)

            # dump logbook in case we are interrupted
            logbook_file = open(self.result_dir + "/intermediate/logbook.pickle", 'wb')
            pickle.dump(logbook, logbook_file)
            logbook_file.close()

        return self.population, logbook

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

    def update_best_historic_objectives_achieved(self, population, gen):
        for ind in population:
            fit = ind.fitness.values
            coverage = fit[0]
            length = fit[1]
            crashes = fit[2]

            if crashes > self.best_historic_crashes:
                self.best_historic_crashes = crashes

            if coverage > self.best_historic_coverage:
                self.best_historic_coverage = coverage

            if crashes > 0 and length < self.best_historic_length:
                self.best_historic_length = length

        logger.log_progress("\n- Best historic crashes: " + str(self.best_historic_crashes))
        logger.log_progress("\n- Best historic coverage: " + str(self.best_historic_coverage))
        if self.best_historic_crashes > 0:
            logger.log_progress("\n- Best historic length: " + str(self.best_historic_length))

        self.log_best_historic_objectives_achieved(gen)

    def setup_log_best_historic_objectives_achieved(self):
        log_file = self.targets_historic_log_file
        os.system("echo \"gen,coverage,crashes,length\" > " + log_file)

    def log_best_historic_objectives_achieved(self, gen):
        log_file = self.targets_historic_log_file
        echo_cmd = "echo \"" + \
                   str(gen) + "," + \
                   str(self.best_historic_coverage) + "," + \
                   str(self.best_historic_crashes) + ","

        if self.best_historic_crashes > 0:
            echo_cmd += str(self.best_historic_length) + " "
        else:
            echo_cmd += "-- "

        echo_cmd += "\" >> "
        os.system(echo_cmd + log_file)
