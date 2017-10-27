import multiprocessing.pool
import time
import traceback
import subprocess as sub

import os

import logger
from devices import any_device, adb
from datetime import datetime

# global results for mp callback
from devices.prepare_apk_parallel import prepare_apk

EXPERIMENT_TIME = 15
COVERAGE_INTERVAL = 5
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

def instrument_apk(app_path, result_dir):
    os.chdir(app_path)
    os.system("mkdir -p " + result_dir)
    os.system("ant clean emma debug 2>&1 > " + result_dir + "/build.log")
    os.system("cp bin/coverage.em " + result_dir + "/")

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

def startIntermediateCoverage(device, result_dir):
    iterations = EXPERIMENT_TIME / COVERAGE_INTERVAL
    for i in range(0, iterations):
        time.sleep(60 * COVERAGE_INTERVAL) # sleep for interval time
        collectCoverage(device, result_dir)

def collectCoverage(device, result_dir):
    os.system(adb.adb_cmd_prefix + " -s " + device + " shell am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE")
    os.system(adb.adb_cmd_prefix + " -s " + device + " pull /mnt/sdcard/coverage.ec " + result_dir + "/coverage.e")

def run_monkey_one_app(app_path, device):
    try:
        folder_name = os.path.basename(app_path)
        result_dir = "../../results/" + folder_name
        apk_path, package_name = instrument_apk(app_path, result_dir)

        os.system(adb.adb_cmd_prefix + " -s " + device + " install " + apk_path + " 2>&1 >"  + result_dir  +"/install.log")

        # run logcat
        os.system(adb.adb_cmd_prefix  +" -s " + device + " logcat  2>&1 >" + result_dir  +"/monkey.logcat &")

        # start dumping intermediate coverage
        p = multiprocessing.Process(target=startIntermediateCoverage, args=(device, result_dir))
        p.start()

        # start running monkey with timeout 1h
        # should we add "--throttle 200" flag ? It's used in the experiments of "Are we there yet?" but it's usage in the sapienz experiments are unclear.
        monkey_cmd = timeout_cmd + adb.adb_cmd_prefix + " -s " + device + " shell monkey -p " + package_name + " -v 1000000 --ignore-crashes --ignore-timeouts --ignore-security-exceptions 2>&1 > " + result_dir + "/monkey.log"
        os.system(monkey_cmd)

        p.join()

        # collect final coverage
        collectCoverage(device, result_dir)

        return (True, device)
    except Exception as e:
        print "There was an error running monkey on app: " + app_path
        # print e
        traceback.print_exc()
        return (False, device)

def run_monkey(app_paths):
    # logger.prepare()
    # logger.clear_progress()
    # logger.log_progress("Monkey")

    print "Preparing devices ..."
    any_device.boot_devices()

    # start time budget
    global start_time
    start_time = time.time()
    print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")

    any_device.clean_sdcard()

    idle_devices.extend(any_device.get_devices())

    # 2. aissign tasks to devices
    pool = NoDaemonPool(processes=len(idle_devices))
    time_out = False
    for i in range(0, len(app_paths)):
        while len(idle_devices) == 0:
            time.sleep(10)

        device = idle_devices.pop(0)

        pool.apply_async(run_monkey_one_app,
                         args=(app_paths[i], device),
                         callback=process_app_result)

    print "run_monkey is wating for all processes to finish ... "
    pool.close()
    pool.join()

    print "### Finished run_monkey"

    # recover stdout and stderr
    # logger.restore()

def get_subject_paths():
    p = sub.Popen("ls -d $PWD/monkey/subjects/*/", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    app_paths = []
    for line in output.strip().split('\n'):
        if "hydrate" not in line: # hydrate app doesn't compile yet, so don't bother
            app_paths.append(line.rstrip('/')) # remove trailing forward slash
    return app_paths

if __name__ == "__main__":
    # run this script from the root folder as:
    # python -m monkey.run_monkey

    app_paths = get_subject_paths()
    run_monkey(app_paths[0:1])