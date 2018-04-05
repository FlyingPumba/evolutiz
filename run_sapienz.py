import os
import subprocess as sub
import traceback
import time
from datetime import datetime

import numpy
from deap import tools, base

import logger
import settings
from algorithms.eaMuPlusLambdaParallel import eaMuPlusLambdaParallel
from algorithms.gen_individual import gen_individual
from coverages import emma_coverage
from devices import adb
from devices import any_device
from devices.prepare_apk_parallel import prepare_apk
from init import initRepeatParallel

REPETITIONS = 1

start_time = None
apk_dir = None
package_name = None


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

def run_sapienz_one_app(app_path, devices):
    folder_name = os.path.basename(app_path)
    try:
        result_dir = "../../results/" + folder_name

        os.chdir(app_path)
        global apk_dir
        apk_dir = app_path
        os.system("rm -r " + result_dir + "/*" + logger.redirect_string())

        instrument_apk(folder_name, result_dir)
        global package_name
        package_name, installation_successful = prepare_apk(devices, app_path)
        if not installation_successful:
            logger.log_progress("\nUnable to install apk in all devices")
            return False

        for repetition in range(0, REPETITIONS):
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
            toolbox.register("individual", gen_individual)
            toolbox.register("population", initRepeatParallel.initPop, list, toolbox.individual)
            toolbox.register("time_budget_available", time_budget_available)
            toolbox.register("get_apk_dir", get_apk_dir)
            toolbox.register("get_package_name", get_package_name)

            # hof = tools.HallOfFame(6)
            # pareto front can be large, there is a similarity option parameter
            hof = tools.ParetoFront()

            # genetic algorithm
            eaStrategy = eaMuPlusLambdaParallel()
            eaStrategy.setup(toolbox)
            population = eaStrategy.evolve()

            logger.log_progress("\nSapienz finished for app: " + folder_name)

            # p.join()

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


def get_package_name():
    global package_name
    return package_name


def run_sapienz(app_paths):
    print "Preparing devices ..."
    any_device.boot_devices()

    any_device.prepare_motifcore()
    any_device.clean_sdcard()

    devices = any_device.get_devices()

    for device in devices:
        logger.log_progress("\nPreparing device: " + device + " sdcard")
        adb.sudo_shell_command(device, "mount -o rw,remount rootfs /")
        adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard")
        adb.sudo_shell_command(device, "mount -o rw,remount /system")

    for i in range(0, len(app_paths)):
        success = run_sapienz_one_app(app_paths[i], devices)
        if not success:
            break


    print "### Finished run_sapienz"


def get_subject_paths():
    p = sub.Popen("ls -d $PWD/monkey/subjects/*/", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    app_paths = []
    for line in output.strip().split('\n'):
        if "hydrate" not in line:  # hydrate app doesn't compile yet, so don't bother
            app_paths.append(line.rstrip('/'))  # remove trailing forward slash
    return app_paths


if __name__ == "__main__":
    # run this script from the root folder as:
    # python run_sapienz

    logger.prepare()
    logger.clear_progress()
    logger.log_progress("Sapienz")

    app_paths = get_subject_paths()[0:1]
    run_sapienz(app_paths)
    # results_per_app = process_results(app_paths)

    # logger.log_progress("\n" + str(results_per_app))
    # print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
