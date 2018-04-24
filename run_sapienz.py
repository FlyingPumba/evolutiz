import matplotlib
# Force matplotlib to not use any Xwindows backend.
matplotlib.use('Agg')

import argparse
import os
import pickle
import subprocess as sub
import traceback
import time
from datetime import datetime

import numpy
from deap import tools, base

import logger
import settings
from algorithms.eaDynaMosaParallel import eaDynaMosaParallel
from algorithms.eaMonotonicParallel import eaMonotonicParallel
from algorithms.eaMosaParallel import eaMosaParallel
from algorithms.eaMuPlusLambdaParallel import eaMuPlusLambdaParallel
from algorithms.eaOnePlusLambdaCommaLambdaParallel import eaOnePlusLambdaCommaLambdaParallel
from algorithms.eaStandardParallel import eaStandardParallel
from algorithms.eaSteadyStateParallel import eaSteadyStateParallel
from algorithms.randomParallel import randomParallel
from algorithms.gen_individual import gen_individual
from devices import adb
from devices import any_device
from devices.prepare_apk_parallel import prepare_apk
from init import initRepeatParallel
from plot import two_d_line

REPETITIONS = 1

start_time = None
apk_dir = None
result_dir = None
package_name = None
motifgene_enabled = True

def instrument_apk(folder_name, result_dir):
    logger.log_progress("\nInstrumenting app: " + folder_name)

    result_code = os.system("mkdir -p " + result_dir)
    if result_code != 0: raise Exception("Unable to create result dir")

    result_code = os.system("ant clean emma debug 2>&1 >" + result_dir + "/build.log")
    if result_code != 0: raise Exception("Unable run ant clean emma debug")

    result_code = os.system("cp bin/coverage.em " + result_dir + "/" + logger.redirect_string())
    if result_code != 0: raise Exception("Unable to copy coverage.em file")

    p = sub.Popen("ls bin/*-debug.apk", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    apk_path, errors = p.communicate()
    apk_path = apk_path.rstrip('\n')

    p = sub.Popen(
        "../../android-sdk-linux/build-tools/20.0.0/aapt d xmltree " + apk_path + " AndroidManifest.xml | grep package | awk 'BEGIN {FS=\"\\\"\"}{print $2}'",
        stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    package_name, errors = p.communicate()
    package_name = package_name.rstrip('\n')

    return apk_path, package_name


def check_devices_battery(devices):
    # check that all devices have enough battery
    battery_threshold = 75
    while True:
        all_devices_with_battery = True
        for device in devices:
            level = adb.get_battery_level(device)
            all_devices_with_battery = all_devices_with_battery and level >= battery_threshold

        if all_devices_with_battery:
            break
        else:
            print "Waiting for some devices to reach " + str(battery_threshold) + "% battery level"
            time.sleep(60)  # sleep 1 minute

def log_devices_battery(devices, result_dir, gen):
    log_file = result_dir + "/battery.log"
    os.system("echo 'Battery levels at gen: " + str(gen) + "' >> " + log_file)

    for device in devices:
        level = adb.get_battery_level(device)
        imei = adb.get_imei(device)
        os.system("echo '" + imei + " -> " + str(level) + "' >> " + log_file)


def run_sapienz_one_app(strategy_name, strategy, app_path, devices, use_motifgene=True):

    folder_name = os.path.basename(app_path)
    try:
        global motifgene_enabled
        motifgene_enabled = use_motifgene

        if not use_motifgene:
            strategy_name += "-nm"

        base_result_dir = os.path.dirname(os.path.dirname(app_path)) + "/results/" + strategy_name + "/" + folder_name

        os.chdir(app_path)
        global apk_dir
        apk_dir = app_path
        os.system("rm -r " + base_result_dir + "/*" + logger.redirect_string())

        instrument_apk(folder_name, base_result_dir)
        global package_name
        package_name, installation_successful = prepare_apk(devices, app_path, base_result_dir)
        if not installation_successful:
            logger.log_progress("\nUnable to install apk in all devices")
            return False

        for repetition in range(0, REPETITIONS):

            global result_dir
            result_dir = base_result_dir + "/" + str(repetition)
            os.system("mkdir -p " + result_dir)

            check_devices_battery(devices)
            log_devices_battery(devices, base_result_dir, "init")

            logger.log_progress("\nStarting repetition: " + str(repetition) + " for app: " + folder_name)

            # start time budget
            global start_time
            start_time = time.time()
            print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")

            for device in devices:
                # clear package data from previous runs
                adb.shell_command(device, "pm clear " + package_name)

            # register common functions in toolbox
            toolbox = base.Toolbox()
            toolbox.register("individual", gen_individual, use_motifgene)
            toolbox.register("population", initRepeatParallel.initPop, toolbox.individual)
            toolbox.register("time_budget_available", time_budget_available)
            toolbox.register("get_apk_dir", get_apk_dir)
            toolbox.register("get_result_dir", get_result_dir)
            toolbox.register("get_package_name", get_package_name)
            toolbox.register("is_motifgene_enabled", is_motifgene_enabled)
            toolbox.register("log_devices_battery", log_devices_battery, devices)

            stats = tools.Statistics(lambda ind: ind.fitness.values)
            # axis = 0, the numpy.mean will return an array of results
            stats.register("avg", numpy.mean, axis=0)
            stats.register("std", numpy.std, axis=0)
            stats.register("min", numpy.min, axis=0)
            stats.register("max", numpy.max, axis=0)
            stats.register("pop_fitness", return_as_is)

            # hof = tools.HallOfFame(6)
            # pareto front can be large, there is a similarity option parameter
            hof = tools.ParetoFront()

            # setup toolbox specific stuff by strategy
            strategy.setup(toolbox, stats=stats)

            # log the history
            history = tools.History()
            # Decorate the variation operators
            toolbox.decorate("mate", history.decorator)
            toolbox.decorate("mutate", history.decorator)

            # run the strategy
            strategy.initPopulation()
            population, logbook = strategy.evolve()

            logger.log_progress("\nSapienz finished for app: " + folder_name)

            # write stats
            logbook_file = open(result_dir + "/intermediate/logbook.pickle", 'wb')
            pickle.dump(logbook, logbook_file)
            logbook_file.close()

            hof_file = open(result_dir + "/intermediate/hof.pickle", 'wb')
            pickle.dump(hof, hof_file)
            hof_file.close()

            history_file = open(result_dir + "/intermediate/history.pickle", 'wb')
            pickle.dump(history, history_file)
            history_file.close()

            # draw graph
            two_d_line.plot(logbook, 0, result_dir)
            two_d_line.plot(logbook, 1, result_dir)
            two_d_line.plot(logbook, 2, result_dir)

        return True
    except Exception as e:
        logger.log_progress("\nThere was an error running sapienz on app: " + folder_name)
        traceback.print_exc()
        return False


def time_budget_available():
    current_time = time.time()
    global start_time
    elapsed_time = current_time - start_time
    return elapsed_time < settings.SEARCH_BUDGET_IN_SECONDS


def get_apk_dir():
    global apk_dir
    return apk_dir

def get_result_dir():
    global result_dir
    return result_dir

def get_package_name():
    global package_name
    return package_name

def is_motifgene_enabled():
    global motifgene_enabled
    return motifgene_enabled

def return_as_is(a):
    return a


def run_sapienz(strategy_name, strategy, app_paths, use_motifgene=True):
    print "Preparing devices ..."
    any_device.boot_devices()

    any_device.prepare_motifcore()
    any_device.clean_sdcard()

    devices = any_device.get_devices()

    # make /mnt/sdcard and /system writable
    for device in devices:
        logger.log_progress("\nPreparing device: " + device + " sdcard")
        adb.sudo_shell_command(device, "mount -o rw,remount rootfs /")
        adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard")
        adb.sudo_shell_command(device, "mount -o rw,remount /system")

    for i in range(0, len(app_paths)):
        success = run_sapienz_one_app(strategy_name, strategy, app_paths[i], devices, use_motifgene=use_motifgene)
        if not success:
            break


    print "### Finished run_sapienz"


def get_subject_paths(subjects_directory):
    p = sub.Popen("ls -d " + subjects_directory + "*/", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    app_paths = []
    for line in output.strip().split('\n'):
        if "hydrate" not in line:  # hydrate app doesn't compile yet, so don't bother
            app_paths.append(line.rstrip('/'))  # remove trailing forward slash
    return app_paths


if __name__ == "__main__":
    # run this script from the root folder as:
    # python run_sapienz

    possible_strategies = {
        "standard": eaStandardParallel(),
        "monotonic": eaMonotonicParallel(),
        "steady": eaSteadyStateParallel(),
        "muPlusLambda": eaMuPlusLambdaParallel(),
        "onePlusLambdaCommaLambda": eaOnePlusLambdaCommaLambdaParallel(),
        "mosa": eaMosaParallel(),
        "dynaMosa": eaDynaMosaParallel(),
        "random": randomParallel()
    }

    # parse args
    parser = argparse.ArgumentParser(description='Run Sapienz experiment with different strategies.')
    parser.add_argument('-d', '--subjects', dest='subjects_directory', default='$PWD/monkey/subjects/',
                        help='Directory where subjects are located')
    parser.add_argument('-s', '--strategy', dest='selected_strategy', default='muPlusLambda',
                        choices=possible_strategies.keys(), help='Strategy to be used')
    parser.add_argument('-nm', '--no-motifgene', dest='use_motifgene', action='store_false',
                        default=True, help='Disable motifgenes')

    args = parser.parse_args()
    app_paths = get_subject_paths(args.subjects_directory)[0:1]
    strategy = possible_strategies[args.selected_strategy]
    use_motifgene = args.use_motifgene

    # run Sapienz exp
    logger.prepare()
    logger.clear_progress()
    logger.log_progress("Sapienz (" + args.selected_strategy + ")")
    logger.log_progress("\nUse motifgene: " + str(use_motifgene))

    run_sapienz(args.selected_strategy, strategy, app_paths, use_motifgene=use_motifgene)

    # process results
    # results_per_app = process_results(app_paths)

    # logger.log_progress("\n" + str(results_per_app))
    # print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
