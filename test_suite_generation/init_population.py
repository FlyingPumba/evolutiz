import time

import multiprocessing as mp

from devices import adb
from util import logger

# global results for mp callback
idle_devices = []
total_individuals = 0
remaining_individuals_to_generate = 0
individuals_generated = 0

class InitPopulation(object):

    def process_results(data):
        individual, device = data
        if individual == False:
            logger.log_progress(
                "\nInit population in parallel: failed to generate_individual on device: " + device.name)
            # device was unable to complete the generation of individual
            global remaining_individuals_to_generate
            remaining_individuals_to_generate += 1
        else:
            global individuals_generated
            individuals_generated.append(individual)

            global idle_devices
            idle_devices.append(device)

            global total_individuals
            logger.log_progress(
                "\rInit population in parallel: " + str(len(individuals_generated)) + "/" + str(total_individuals))


    def initPop(device_manager, gen_individual_function, n, result_dir, package_name):
        # init global states
        global total_individuals
        total_individuals = n

        global individuals_generated
        if len(individuals_generated) > 0:
            individuals_generated = []

        global idle_devices
        if len(idle_devices) > 0:
            idle_devices = []

        global remaining_individuals_to_generate
        remaining_individuals_to_generate = total_individuals

        # get idle devices
        idle_devices.extend(device_manager.get_devices())
        total_devices = len(idle_devices)

        # 2. aissign tasks to devices
        logger.log_progress(
            "\nInit population in parallel: " + str(len(individuals_generated)) + "/" + str(total_individuals))

        pool = mp.Pool(processes=total_devices)
        while len(individuals_generated) < total_individuals:

            if remaining_individuals_to_generate > 0 and len(idle_devices) > 0:
                remaining_individuals_to_generate -= 1
                device = idle_devices.pop(0)
                pool.apply_async(gen_individual_function,
                                 args=(device, result_dir, package_name),
                                 callback=process_results)
            else:
                time.sleep(2)

        pool.close()  # prevent any more tasks from being submitted to the pool
        pool.terminate()  # stops the worker processes immediately without completing outstanding work
        pool.join()  # wait for the worker processes to exit

        return individuals_generated
