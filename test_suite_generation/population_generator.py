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
        else:
            self.individuals_generated.append(data)
            logger.log_progress("\rInit population in parallel: " +
                                str(len(self.individuals_generated)) + "/" + str(self.total_individuals))

        self.device_manager.mark_work_stop_on_device(device)

    def generate(self, n, gen=""):
        self.total_individuals = n
        self.individuals_generated = []
        self.remaining_individuals_index_to_generate = [i for i in range(0, self.total_individuals)]

        # get idle devices
        idle_devices = self.device_manager.get_idle_devices()

        # 2. aissign tasks to devices
        logger.log_progress("\nInit population in parallel: " +
                            str(len(self.individuals_generated)) + "/" + str(self.total_individuals))

        pool = mp.Pool(processes=len(idle_devices))
        while len(self.individuals_generated) < self.total_individuals:

            idle_devices = self.device_manager.get_idle_devices()

            if len(self.remaining_individuals_index_to_generate) > 0 and len(idle_devices) > 0:

                individual_index = self.remaining_individuals_index_to_generate.pop(0)
                device = idle_devices.pop(0)
                self.device_manager.mark_work_start_on_device(device)

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
