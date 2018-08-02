import multiprocessing as mp
import time

from test_suite_generation.generate_individual_with_coverage import IndividualWithCoverageGenerator
from util import logger


class InitPopulationWithCoverage(object):

    def __init__(self, device_manager, test_runner, result_dir, apk_dir, package_name):

        self.device_manager = device_manager
        self.test_runner = test_runner
        self.result_dir = result_dir
        self.apk_dir = apk_dir
        self.package_name = package_name

        self.individual_generator = IndividualWithCoverageGenerator(test_runner, result_dir, apk_dir, package_name)
        self.generate_individual_function = self.individual_generator.gen_individual_with_coverage

        # variables for mp callback
        self.idle_devices = []
        self.total_individuals = 0
        self.remaining_individuals_to_generate = 0
        self.individuals_generated = []

    def process_results(self, data):
        individual, device = data
        if individual == False:
            logger.log_progress(
                "\nInit population in parallel: failed to generate_individual on device: " + device.name)
            # device was unable to complete the generation of individual
            self.remaining_individuals_to_generate += 1
        else:
            self.individuals_generated.append(individual)
            self.idle_devices.append(device)
            logger.log_progress(
                "\rInit population in parallel: " + str(len(self.individuals_generated)) + "/" + str(
                    self.total_individuals))

    def initPop(self, n, gen):
        self.total_individuals = n

        if len(self.individuals_generated) > 0:
            self.individuals_generated = []

        if len(self.idle_devices) > 0:
            self.idle_devices = []

        self.remaining_individuals_to_generate = self.total_individuals

        # get idle devices
        self.idle_devices.extend(self.device_manager.get_devices())
        total_devices = len(self.idle_devices)

        # 2. aissign tasks to devices
        logger.log_progress(
            "\nInit population in parallel: " + str(len(self.individuals_generated)) + "/" + str(
                self.total_individuals))

        pool = mp.Pool(processes=total_devices)
        while len(self.individuals_generated) < self.total_individuals:

            if self.remaining_individuals_to_generate > 0 and len(self.idle_devices) > 0:
                self.remaining_individuals_to_generate -= 1
                device = self.idle_devices.pop(0)
                pool.apply_async(self.generate_individual_function,
                                 args=(device, gen, self.remaining_individuals_to_generate),
                                 callback=self.process_results)
            else:
                time.sleep(2)

        pool.close()  # prevent any more tasks from being submitted to the pool
        pool.terminate()  # stops the worker processes immediately without completing outstanding work
        pool.join()  # wait for the worker processes to exit

        return self.individuals_generated
