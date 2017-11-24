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

EXPERIMENT_TIME = 5
COVERAGE_INTERVAL = 10
REPETITIONS = 2
timeout_cmd = "timeout " + str(EXPERIMENT_TIME) + "m "

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
        os.system("rm " + result_dir + "/*" + logger.redirect_string())

        instrument_apk(folder_name, result_dir)
        global package_name
        package_name = prepare_apk(devices, app_path)

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

        return (True, device)
    except Exception as e:
        logger.log_progress("\nThere was an error running monkey on app: " + folder_name)
        traceback.print_exc()
        return (False, device)


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
        run_sapienz_one_app(app_paths[i], devices)

    print "### Finished run_sapienz"


def process_results(app_paths):
    results_per_app = {}
    for app_path in app_paths:
        folder_name = os.path.basename(app_path)
        current_relative_dir = "monkey/results/" + folder_name
        os.chdir(current_relative_dir)

        results_per_repetition = []
        for repetition in range(0, REPETITIONS):
            unique_crashes = set()
            crashes_length = []
            coverage = 0

            events_count = 0
            current_test_content = ""

            with open("monkey.log." + str(repetition), "r") as monkey_log_file:
                for line_no, line in enumerate(monkey_log_file):
                    if line.startswith(":Sending"):
                        events_count += 1
                        current_test_content += line
                    if line.startswith("// CRASH:") and not line.startswith("// CRASH: com.android."):
                        crashes_length.append(events_count)
                        events_count = 0
                        if current_test_content not in unique_crashes:
                            unique_crashes.add(current_test_content)
                            current_test_content = ""

            coverage_filename = "coverage.ec." + str(repetition)

            os.system(
                "java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em," + coverage_filename + logger.redirect_string())

            html_file = settings.WORKING_DIR + current_relative_dir + "/coverage/index.html"

            try:
                coverage_str = emma_coverage.extract_coverage(html_file)
                os.system("mv coverage/ coverage." + str(repetition) + logger.redirect_string())
            except Exception, e:
                print "Exception occurred trying to extra coverage from html file: ", str(e)

            if coverage_str.find("%") != -1:
                coverage = int(coverage_str.split("%")[0])

            avg_crash_length = 0
            if len(crashes_length) > 0:
                avg_crash_length = numpy.mean(crashes_length)

            results_per_repetition.append((coverage, avg_crash_length, len(unique_crashes)))

        results_per_app[folder_name] = results_per_repetition

    return results_per_app


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
    # python -m monkey.run_sapienz

    logger.prepare()
    logger.clear_progress()
    logger.log_progress("Sapienz")

    app_paths = get_subject_paths()[0:1]
    run_sapienz(app_paths)
    results_per_app = process_results(app_paths)

    logger.log_progress("\n" + str(results_per_app))
    print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
