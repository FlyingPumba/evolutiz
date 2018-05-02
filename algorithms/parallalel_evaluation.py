import multiprocessing as mp
import time
import traceback

import logger
import settings
from devices import any_device

# global results for mp callback
results = []
idle_devices = []
total_individuals = 0

def process_results(data):
    indi_index, fitness, device = data
    print "Finished evaluating an individual: ", indi_index, " ", fitness, " ", device

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
        logger.log_progress("There was an error evaluating individual in parallel on device:" + device)
        # print e
        traceback.print_exc()

        # return malformed response (not 3-tuple), so it will crash the process_results callback
        return False


def evaluate_in_parallel(toolbox, individuals, gen):
    """ Evaluate the individuals fitnesses and assign them to each individual
    :param eval_fitness: The fitness evaluation fucntion
    :param individuals: The individuals under evaluation
    :param pool_size:
    :return: When all individuals have been evaluated
    """

    global idle_devices
    if settings.DEBUG:
        print "### Starting evaluation in parallel"
        print "idle devices=", idle_devices

    global total_individuals
    global results
    total_individuals = len(individuals)
    logger.log_progress("\nEvaluating in parallel: " + str(len(results)) + "/" + str(total_individuals))

    # init global states
    while len(results) > 0:
        results.pop()
    while len(idle_devices) > 0:
        idle_devices.pop()

    # 1. get idle devices
    idle_devices.extend(any_device.get_devices())

    if settings.DEBUG:
        print "idle devices after extending from any_device.get_devices()=", idle_devices
        print "number of devices", len(idle_devices)

    # 2. aissign tasks to devices
    pool = mp.Pool(processes=len(idle_devices))
    time_out = False
    for i in range(0, len(individuals)):
        while len(idle_devices) == 0 and toolbox.time_budget_available():
            # print "Waiting for idle_devices"
            # print idle_devices
            time.sleep(1)

        if not toolbox.time_budget_available():
            print "Time budget run out, exiting parallel evaluation"
            time_out = True
            break

        device = idle_devices.pop(0)
        if settings.DEBUG:
            print "assigning task to device ", device
            print "individual is ", i

        pool.apply_async(eval_suite_parallel_wrapper,
                         args=(toolbox.is_motifgene_enabled(), toolbox.evaluate, individuals[i], device, toolbox.get_result_dir(),
                               toolbox.get_apk_dir(), toolbox.get_package_name(), gen, i),
                         callback=process_results)
    # res = pool.apply(eval_suite_parallel_wrapper,
    #				 args=(eval_suite_parallel, individuals[i], device, toolbox, gen, i))
    # process_results(res)

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
