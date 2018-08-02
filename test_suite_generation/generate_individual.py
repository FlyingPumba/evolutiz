import datetime

import random
import traceback
from deap import creator

import settings
from crashes import crash_handler
from devices import adb

class IndividualGenerator(object):

    def __init__(self, test_runner, result_dir, apk_dir, package_name):

        self.test_runner = test_runner
        self.result_dir = result_dir
        self.apk_dir = apk_dir
        self.package_name = package_name

    def get_suite(self, device):
        ret = []
        unique_crashes = set()

        for i in range(0, settings.SUITE_SIZE):
            seq = self.get_sequence(device, i, unique_crashes)
            ret.append(seq)

        return ret


    def get_sequence(self, device, index, unique_crashes):
        random.seed()

        # clear data
        result_code = adb.shell_command(device, "pm clear " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Failed to clear package " + self.package_name + " in device: " + device.name)

        # access the generated script, should ignore the first launch activity
        script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

        motifcore_script_filename = self.result_dir + "/intermediate/" + script_name + ".init." + ts + "." + str(index)

        ret = self.test_runner.generate(device, self.package_name, motifcore_script_filename)

        # deal with crash
        crash_handler.handle(device, self.result_dir, motifcore_script_filename, "init", ts, index, unique_crashes)

        return ret


    def gen_individual(self, device):
        try:
            suite = self.get_suite(device)
            return creator.Individual(suite), device

        except Exception as e:
            print e
            traceback.print_exc()
            return False, device
