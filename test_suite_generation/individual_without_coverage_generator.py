import datetime
import random
import traceback

from deap import creator

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb


class IndividualWithoutCoverageGenerator(object):

    def __init__(self):
        self.test_runner = RequiredFeature('test_runner').request()
        self.package_name = None

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
        result_code = adb.shell_command(device, "pm clear " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT, retry=5)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Failed to clear package " + self.package_name + " in device: " + device.name)

        # access the generated script, should ignore the first launch activity
        script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

        motifcore_script_filename = self.result_dir + "/intermediate/" + script_name + ".init." + ts + "." + str(index)

        ret = self.test_runner.generate(device, self.package_name, motifcore_script_filename)

        return ret

    def gen_individual(self, device, individual_index, gen):
        try:
            self.result_dir = RequiredFeature('result_dir').request()
            self.package_name = RequiredFeature('package_name').request()

            device.mark_work_start()

            suite = self.get_suite(device)

            device.mark_work_stop()

            individual = creator.Individual(suite)
            individual.index_in_generation = individual_index
            individual.generation = gen

            return individual

        except Exception as e:
            print(e)
            traceback.print_exc()
            return None
