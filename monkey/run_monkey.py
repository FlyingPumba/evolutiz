import multiprocessing.pool
import time
import traceback
import subprocess as sub

import os

import numpy

import logger
import settings
from coverages import emma_coverage
from devices import any_device, adb
from datetime import datetime

# global results for mp callback
from devices.prepare_apk_parallel import prepare_apk

EXPERIMENT_TIME = 15
COVERAGE_INTERVAL = 10
REPETITIONS=4
timeout_cmd = "timeout " + str(EXPERIMENT_TIME) + "m "

results = []
idle_devices = []
total_individuals = 0

class NoDaemonProcess(multiprocessing.Process):
    # make 'daemon' attribute always return False
    def _get_daemon(self):
        return False
    def _set_daemon(self, value):
        pass
    daemon = property(_get_daemon, _set_daemon)

# We sub-class multiprocessing.pool.Pool instead of multiprocessing.Pool
# because the latter is only a wrapper function, not a proper class.
class NoDaemonPool(multiprocessing.pool.Pool):
    Process = NoDaemonProcess

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

def process_app_result(success):
    idle_devices.append(success[1])
    return True

# def startIntermediateCoverage(device, package_name, result_dir, monkey_finished_event):
#     iterations = EXPERIMENT_TIME / COVERAGE_INTERVAL
#     for i in range(0, iterations):
#         for j in range (0, COVERAGE_INTERVAL):
#             if monkey_finished_event.is_set():
#                 break
#             time.sleep(60)
#
#         if monkey_finished_event.is_set():
#             break
#         logger.log_progress("\nCollecting intermediate coverage in device: " + device)
#         # fix suffix
#         collectCoverage(device, package_name, result_dir, suffix=str(i))
#     return True

def collectCoverage(device, package_name, result_dir, suffix=""):
    logger.log_progress("\nSending coverage broadcast in device: " + device + " at: " + datetime.today().strftime("%H:%M:%S"))
    os.system(adb.adb_cmd_prefix + " -s " + device + " shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE" + logger.redirect_string())

    logger.log_progress("\nPulling coverage from device: " + device + " at: " + datetime.today().strftime("%H:%M:%S"))
    coverageFilePath = "/data/data/" + package_name + "/files/coverage.ec"
    os.system(adb.adb_cmd_prefix + " -s " + device + " pull " + coverageFilePath + " " + result_dir + "/coverage.ec" + suffix + logger.redirect_string())

    return True

def run_monkey_one_app(app_path, apk_path, package_name, device_name, device_number, result_dir):
    try:
        os.chdir(app_path)

        os.system(adb.adb_cmd_prefix + " -s " + device_name + " install " + apk_path + " 2>&1 >" + result_dir + "/install.log" + "." + device_name)

        logger.log_progress("\nPreparing device: " + device_name + " sdcard")
        adb.sudo_shell_command(device_name, "mount -o rw,remount rootfs /")
        adb.sudo_shell_command(device_name, "chmod 777 /mnt/sdcard")
        adb.sudo_shell_command(device_name, "mount -o rw,remount /system")

        for repetition in range(0, REPETITIONS):
            logger.log_progress("\nStarting repetition: " + str(repetition) + " for app: " + package_name + " in device: " + device_name)
            log_files_suffix = "." + str(repetition) + "." + str(device_number)

            # clear package data from previous runs
            adb.shell_command(device_name, "pm clear " + package_name)

            # clear logcat
            os.system(adb.adb_cmd_prefix +" -s " + device_name + " logcat -c")

            # run logcat
            logcat_file = open(result_dir + "/monkey.logcat" + log_files_suffix, 'w')
            sub.Popen(adb.adb_cmd_prefix +" -s " + device_name + " logcat", stdout=logcat_file, stderr=logcat_file, shell=True)

            # start dumping intermediate coverage
            #monkey_finished_event = multiprocessing.Event()
            # p = multiprocessing.Process(target=startIntermediateCoverage, args=(device, result_dir, monkey_finished_event))
            # p.start()

            # start running monkey with timeout EXPERIMENT_TIME
            logger.log_progress("\nStarting monkey for app: " + package_name + " in device: " + device_name + " at: " + datetime.today().strftime("%H:%M:%S"))
            monkey_cmd = timeout_cmd + adb.adb_cmd_prefix + " -s " + device_name + " shell monkey -p " + package_name + " -v --throttle 200 --ignore-crashes --ignore-native-crashes --ignore-timeouts --ignore-security-exceptions 1000000 2>&1 >" + result_dir + "/monkey.log" + log_files_suffix
            os.system(monkey_cmd)

            adb.pkill(device_name, "monkey")

            logger.log_progress("\nMonkey finished for app: " + package_name + " in device: " + device_name + " at: " + datetime.today().strftime("%H:%M:%S"))
            #monkey_finished_event.set()

            # p.join()

            # collect final coverage
            collectCoverage(device_name, package_name, result_dir, suffix=log_files_suffix)

        return (True, device_name)
    except Exception as e:
        logger.log_progress("\nThere was an error running monkey on app: " + package_name)
        traceback.print_exc()
        return (False, device_name)

def run_monkey(app_paths):
    print "Preparing devices ..."
    any_device.boot_devices()

    print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")

    any_device.clean_sdcard()

    idle_devices.extend(any_device.get_devices())

    # 2. assign tasks to devices
    pool = NoDaemonPool(processes=len(idle_devices))

    for i in range(0, len(app_paths)):
        while len(idle_devices) != settings.DEVICE_NUM:
            time.sleep(10)

        app_path = app_paths[i]
        folder_name = os.path.basename(app_path)
        result_dir = "../../results/" + folder_name

        os.chdir(app_path)
        os.system("rm " + result_dir + "/*" + logger.redirect_string())

        apk_path, package_name = instrument_apk(folder_name, result_dir)

        for j in range(0, settings.DEVICE_NUM):

            device = idle_devices.pop(0)

            pool.apply_async(run_monkey_one_app,
                             args=(app_path, apk_path, package_name, device, j, result_dir),
                             callback=process_app_result)

    print "run_monkey is wating for all processes to finish ... "
    pool.close()
    pool.join()

    print "### Finished run_monkey"

def process_results(app_paths):
    results_per_app = {}
    for app_path in app_paths:
        folder_name = os.path.basename(app_path)
        current_relative_dir = "monkey/results/" + folder_name
        os.chdir(settings.WORKING_DIR + current_relative_dir)

        results_per_repetition = []
        for repetition in range(0, REPETITIONS):
            unique_crashes = set()
            crashes_length = []
            max_coverage = 0

            for device_number in range(0, settings.DEVICE_NUM):
                log_files_suffix = "." + str(repetition) + "." + str(device_number)

                events_count = 0
                current_test_content = ""

                with open("monkey.log" + log_files_suffix, "r") as monkey_log_file:
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

                coverage_filename = "coverage.ec" + log_files_suffix
                os.system("java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em," + coverage_filename + logger.redirect_string())

                html_file = settings.WORKING_DIR + current_relative_dir + "/coverage/index.html"

                try:
                    coverage_str = emma_coverage.extract_coverage(html_file)
                    os.system("mv coverage/ coverage" + log_files_suffix + logger.redirect_string())
                except Exception, e:
                    print "Exception occurred trying to extra coverage from html file: ", str(e)

                if coverage_str.find("%") != -1:
                    coverage = int(coverage_str.split("%")[0])
                    if coverage > max_coverage:
                        max_coverage = coverage

            avg_crash_length = 0
            if len(crashes_length) > 0:
                avg_crash_length = numpy.mean(crashes_length)

            results_per_repetition.append((max_coverage, avg_crash_length, len(unique_crashes)))

        results_per_app[folder_name] = results_per_repetition

    return results_per_app


def get_subject_paths():
    p = sub.Popen("ls -d $PWD/monkey/subjects/*/", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    app_paths = []
    for line in output.strip().split('\n'):
        if "hydrate" not in line and "a2dp" not in line: # hydrate app doesn't compile yet, so don't bother
            app_paths.append(line.rstrip('/')) # remove trailing forward slash
    return app_paths

if __name__ == "__main__":
    # run this script from the root folder as:
    # python -m monkey.run_monkey

    logger.prepare()
    logger.clear_progress()
    logger.log_progress("Monkey")

    app_paths = get_subject_paths()[0:1]
    run_monkey(app_paths)
    results_per_app = process_results(app_paths)

    logger.log_progress("\n"+str(results_per_app))
    print str(results_per_app)

    # recover stdout and stderr
    logger.restore()