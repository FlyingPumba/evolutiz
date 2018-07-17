import random

import settings
from devices import adb
from test_runner.test_runner import TestRunner

class EvolutizRunner(TestRunner):

    def __init__(self):
        self.EVOLUTIZ_SCRIPT_PATH_IN_DEVICE = "/mnt/sdcard/evolutiz.script"
        pass

    def run(self, device, package_name, script_name):
        self.prepare_device_for_run(device)

        evolutiz_cmd = "evolutiz-test-runner -p " + package_name \
                        + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                        + " -f /mnt/sdcard/" + script_name + " 1"

        adb.sudo_shell_command(device, evolutiz_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

    def generate(self, device, package_name, destination_file_name):
        self.prepare_device_for_run(device)

        evolutiz_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

        evolutiz_cmd = "evolutiz -p " + package_name \
                        + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                        + " -o " + self.EVOLUTIZ_SCRIPT_PATH_IN_DEVICE \
                        + " -v " + str(evolutiz_events)

        adb.shell_command(device, evolutiz_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

        return self.retrieve_generated_test(device, destination_file_name)

    def retrieve_generated_test(self, device, destination_file_name):
        result_code = adb.pull(device, self.EVOLUTIZ_SCRIPT_PATH_IN_DEVICE, destination_file_name,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.reboot(device)
            raise Exception("Failed to retrieve evolutiz script from device: " + adb.get_device_name(device))

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

    def generate_ga_offspring(self, parentFilename1, parentFilename2, offspringFilename1, offspringFilename2):
        # TODO
        pass