import os
import random

import logger
import settings
from devices import adb
from test_runner import TestRunner

class MotifcoreRunner(TestRunner):

    def __init__(self, device_manager, use_motifgene=False):
        self.use_motifgene = use_motifgene

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
            adb.reboot(device)
            raise Exception("Failed to retrieve motifcore script from device: " + adb.get_device_name(device))

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