import multiprocessing as mp
import time
import traceback

import logger
import settings
from devices import any_device, adb

# global results for mp callback
results = []
remaining_index_to_evaluate = []
idle_devices = []
rebooting_devices = []
total_individuals = 0

def process_results(data):
    indi_index, fitness, device = data
    print "Finished evaluating an individual: ", indi_index, " ", fitness, " ", device

    if not fitness:
        # device was unable to complete the evaluation of individual, an thus was rebooted
        global rebooting_devices
        rebooting_devices.append(device)

        global remaining_index_to_evaluate
        remaining_index_to_evaluate.append(indi_index)
    else:
        global results
        results.append((indi_index, fitness))

        global idle_devices
        idle_devices.append(device)

        global total_individuals
        logger.log_progress("\rEvaluating in parallel: " + str(len(results)) + "/" + str(total_individuals))


# 0. prepare wrapper for eval function
def eval_suite_parallel_wrapper(motifgene_enabled, eval_suite_parallel, individual, device, result_dir, apk_dir, package_name, gen, pop):
    try:
        print "starting eval_suite_parallel_wrapper for individual ", pop
        start_time = time.time()
        result = eval_suite_parallel(motifgene_enabled, individual, device, result_dir, apk_dir, package_name, gen, pop)
        elapsed_time = time.time() - start_time
        print "Elapsed seconds to evaluate individual was ", elapsed_time
        # logger.log_progress("\nElapsed seconds to evaluate individual was " + str(elapsed_time))
        return result
    except Exception as e:
        logger.log_progress("\nThere was an error evaluating individual in parallel on device:" + adb.get_device_name(device) + "\n")
        # print e
        traceback.print_exc()

        return pop, False, device


def evaluate_in_parallel(toolbox, individuals, gen):
    """ Evaluate the individuals fitnesses and assign them to each individual
    :param eval_fitness: The fitness evaluation fucntion
    :param individuals: The individuals under evaluation
    :param pool_size:
    :return: When all individuals have been evaluated
    """

    # init global states
    global results
    while len(results) > 0:
        results.pop()

    global idle_devices
    while len(idle_devices) > 0:
        idle_devices.pop()

    global rebooting_devices
    while len(rebooting_devices) > 0:
        rebooting_devices.pop()

    global total_individuals
    total_individuals = len(individuals)

    logger.log_progress("\nEvaluating in parallel: " + str(len(results)) + "/" + str(total_individuals))

    # get idle devices
    idle_devices.extend(any_device.get_devices())

    if settings.DEBUG:
        print "idle devices after extending from any_device.get_devices()=", idle_devices
        print "number of devices", len(idle_devices)


    global remaining_index_to_evaluate
    remaining_index_to_evaluate = [ i for i in range(0, total_individuals)]

    # 2. aissign tasks to devices
    pool = mp.Pool(processes=len(idle_devices))
    time_out = False
    while len(remaining_index_to_evaluate) != 0:
        while len(idle_devices) == 0 and toolbox.time_budget_available():
            # print "Waiting for idle_devices"

            current_devices = any_device.get_devices()
            for device in current_devices:
                if device in rebooting_devices:
                    # a device finished rebooting
                    # wait a bit so it gets stabilized
                    print "Found that device " + device + " just finished rebooting"
                    time.sleep(settings.AVD_BOOT_DELAY)
                    time.sleep(settings.AVD_BOOT_DELAY)

                    if device in any_device.get_devices():
                        # lets hope that it really finished rebooting and its ready to be used
                        print "Device " + device + " looks like really finished rebooting"
                        rebooting_devices.remove(device)
                        idle_devices.append(device)

            time.sleep(1)

        if not toolbox.time_budget_available():
            print "Time budget run out, exiting parallel evaluation"
            time_out = True
            break

        device = idle_devices.pop(0)
        index = remaining_index_to_evaluate.pop(0)

        pool.apply_async(eval_suite_parallel_wrapper,
                         args=(toolbox.is_motifgene_enabled(), toolbox.evaluate, individuals[index], device, toolbox.get_result_dir(),
                               toolbox.get_apk_dir(), toolbox.get_package_name(), gen, index),
                         callback=process_results)

    pool.close()

    if not time_out:
        # should wait for all processes to finish
        print "### evaluate_in_parallel is wating for all processes to finish ... "
        pool.join()
        print "### ... evaluate_in_parallel finished"
        # assign results
        while len(results) > 0:
            i, fitness = results.pop(0)
            individuals[i].fitness.values = fitness
    else:
        pool.terminate()

    return not time_out
