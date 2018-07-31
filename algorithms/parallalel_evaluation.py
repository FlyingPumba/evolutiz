import multiprocessing as mp
import time
import traceback

import logger
import settings
from devices import adb

# global results for mp callback
idle_devices = []
total_individuals = 0
remaining_index_to_evaluate = []
individuals_evaluated = []

def process_results(data):
    indi_index, fitness, device = data
    if not fitness:
        global remaining_index_to_evaluate
        remaining_index_to_evaluate.append(indi_index)
    else:
        global individuals_evaluated
        individuals_evaluated.append((indi_index, fitness))

        global idle_devices
        idle_devices.append(device)

        global total_individuals
        logger.log_progress("\rEvaluating in parallel: " + str(len(individuals_evaluated)) + "/" + str(total_individuals))
        logger.log_fitness_result(fitness)

# 0. prepare wrapper for eval function
def eval_suite_parallel_wrapper(eval_suite_parallel, individual, device, result_dir, apk_dir, package_name, gen, pop):
    try:
        print "starting eval_suite_parallel_wrapper for individual ", pop
        start_time = time.time()
        result = eval_suite_parallel(individual, device, result_dir, apk_dir, package_name, gen, pop)
        elapsed_time = time.time() - start_time
        print "Elapsed seconds to evaluate individual was ", elapsed_time
        return result
    except Exception as e:
        logger.log_progress("\nThere was an error evaluating individual in parallel on device:" + adb.get_device_name(device) + "\n")
        print e

        return pop, False, device

def evaluate_in_parallel(toolbox, individuals, gen):
    """ Evaluate the individuals fitnesses and assign them to each individual
    :param eval_fitness: The fitness evaluation fucntion
    :param individuals: The individuals under evaluation
    :param pool_size:
    :return: When all individuals have been evaluated
    """

    # init global states
    global individuals_evaluated
    while len(individuals_evaluated) > 0:
        individuals_evaluated.pop()

    global idle_devices
    while len(idle_devices) > 0:
        idle_devices.pop()

    global rebooting_devices
    rebooting_devices.clear()

    global total_individuals
    total_individuals = len(individuals)

    logger.log_progress("\nEvaluating in parallel: " + str(len(individuals_evaluated)) + "/" + str(total_individuals))

    # get idle devices
    idle_devices.extend(toolbox.get_device_manager().get_devices())
    total_devices = len(idle_devices)

    global remaining_index_to_evaluate
    remaining_index_to_evaluate = [i for i in range(0, total_individuals)]

    # 2. aissign tasks to devices
    pool = mp.Pool(processes=total_devices)
    time_out = False
    while len(individuals_evaluated) > 0:

        if not toolbox.time_budget_available():
            print "Time budget run out, exiting parallel evaluation"
            time_out = True
            break

        if len(remaining_index_to_evaluate) > 0 and len(idle_devices) > 0:
            device = idle_devices.pop(0)
            index = remaining_index_to_evaluate.pop(0)

            pool.apply_async(eval_suite_parallel_wrapper,
                             args=(toolbox.evaluate, individuals[index], device, toolbox.get_result_dir(),
                                   toolbox.get_apk_dir(), toolbox.get_package_name(), gen, index),
                             callback=process_results)
        else:
            time.sleep(2)

    pool.close()  # prevent any more tasks from being submitted to the pool
    pool.terminate()  # stops the worker processes immediately without completing outstanding work
    pool.join()  # wait for the worker processes to exit

    if not time_out:
        # assign results
        while len(individuals_evaluated) > 0:
            i, fitness = individuals_evaluated.pop(0)
            individuals[i].fitness.values = fitness

    return not time_out