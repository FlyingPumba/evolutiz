import matplotlib
# Force matplotlib to not use any Xwindows backend.
from devices.device_manager import DeviceManager
from test_runner.evolutiz_runner import EvolutizRunner
from test_runner.motifcore_runner import MotifcoreRunner

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
from algorithms.gen_individual_with_coverage import gen_individual_with_coverage
from devices import adb
from devices.prepare_apk_parallel import prepare_apk
from init import initRepeatParallel
from init import initRepeatParallelWithCoverage
from plot import two_d_line

REPETITIONS = 1

start_time = None
apk_dir = None
result_dir = None
package_name = None

def instrument_apk(folder_name, result_dir):
    logger.log_progress("\nInstrumenting app: " + folder_name)

    result_code = os.system("ant clean emma debug 2>&1 >" + result_dir + "/build.log")
    if result_code != 0: raise Exception("Unable run ant clean emma debug")

    result_code = os.system("cp bin/coverage.em " + result_dir + "/" + logger.redirect_string())
    if result_code != 0: raise Exception("Unable to copy coverage.em file")

    p = sub.Popen("ls bin/*-debug.apk", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    apk_path, errors = p.communicate()
    apk_path = apk_path.rstrip('\n')

    p = sub.Popen(
        settings.AAPT + " d xmltree " + apk_path + " AndroidManifest.xml | grep package | awk 'BEGIN {FS=\"\\\"\"}{print $2}'",
        stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    package_name, errors = p.communicate()
    package_name = package_name.rstrip('\n')

    return apk_path, package_name


def check_devices_battery(devices):
    # check that all devices have enough battery
    battery_threshold = 20
    while True:
        all_devices_with_battery = True
        for device in devices:
            level = adb.get_battery_level(device)
            all_devices_with_battery = all_devices_with_battery and level >= battery_threshold

        if all_devices_with_battery:
            break
        else:
            logger.log_progress("\nWaiting for some devices to reach " + str(battery_threshold) + "% battery level")
            time.sleep(60)  # sleep 1 minute

def log_devices_battery(device_manager, gen, result_dir):
    log_file = result_dir + "/battery.log"
    os.system("echo 'Battery levels at gen: " + str(gen) + "' >> " + log_file)

    for device in device_manager.get_devices():
        level = adb.get_battery_level(device)
        imei = adb.get_imei(device)
        os.system("echo '" + imei + " -> " + str(level) + "' >> " + log_file)


def run_sapienz_one_app(strategy_with_runner_name, strategy_class, test_runner, app_path):
    device_manager = DeviceManager()

    folder_name = os.path.basename(app_path)
    try:
        for repetition in range(0, REPETITIONS):

            # choose result_dir and create related directories
            global result_dir
            result_dir = os.path.dirname(
                os.path.dirname(app_path)) + "/results/" + strategy_with_runner_name + "/" + folder_name + "/" + str(repetition)
            adb.adb_logs_dir = result_dir

            os.chdir(app_path)
            global apk_dir
            apk_dir = app_path
            os.system("rm -rf " + result_dir + "/*" + logger.redirect_string())

            result_code = os.system("mkdir -p " + result_dir)
            if result_code != 0: raise Exception("Unable to create result dir")
            os.system("mkdir -p " + result_dir + "/intermediate")
            os.system("mkdir -p " + result_dir + "/coverage")
            os.system("mkdir -p " + result_dir + "/crashes")

            #  reboot all devices and restart adb server before starting a repetition
            adb.restart_server()
            device_manager.reboot_devices()

            logger.log_progress("\n-----> Starting repetition: " + str(repetition) + " for app: " + folder_name)

            device_manager.clean_sdcard()

            # give test runner opportunity to install on devices
            test_runner.install_on_devices()

            devices = device_manager.get_devices()

            instrument_apk(folder_name, result_dir)
            global package_name
            package_name, installation_successful = prepare_apk(devices, app_path, result_dir)
            if not installation_successful:
                logger.log_progress("\nUnable to install apk in all devices")
                return False

            check_devices_battery(devices)
            log_devices_battery(device_manager, "init", result_dir)

            # start time budget
            global start_time
            start_time = time.time()
            print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")

            for device in devices:
                # clear package data from previous runs
                adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

            # register common functions in toolbox
            toolbox = base.Toolbox()
            toolbox.register("individual", gen_individual, test_runner)
            toolbox.register("population", initRepeatParallel.initPop, toolbox.individual)
            toolbox.register("individual_with_coverage", gen_individual_with_coverage, test_runner)
            toolbox.register("population_with_coverage", initRepeatParallelWithCoverage.initPop, toolbox.individual_with_coverage)
            toolbox.register("time_budget_available", time_budget_available)
            toolbox.register("get_apk_dir", get_apk_dir)
            toolbox.register("get_result_dir", get_result_dir)
            toolbox.register("get_package_name", get_package_name)
            toolbox.register("log_devices_battery", log_devices_battery)

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
            strategy = strategy_class()
            strategy.setup(toolbox, test_runner, stats=stats)

            # log the history
            history = tools.History()
            # Decorate the variation operators
            toolbox.decorate("mate", history.decorator)
            toolbox.decorate("mutate", history.decorator)

            # run the strategy
            success = strategy.initPopulation()
            if not success:
                logger.log_progress("\nThere was an error initializing pupulation for app: " + folder_name)
                continue

            population, logbook = strategy.evolve()

            logger.log_progress("\nSapienz finished for app: " + folder_name + "\n")

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

def return_as_is(a):
    return a


def run_sapienz(strategy_name, strategy, test_runner, app_paths):
    for i in range(0, len(app_paths)):
        success = run_sapienz_one_app(strategy_name, strategy, test_runner, app_paths[i])
        if not success:
            break

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
        "standard": eaStandardParallel,
        "monotonic": eaMonotonicParallel,
        "steady": eaSteadyStateParallel,
        "muPlusLambda": eaMuPlusLambdaParallel,
        "onePlusLambdaCommaLambda": eaOnePlusLambdaCommaLambdaParallel,
        "mosa": eaMosaParallel,
        "dynaMosa": eaDynaMosaParallel,
        "random": randomParallel
    }

    possible_test_runners = {
        "motifcore": MotifcoreRunner(),
        "motifcore-nm": MotifcoreRunner(use_motifgene=True),
        "evolutiz": EvolutizRunner()
    }

    # parse args
    parser = argparse.ArgumentParser(description='Run Sapienz experiment with different strategies.')
    parser.add_argument('-d', '--subjects', dest='subjects_directory', default='$PWD/monkey/subjects/',
                        help='Directory where subjects are located')
    parser.add_argument('-s', '--strategy', dest='selected_strategy', default='muPlusLambda',
                        choices=possible_strategies.keys(), help='Strategy to be used')
    parser.add_argument('-t', '--test-runner', dest='selected_test_runner', default='motifcore',
                        choices=possible_test_runners.keys(), help='Test runner to be used')
    # parser.add_argument('-nm', '--no-motifgene', dest='use_motifgene', action='store_false',
    #                     default=True, help='Disable motifgenes')

    args = parser.parse_args()
    app_paths = get_subject_paths(args.subjects_directory)[0:1]

    strategy_with_runner_name = args.selected_strategy + "-" + args.selected_test_runner
    strategy_class = possible_strategies[args.selected_strategy]
    test_runner = possible_test_runners[args.selected_test_runner]

    # run Sapienz exp
    logger.prepare()
    logger.clear_progress()
    logger.log_progress("Sapienz (" + args.selected_strategy + ", " + args.selected_test_runner + ")")

    run_sapienz(strategy_with_runner_name, strategy_class, test_runner, app_paths)

    # process results
    # results_per_app = process_results(app_paths)

    # logger.log_progress("\n" + str(results_per_app))
    # print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
