import datetime
import time

import os
from bs4 import UnicodeDammit
from lxml import html

import settings
from crashes import crash_handler
from devices import adb
from util import logger

class EmmaCoverage(object):

    def __init__(self):
        pass

    def extract_coverage(path):
        with open(path, 'rb') as file:
            content = file.read()
            doc = UnicodeDammit(content, is_html=True)

        parser = html.HTMLParser(encoding=doc.original_encoding)
        root = html.document_fromstring(content, parser=parser)
        return root.xpath('/html/body/table[2]/tr[2]/td[5]/text()')[0].strip()


    # return accumulative coverage and average length
    def get_suite_coverage(test_runner, scripts, device, result_dir, apk_dir, package_name, gen, pop):
        unique_crashes = set()
        scripts_crash_status = {}

        # clean states
        adb.shell_command(device, "am force-stop " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        application_files = "/data/data/" + package_name + "/files"
        coverage_path_in_device = application_files + "/coverage.ec"
        coverage_backup_path_before_clear = "/mnt/sdcard/coverage.ec"

        adb.shell_command(device, "rm -f " + coverage_path_in_device, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(device, "rm -f " + coverage_backup_path_before_clear,
                          timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
        coverage_folder = str(gen) + "." + str(pop) + "." + ts

        os.chdir(result_dir)
        os.system("mkdir -p coverages/" + coverage_folder)
        os.system("cp coverage.em coverages/" + coverage_folder + logger.redirect_string())
        os.chdir("coverages/" + coverage_folder)

        there_is_coverage = False

        # run scripts
        for index, script in enumerate(scripts):
            result_code = adb.shell_command(device,
                                            "am instrument " + package_name + "/" + package_name + ".EmmaInstrument.EmmaInstrumentation",
                                            timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception("Unable to instrument " + package_name)

            result_code = adb.push(device, script, "/mnt/sdcard/", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception("Unable to push motifcore script " + script + " to device: " + device.name)

            script_name = script.split("/")[-1]

            test_runner.run(device, package_name, script_name)

            if crash_handler.handle(device, result_dir, script, gen, pop, index, unique_crashes):
                scripts_crash_status[script] = True
                pass
            else:
                scripts_crash_status[script] = False
                # no crash, can broadcast
                result_code = adb.shell_command(device, "am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE",
                                                timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
                if result_code != 0:
                    adb.log_evaluation_result(device, result_dir, script, False)
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
                    adb.log_evaluation_result(device, result_dir, script, False)
                    device.flag_as_malfunctioning()
                    raise Exception(
                        "Coverage broadcast was sent for script " + script + " in device: " + device.name + " but there is not file: " + coverage_path_in_device)

                # save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_path_in_device + " " + coverage_backup_path_before_clear,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to retrieve coverage.ec file after coverage broadcast for script " + script + " in  device: " + device.name)

        # close app
        result_code = adb.shell_command(device, "pm clear " + package_name,
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, script, False)
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to clear package for script " + script + " in device: " + device.name)

        # restore the coverage.ec file from /mnt/sdcard to app files
        result_code = adb.sudo_shell_command(device, "mkdir " + application_files,
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, script, False)
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to create application files directory for script " + script + " in device: " + device.name)

        if there_is_coverage:
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_backup_path_before_clear + " " + coverage_path_in_device,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, result_dir, script, False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to copy backup coverage.ec file in sdcard for script " + script + " in device: " + device.name)

        adb.log_evaluation_result(device, result_dir, script, True)

    print "### Getting EMMA coverage.ec and report ..."
    result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    if result_code != 0:
        adb.log_evaluation_result(device, result_dir, "clear-package", False)
        device.flag_as_malfunctioning()
        raise Exception("Unable to clear package " + package_name + " in device: " + device.name)

    if there_is_coverage:
        result_code = adb.pull(device, coverage_backup_path_before_clear, "coverage.ec",
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, "pull-coverage", False)
            device.flag_as_malfunctioning()
            raise Exception("Unable to pull coverage for device: " + device.name)

        os.system(
            "java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em,coverage.ec -sp " + apk_dir + "/src " + logger.redirect_string())

        html_file = result_dir + "/coverages/" + coverage_folder + "/coverage/index.html"
        coverage_str = extract_coverage(html_file)

        if coverage_str.find("%") != -1:
            return int(coverage_str.split("%")[0]), len(unique_crashes), scripts_crash_status
        else:
            adb.log_evaluation_result(device, result_dir, "html-extract-coverage", False)
            return 0, len(unique_crashes), scripts_crash_status
    else:
        return 0, len(unique_crashes), scripts_crash_status
