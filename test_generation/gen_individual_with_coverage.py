import datetime
import sys
import time

import numpy
import os
import random
import traceback
from bs4 import UnicodeDammit
from deap import creator
from lxml import html

import settings
from crashes import crash_handler
from devices import adb
from util import logger


class CanNotInitSeqException(Exception):
    pass


def get_suite_with_fitness(test_runner, device, result_dir, apk_dir, package_name, gen, pop):
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
            adb.log_evaluation_result(device, result_dir, "am instrument", False)
            device.flag_as_malfunctioning()
            raise Exception("Unable to instrument " + package_name)

        seq, there_is_coverage = get_sequence_with_fitness(test_runner, device, result_dir, package_name, gen, pop, i,
                                                           unique_crashes)
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
                adb.log_evaluation_result(device, result_dir, "file doesnt exist", False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Coverage broadcast was sent in device: " + device.name + " but there is not file: " + coverage_path_in_device)

            # save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_path_in_device + " " + coverage_backup_path_before_clear,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, result_dir, "cp coverage to sdcard", False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to retrieve coverage.ec file after coverage broadcast in  device: " + device.name)

        # close app
        result_code = adb.shell_command(device, "pm clear " + package_name,
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, "pm clear", False)
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to clear package in device: " + device.name)

        # restore the coverage.ec file from /mnt/sdcard to app files
        result_code = adb.sudo_shell_command(device, "mkdir " + application_files,
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, "mkdir application_files", False)
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to create application files directory in device: " + device.name)

        if there_is_coverage_in_suite:
            result_code = adb.sudo_shell_command(device,
                                                 "cp -p " + coverage_backup_path_before_clear + " " + coverage_path_in_device,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, result_dir, "cp coverage from sdcard", False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to copy backup coverage.ec file in sdcard in device: " + device.name)

        adb.log_evaluation_result(device, result_dir, "success", True)

    print "### Getting EMMA coverage.ec and report ..."
    result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    if result_code != 0:
        adb.log_evaluation_result(device, result_dir, "clear-package", False)
        device.flag_as_malfunctioning()
        raise Exception("Unable to clear package " + package_name + " in device: " + device.name)

    crashes = len(unique_crashes)
    length = sys.maxint
    if len(lengths) > 0:
        length = numpy.mean(lengths)
    coverage = 0

    if there_is_coverage_in_suite:
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
            coverage = int(coverage_str.split("%")[0])
        else:
            adb.log_evaluation_result(device, result_dir, "html-extract-coverage", False)

        return ret, (coverage, length, crashes)
    else:
        return ret, (0, length, crashes)


def get_sequence_with_fitness(test_runner, device, result_dir, package_name, gen, pop, index, unique_crashes):
    start_time = datetime.datetime.now()
    random.seed()

    # access the generated script, should ignore the first launch activity
    script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
    ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

    motifcore_script_filename = result_dir + "/intermediate/" + script_name + ".init." + str(gen) + "." + str(
        pop) + "." + ts

    ret = test_runner.generate(device, package_name, motifcore_script_filename)

    there_is_coverage = False

    if crash_handler.handle(device, result_dir, motifcore_script_filename, gen, pop, index, unique_crashes):
        pass
    else:
        # no crash, can broadcast
        result_code = adb.shell_command(device, "am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE",
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, motifcore_script_filename, False)
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to broadcast coverage gathering for script " + motifcore_script_filename + " in device: " + device.name)
        there_is_coverage = True

    # logger.log_progress("\nget_sequence took " + str((datetime.datetime.now() - start_time).seconds))

    adb.log_evaluation_result(device, result_dir, motifcore_script_filename, True)

    return ret, there_is_coverage


def gen_individual_with_coverage(test_runner, device, result_dir, apk_dir, package_name, gen, pop):
    try:
        suite, fitness = get_suite_with_fitness(test_runner, device, result_dir, apk_dir, package_name, gen, pop)
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