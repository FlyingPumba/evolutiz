import datetime
import multiprocessing as mp
import os
import time

import settings
from algorithms.genetic_algorithm import GeneticAlgorithm
from devices import adb
from util import logger
from util.pickable import pickable_function


class Standard(GeneticAlgorithm):

    def __init__(self):
        super(Standard, self).__init__()

        self.mutation_add_probability = 1 / float(3)
        self.mutation_modify_probability = 1 / float(3)
        self.mutation_delete_probability = 1 / float(3)

    def evolve(self):
        for gen in range(1, self.max_generations + 1):

            if not self.budget_manager.time_budget_available():
                print "Time budget run out, exiting evolve"
                break

            logger.log_progress("\n---> Starting generation " + str(gen))

            offspring = self.generate_offspring_in_parallel()

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
            individuals_evaluated = self.parallel_evaluator.evaluate(invalid_ind, gen)

            if individuals_evaluated is None:
                print "Time budget run out durring parallel evaluation, exiting evolve"
                break

            # Select the next generation population
            self.population = offspring

            self.device_manager.log_devices_battery(gen, self.result_dir)
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen)

        return self.population

    def generate_offspring_in_parallel(self):
        self.idle_devices = []
        self.idle_devices.extend(self.device_manager.get_devices())

        self.offspring_generated = []
        self.remaining_offspring_to_generate = self.offspring_size

        pool = mp.Pool(processes=len(self.idle_devices))
        while len(self.offspring_generated) < self.offspring_size:

            if self.remaining_offspring_to_generate > 0 and len(self.idle_devices) > 0:
                device = self.idle_devices.pop(0)
                self.remaining_offspring_to_generate -= 1

                pool.apply_async(pickable_function,
                                 args=(self, 'generate_two_offspring',
                                       (self, device,)),
                                 callback=self.two_offspring_generated)
            else:
                time.sleep(2)

        pool.close()  # prevent any more tasks from being submitted to the pool
        pool.terminate()  # stops the worker processes immediately without completing outstanding work
        pool.join()  # wait for the worker processes to exit

        return self.offspring_generated

    def two_offspring_generated(self, result):
        o1, o2, device = result
        self.offspring_generated.append(o1)
        self.offspring_generated.append(o2)

    def generate_two_offspring(self, device):
        p1, p2 = self.toolbox.select(self.population, 2)
        o1, o2 = self.toolbox.mate(p1, p2)
        o1, o2 = self.mutate(device, self.package_name, o1, o2)
        return o1, o2, device
