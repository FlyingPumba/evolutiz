import os
import random

from deap import tools

import settings
from devices import adb
from test_runner.motifcore.mut_suite import sapienz_mut_suite
from test_runner.test_runner_installer import TestRunnerInstaller
from test_runner.test_runner import TestRunner


class MotifcoreTestRunner(TestRunner):

    def __init__(self, use_motifgene=False):
        self.use_motifgene = use_motifgene

        self.test_runner_installer = TestRunnerInstaller("motifcore",
                                                         settings.WORKING_DIR + "test_runner/motifcore/motifcore",
                                                         settings.WORKING_DIR + "test_runner/motifcore/motifcore.jar")

    def register_crossover_operator(self, toolbox):
        toolbox.register("mate", tools.cxUniform, indpb=0.5)

    def register_mutation_operator(self, toolbox):
        toolbox.register("mutate", sapienz_mut_suite, indpb=0.5)

    def install_on_devices(self):
        self.test_runner_installer.install_in_all_devices()

    def run(self, device, package_name, script_name):
        self.prepare_device_for_run(device)

        string_seeding_flag = ""

        if self.use_motifgene:
            string_seeding_flag = "--string-seeding /mnt/sdcard/" + package_name + "_strings.xml"

        motifcore_cmd = "motifcore -p " + package_name \
                        + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                        + string_seeding_flag + " -f /mnt/sdcard/" + script_name + " 1"

        adb.sudo_shell_command(device, motifcore_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

        # need to manually kill motifcore when timeout
        adb.pkill(device, "motifcore")

    def generate(self, device, package_name, destination_file_name):
        self.prepare_device_for_run(device)

        motifcore_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

        string_seeding_flag = ""

        if self.use_motifgene:
            string_seeding_flag = "--string-seeding /mnt/sdcard/" + package_name + "_strings.xml"

        motifcore_cmd = "motifcore -p " + package_name \
                        + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                        + string_seeding_flag + " -v " + str(motifcore_events)

        adb.shell_command(device, motifcore_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

        # need to manually kill motifcore when timeout
        adb.pkill(device, "motifcore")

        return self.retrieve_generated_test(device, destination_file_name)

    def retrieve_generated_test(self, device, destination_file_name):
        result_code = adb.pull(device, settings.MOTIFCORE_SCRIPT_PATH, destination_file_name,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Failed to retrieve motifcore script from device: " + device.name)

        # remove motifgenes from test case if they are disabled
        if not self.use_motifgene:
            os.system("sed -i '/GUIGen/d' " + destination_file_name)

        test_content = []

        script = open(destination_file_name)
        is_content = False
        is_skipped_first = False
        for line in script:
            line = line.strip()
            if line.find("start data >>") != -1:
                is_content = True
                continue
            if is_content and line != "":
                if not is_skipped_first:
                    is_skipped_first = True
                    continue
                if is_skipped_first:
                    test_content.append(line)

        script.close()
        return test_content
