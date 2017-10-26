import multiprocessing as mp
import time
import traceback
import subprocess as sub

import os

import logger
from devices import any_device, adb
from datetime import datetime

# global results for mp callback
from devices.prepare_apk_parallel import prepare_apk

BASE_RESULTDIR="../../results/"

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
def eval_suite_parallel_wrapper(eval_suite_parallel, individual, device, apk_dir, package_name, gen, pop):
    try:
        print "starting eval_suite_parallel_wrapper for individual ", pop
        start_time = time.time()
        result = eval_suite_parallel(individual, device, apk_dir, package_name, gen, pop)
        elapsed_time = time.time() - start_time
        print "Elapsed seconds to evaluate individual was ", elapsed_time
        return result
    except Exception as e:
        print "There was an error evaluating individual in parallel"
        # print e
        traceback.print_exc()
        return pop, (0, 0, 0), device

def instrument_apk(app_path):
    result_dir = BASE_RESULTDIR + "package_name"+ "/"

    print app_path
    print result_dir

    os.system("cd " + app_path)
    os.system("mkdir -p " + result_dir)
    os.system("ant clean")
    os.system("ant emma debug &> " + result_dir + "build.log")
    os.system("ant installd &> " + result_dir + "install.log")
    os.system("cp bin/coverage.em &> " + result_dir)

    p = sub.Popen("ls bin/*-debug.apk", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    apk_path, errors = p.communicate()

    p = sub.Popen(
        "monkey/android-sdk-linux/build-tools/20.0.0/aapt d xmltree " + apk_path + " AndroidManifest.xml | grep package | awk 'BEGIN {FS=\"\\\"\"}{print $2}'",
        stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    package_name, errors = p.communicate()
    print package_name
    print errors

    return apk_path, package_name

def process_app_result(result):
    pass

def run_monkey_one_app(app_path, device):
    try:
        apk_path, package_name = instrument_apk(app_path)
        print package_name
        #package_name = prepare_apk([device], app_path)

        # run logcat
        #os.system(adb.adb_cmd_prefix  +" -s " + device + " logcat &> " + RESULTDIR + package_name  +"/monkey.logcat &")

        # start dumping intermediate coverage


        # start running monkey with timeout 1h

        # obtain coverage

        return True
    except Exception as e:
        print "There was an error running monkey on app: " + app_path
        # print e
        traceback.print_exc()
        return False

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
    pool = mp.Pool(processes=len(idle_devices))
    time_out = False
    for i in range(0, len(app_paths)):
        while len(idle_devices) == 0:
            time.sleep(60)

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
        app_paths.append(line)
    return app_paths

if __name__ == "__main__":
    # run this script from the root folder as:
    # python -m monkey.run_monkey

    app_paths = get_subject_paths()
    run_monkey(app_paths)