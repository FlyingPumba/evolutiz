import multiprocessing as mp
import time

from dependency_injection.di_assertions import HasMethods
from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.pickable import pickable_function


class PopulationGenerator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.individual_generator = RequiredFeature('individual_generator', HasMethods('gen_individual')).request()

        # variables for mp callback
        self.idle_devices = []
        self.total_individuals = 0
        self.individuals_generated = []
        self.remaining_individuals_index_to_generate = []

    def process_results(self, result):
        data, individual_index, device, success = result
        if not success:
            logger.log_progress(
                "\nInit population in parallel: failed to generate_individual on device: " + device.name + "\n")
            # device was unable to complete the generation of individual
            self.remaining_individuals_index_to_generate.append(individual_index)
            self.idle_devices.append(device)
        else:
            self.individuals_generated.append(data)
            self.idle_devices.append(device)
            logger.log_progress("\rInit population in parallel: " +
                                str(len(self.individuals_generated)) + "/" + str(self.total_individuals))

    def generate(self, n, gen=""):
        self.idle_devices = []
        self.total_individuals = n
        self.individuals_generated = []
        self.remaining_individuals_index_to_generate = [i for i in range(0, self.total_individuals)]

        # get idle devices
        self.idle_devices.extend(self.device_manager.get_devices())
        total_devices = len(self.idle_devices)

        # 2. aissign tasks to devices
        logger.log_progress("\nInit population in parallel: " +
                            str(len(self.individuals_generated)) + "/" + str(self.total_individuals))

        pool = mp.Pool(processes=total_devices)
        while len(self.individuals_generated) < self.total_individuals:

            if len(self.remaining_individuals_index_to_generate) > 0 and len(self.idle_devices) > 0:
                individual_index = self.remaining_individuals_index_to_generate.pop(0)
                device = self.idle_devices.pop(0)
                pool.apply_async(pickable_function,
                                 args=(self.individual_generator, 'gen_individual',
                                       (device, gen, individual_index,)),
                                 callback=self.process_results)
            else:
                time.sleep(2)

        pool.close()  # prevent any more tasks from being submitted to the pool
        pool.terminate()  # stops the worker processes immediately without completing outstanding work
        pool.join()  # wait for the worker processes to exit

        return self.individuals_generated
