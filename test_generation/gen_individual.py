import datetime

import random
import traceback
from deap import creator

import settings
from crashes import crash_handler
from devices import adb


class CanNotInitSeqException(Exception):
    pass


def get_suite(test_runner, device, result_dir, package_name):
    ret = []
    unique_crashes = set()

    for i in range(0, settings.SUITE_SIZE):
        seq = get_sequence(test_runner, device, result_dir, package_name, i, unique_crashes)
        ret.append(seq)

    return ret


def get_sequence(test_runner, device, result_dir, package_name, index, unique_crashes):
    random.seed()

    # clear data
    result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    if result_code != 0:
        device.flag_as_malfunctioning()
        raise Exception("Failed to clear package " + package_name + " in device: " + device.name)

    # access the generated script, should ignore the first launch activity
    script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
    ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

    motifcore_script_filename = result_dir + "/intermediate/" + script_name + ".init." + ts + "." + str(index)

    ret = test_runner.generate(device, package_name, motifcore_script_filename)

    # deal with crash
    crash_handler.handle(device, result_dir, motifcore_script_filename, "init", ts, index, unique_crashes)

    return ret


def gen_individual(test_runner, device, result_dir, package_name):
    try:
        suite = get_suite(test_runner, device, result_dir, package_name)
        return creator.Individual(suite), device

    except Exception as e:
        print e
        traceback.print_exc()
        return False, device
