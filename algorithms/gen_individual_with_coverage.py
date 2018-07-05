import datetime
import os
import sys
import time
import random
import traceback

import numpy
from deap import creator

import logger
import settings
from crashes import crash_handler
from devices import adb

from lxml import html
from bs4 import UnicodeDammit

class CanNotInitSeqException(Exception):
    pass

def get_suite_with_fitness(use_motifgene, device, result_dir, apk_dir, package_name, gen, pop):
    ret = []
    unique_crashes = set()

    # clean states
    adb.shell_command(device, "am force-stop " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    # delete previous coverage files
    application_files = "/data/data/" + package_name + "/files"
    coverage_path_in_device = application_files + "/coverage.ec"
    coverage_backup_path_before_clear = "/mnt/sdcard/coverage.ec"

    adb.shell_command(device, "rm " + coverage_path_in_device, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    adb.shell_command(device, "rm " + coverage_backup_path_before_clear, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    # create folder to store coverage of suite
    ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
    coverage_folder = str(gen) + "." + str(pop) + "." + ts

    os.chdir(result_dir)
    os.system("mkdir -p coverages/" + coverage_folder)
    os.system("cp coverage.em coverages/" + coverage_folder + logger.redirect_string())
    os.chdir("coverages/" + coverage_folder)

    lengths = []
    there_is_coverage_in_suite = False

    for i in range(0, settings.SUITE_SIZE):

        result_code = adb.shell_command(device,
                                        "am instrument " + package_name + "/" + package_name + ".EmmaInstrument.EmmaInstrumentation",
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_evaluation_result(device, result_dir, "am instrument", False)
            adb.reboot(device)
            raise Exception("Unable to instrument " + package_name)

        seq, there_is_coverage = get_sequence(use_motifgene, device, result_dir, package_name, gen, pop, i, unique_crashes)
        there_is_coverage_in_suite = there_is_coverage or there_is_coverage_in_suite

        ret.append(seq)

        if not there_is_coverage:
            lengths.append(len(seq))
        else:
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
                logger.log_evaluation_result(device, result_dir, "file doesnt exist", False)
                adb.reboot(device)
                raise Exception(
                    "Coverage broadcast was sent in device: " + adb.get_device_name(
                        device) + " but there is not file: " + coverage_path_in_device)

            # save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_path_in_device + " " + coverage_backup_path_before_clear,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                logger.log_evaluation_result(device, result_dir, "cp coverage to sdcard", False)
                adb.reboot(device)
                raise Exception(
                    "Unable to retrieve coverage.ec file after coverage broadcast in  device: " + adb.get_device_name(
                        device))

        # close app
        result_code = adb.shell_command(device, "pm clear " + package_name,
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_evaluation_result(device, result_dir, "pm clear", False)
            adb.reboot(device)
            raise Exception(
                "Unable to clear package in device: " + adb.get_device_name(device))

        # restore the coverage.ec file from /mnt/sdcard to app files
        result_code = adb.sudo_shell_command(device, "mkdir " + application_files,
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_evaluation_result(device, result_dir, "mkdir application_files", False)
            adb.reboot(device)
            raise Exception(
                "Unable to create application files directory in device: " + adb.get_device_name(
                    device))

        if there_is_coverage_in_suite:
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_backup_path_before_clear + " " + coverage_path_in_device,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                logger.log_evaluation_result(device, result_dir, "cp coverage from sdcard", False)
                adb.reboot(device)
                raise Exception(
                    "Unable to copy backup coverage.ec file in sdcard in device: " + adb.get_device_name(
                        device))

        logger.log_evaluation_result(device, result_dir, "success", True)

    print "### Getting EMMA coverage.ec and report ..."
    result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    if result_code != 0:
        logger.log_evaluation_result(device, result_dir, "clear-package", False)
        adb.reboot(device)
        raise Exception("Unable to clear package " + package_name + " in device: " + adb.get_device_name(device))

    crashes = len(unique_crashes)
    length = sys.maxint
    if len(lengths) > 0:
        length = numpy.mean(lengths)
    coverage = 0

    if there_is_coverage_in_suite:
        result_code = adb.pull(device, coverage_backup_path_before_clear, "coverage.ec",
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_evaluation_result(device, result_dir, "pull-coverage", False)
            adb.reboot(device)
            raise Exception("Unable to pull coverage for device: " + adb.get_device_name(device))

        os.system("java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em,coverage.ec -sp " + apk_dir + "/src " + logger.redirect_string())

        html_file = result_dir + "/coverages/" + coverage_folder + "/coverage/index.html"
        coverage_str = extract_coverage(html_file)

        if coverage_str.find("%") != -1:
            coverage = int(coverage_str.split("%")[0])
        else:
            logger.log_evaluation_result(device, result_dir, "html-extract-coverage", False)

        return ret, (coverage, length, crashes)
    else:
        return ret, (0, length, crashes)


def get_sequence(use_motifgene, device, result_dir, package_name, gen, pop, index, unique_crashes):
    start_time = datetime.datetime.now()

    random.seed()

    motifcore_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

    ret = []

    adb.set_bluetooth_state(device, True, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    adb.set_wifi_state(device, True, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    adb.set_location_state(device, True, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    # start motifcore
    string_seeding_flag = ""

    if use_motifgene:
        string_seeding_flag = "--string-seeding /mnt/sdcard/" + package_name + "_strings.xml"

    motifcore_cmd = "motifcore -p " + package_name + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " + string_seeding_flag + " -v " + str(
        motifcore_events)
    adb.shell_command(device, motifcore_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

    # access the generated script, should ignore the first launch activity
    script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
    ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

    motifcore_script_filename = result_dir + "/intermediate/" + script_name + ".init." + str(gen) + "." + str(pop) + "." + ts

    # need to manually kill motifcore when timeout
    adb.pkill(device, "motifcore")

    result_code = adb.pull(device, settings.MOTIFCORE_SCRIPT_PATH, motifcore_script_filename,
                           timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    if result_code != 0:
        logger.log_evaluation_result(device, result_dir, motifcore_script_filename, False)
        adb.reboot(device)
        raise Exception("Failed to retrieve motifcore script from device: " + adb.get_device_name(device))

    # remove motifgenes from test case if they are disabled
    if not use_motifgene:
        os.system("sed -i '/GUIGen/d' " + motifcore_script_filename)

    script = open(motifcore_script_filename)
    is_content = False
    is_skipped_first = False
    for line in script:
        line = line.strip()
        if line.find("start data >>") != -1:
            is_content = True
            continue
        if is_content and line != "":
            if is_skipped_first == False:
                is_skipped_first = True
                continue
            if is_skipped_first:
                ret.append(line)

    script.close()

    there_is_coverage = False

    if crash_handler.handle(device, result_dir, motifcore_script_filename, gen, pop, index, unique_crashes):
        pass
    else:
        # no crash, can broadcast
        result_code = adb.shell_command(device, "am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE",
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_evaluation_result(device, result_dir, motifcore_script_filename, False)
            adb.reboot(device)
            raise Exception(
                "Unable to broadcast coverage gathering for script " + motifcore_script_filename + " in device: " + adb.get_device_name(
                    device))
        there_is_coverage = True

    # logger.log_progress("\nget_sequence took " + str((datetime.datetime.now() - start_time).seconds))

    logger.log_evaluation_result(device, result_dir, motifcore_script_filename, True)

    return ret, there_is_coverage


def gen_individual_with_coverage(use_motifgene, device, result_dir, apk_dir, package_name, gen, pop):
    try:
        suite, fitness = get_suite_with_fitness(use_motifgene, device, result_dir, apk_dir, package_name, gen, pop)
        ind = creator.Individual(suite)
        ind.fitness.values = fitness
        logger.log_fitness_result(fitness)

        return ind, device

    except Exception as e:
        print e
        traceback.print_exc()
        return False, device

def extract_coverage(path):
    with open(path, 'rb') as file:
        content = file.read()
        doc = UnicodeDammit(content, is_html=True)

    parser = html.HTMLParser(encoding=doc.original_encoding)
    root = html.document_fromstring(content, parser=parser)
    return root.xpath('/html/body/table[2]/tr[2]/td[5]/text()')[0].strip()