import datetime
import os
import subprocess
import time

import settings
from coverage.coverage_extractor import extract_coverage
from crashes import crash_handler
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from util import logger


class EmmaCoverage(object):

    def __init__(self):
        self.test_runner = RequiredFeature('test_runner').request()
        self.app_path = RequiredFeature('app_path').request()


    def get_suite_coverage(self, scripts, device, gen, pop):
        self.result_dir = RequiredFeature('result_dir').request()
        self.package_name = RequiredFeature('package_name').request()

        unique_crashes = set()
        scripts_crash_status = {}

        # clean states
        adb.shell_command(device, "am force-stop " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(device, "pm clear " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        application_files = "/data/data/" + self.package_name + "/files"
        coverage_path_in_device = application_files + "/coverage.ec"
        coverage_backup_path_before_clear = "/mnt/sdcard/coverage.ec"

        adb.shell_command(device, "rm -f " + coverage_path_in_device, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(device, "rm -f " + coverage_backup_path_before_clear,
                          timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
        coverage_folder = str(gen) + "." + str(pop) + "." + ts

        os.chdir(self.result_dir)
        os.system("mkdir -p coverages/" + coverage_folder)
        os.system("cp coverage.em coverages/" + coverage_folder + logger.redirect_string())
        os.chdir("coverages/" + coverage_folder)

        there_is_coverage = False

        # run scripts
        for index, script in enumerate(scripts):
            result_code = adb.shell_command(device,
                                            "am instrument " + self.package_name + "/" + self.package_name + ".EmmaInstrument.EmmaInstrumentation",
                                            timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception("Unable to instrument " + self.package_name)

            result_code = adb.push(device, script, "/mnt/sdcard/", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception("Unable to push motifcore script " + script + " to device: " + device.name)

            script_name = script.split("/")[-1]

            self.test_runner.run(device, self.package_name, script_name)

            if crash_handler.handle(device, self.result_dir, script, gen, pop, index, unique_crashes):
                scripts_crash_status[script] = True
                pass
            else:
                scripts_crash_status[script] = False
                # no crash, can broadcast
                result_code = adb.shell_command(device, "am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE",
                                                timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
                if result_code != 0:
                    adb.log_evaluation_result(device, self.result_dir, script, False)
                    device.flag_as_malfunctioning()
                    raise Exception(
                        "Unable to broadcast coverage gathering for script " + script + " in device: " + device.name)
                there_is_coverage = True

                tries = 0
                max_tries = 10
                found_coverage_file = False
                while tries < max_tries:
                    if not adb.exists_file(device, coverage_path_in_device, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT):
                        time.sleep(15)
                        tries += 1
                    else:
                        found_coverage_file = True
                        break

                if not found_coverage_file:
                    adb.log_evaluation_result(device, self.result_dir, script, False)
                    device.flag_as_malfunctioning()
                    raise Exception(
                        "Coverage broadcast was sent for script " + script + " in device: " + device.name +
                        " but there is not file: " + coverage_path_in_device)

            # save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_path_in_device + " " + coverage_backup_path_before_clear,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to retrieve coverage.ec file after coverage broadcast for script " + script + " in  device: " + device.name)

        # close app
        result_code = adb.shell_command(device, "pm clear " + self.package_name,
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, script, False)
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to clear package for script " + script + " in device: " + device.name)

        # restore the coverage.ec file from /mnt/sdcard to app files
        result_code = adb.sudo_shell_command(device, "mkdir " + application_files,
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, script, False)
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to create application files directory for script " + script + " in device: " + device.name)

        if there_is_coverage:
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_backup_path_before_clear + " " + coverage_path_in_device,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to copy backup coverage.ec file in sdcard for script " + script + " in device: " + device.name)

        adb.log_evaluation_result(device, self.result_dir, script, True)

        print "### Getting EMMA coverage.ec and report ..."
        result_code = adb.shell_command(device, "pm clear " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, "clear-package", False)
            device.flag_as_malfunctioning()
            raise Exception("Unable to clear package " + self.package_name + " in device: " + device.name)

        if there_is_coverage:
            result_code = adb.pull(device, coverage_backup_path_before_clear, "coverage.ec",
                                   timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, "pull-coverage", False)
                device.flag_as_malfunctioning()
                raise Exception("Unable to pull coverage for device: " + device.name)

            emma_cmd = "java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em,coverage.ec -sp " + self.app_path + "/src "
            output, errors = subprocess.Popen(emma_cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()

            # logger.log_progress("Emma jar finished.\nOutput:\n" + output + ".\nErrors:\n" + errors + "\n")

            html_file = "coverage/index.html"
            coverage_str = extract_coverage(html_file)

            if coverage_str.find("%") != -1:
                return int(coverage_str.split("%")[0]), len(unique_crashes), scripts_crash_status
            else:
                adb.log_evaluation_result(device, self.result_dir, "html-extract-coverage", False)
                return 0, len(unique_crashes), scripts_crash_status
        else:
            return 0, len(unique_crashes), scripts_crash_status
