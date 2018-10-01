import datetime
import os
import random
import sys
import time
import traceback

import numpy
from deap import creator

import settings
from coverage.coverage_extractor import extract_coverage
from crashes import crash_handler
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from util import logger


class IndividualWithCoverageGenerator(object):

    def __init__(self):
        self.test_runner = RequiredFeature('test_runner').request()

    def get_suite_with_fitness(self, device, gen, pop):
        ret = []
        unique_crashes = set()

        # clean states
        adb.shell_command(device, "am force-stop " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(device, "pm clear " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # delete previous coverage files
        application_files = "/data/data/" + self.package_name + "/files"
        coverage_path_in_device = application_files + "/coverage.ec"
        coverage_backup_path_before_clear = "/mnt/sdcard/coverage.ec"

        adb.shell_command(device, "rm " + coverage_path_in_device, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(device, "rm " + coverage_backup_path_before_clear,
                          timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # create folder to store coverage of suite
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
        coverage_folder = str(gen) + "." + str(pop) + "." + ts

        os.chdir(self.result_dir)
        os.system("mkdir -p coverages/" + coverage_folder)
        os.system("cp coverage.em coverages/" + coverage_folder + logger.redirect_string())
        os.chdir("coverages/" + coverage_folder)

        lengths = []
        there_is_coverage_in_suite = False

        for i in range(0, settings.SUITE_SIZE):

            output, errors, result_code = adb.shell_command(device,
                                            "am instrument " + self.package_name + "/" + self.package_name + ".EmmaInstrument.EmmaInstrumentation",
                                            timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, "am instrument", False)
                device.flag_as_malfunctioning()
                raise Exception("Unable to instrument " + self.package_name)

            seq, there_is_coverage = self.get_sequence_with_fitness(device, gen, pop, i, unique_crashes)
            there_is_coverage_in_suite = there_is_coverage or there_is_coverage_in_suite

            ret.append(seq)

            if not there_is_coverage:
                lengths.append(len(seq))
            else:
                tries = 0
                max_tries = 10
                found_coverage_file = False
                while tries < max_tries:
                    if not adb.exists_file(device, coverage_path_in_device):
                        time.sleep(15)
                        tries += 1
                    else:
                        found_coverage_file = True
                        break

                if not found_coverage_file:
                    adb.log_evaluation_result(device, self.result_dir, "file doesnt exist", False)
                    device.flag_as_malfunctioning()
                    raise Exception(
                        "Coverage broadcast was sent in device: " + device.name + " but there is not file: " + coverage_path_in_device)

                # save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
                output, errors, result_code = adb.sudo_shell_command(device,
                                                     "cp -p " + coverage_path_in_device + " " + coverage_backup_path_before_clear,
                                                     timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
                if result_code != 0:
                    adb.log_evaluation_result(device, self.result_dir, "cp coverage to sdcard", False)
                    device.flag_as_malfunctioning()
                    raise Exception(
                        "Unable to retrieve coverage.ec file after coverage broadcast in  device: " + device.name)

            # close app
            output, errors, result_code = adb.shell_command(device, "pm clear " + self.package_name,
                                            timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, "pm clear", False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to clear package in device: " + device.name)

            # restore the coverage.ec file from /mnt/sdcard to app files
            output, errors, result_code = adb.sudo_shell_command(device, "mkdir " + application_files,
                                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, "mkdir application_files", False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to create application files directory in device: " + device.name)

            if there_is_coverage_in_suite:
                output, errors, result_code = adb.sudo_shell_command(device,
                                                     "cp -p " + coverage_backup_path_before_clear + " " + coverage_path_in_device,
                                                     timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
                if result_code != 0:
                    adb.log_evaluation_result(device, self.result_dir, "cp coverage from sdcard", False)
                    device.flag_as_malfunctioning()
                    raise Exception(
                        "Unable to copy backup coverage.ec file in sdcard in device: " + device.name)

            adb.log_evaluation_result(device, self.result_dir, "success", True)

        print("### Getting EMMA coverage.ec and report ...")
        output, errors, result_code = adb.shell_command(device, "pm clear " + self.package_name,
                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, "clear-package", False)
            device.flag_as_malfunctioning()
            raise Exception("Unable to clear package " + self.package_name + " in device: " + device.name)

        crashes = len(unique_crashes)
        length = sys.maxsize
        if len(lengths) > 0:
            length = numpy.mean(lengths)
        coverage = 0

        if there_is_coverage_in_suite:
            output, errors, result_code = adb.pull(device, coverage_backup_path_before_clear, "coverage.ec",
                                   timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, "pull-coverage", False)
                device.flag_as_malfunctioning()
                raise Exception("Unable to pull coverage for device: " + device.name)

            os.system(
                "java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em,coverage.ec -sp " + self.app_path + "/src " + logger.redirect_string())

            html_file = self.result_dir + "/coverages/" + coverage_folder + "/coverage/index.html"
            coverage_str = extract_coverage(html_file)

            if coverage_str.find("%") != -1:
                coverage = int(coverage_str.split("%")[0])
            else:
                adb.log_evaluation_result(device, self.result_dir, "html-extract-coverage", False)

            return ret, (coverage, length, crashes)
        else:
            return ret, (0, length, crashes)

    def get_sequence_with_fitness(self, device, gen, pop, index, unique_crashes):
        start_time = datetime.datetime.now()
        random.seed()

        # access the generated script, should ignore the first launch activity
        script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

        motifcore_script_filename = self.result_dir + "/intermediate/" + script_name + ".init." + str(gen) + "." + str(
            pop) + "." + ts

        ret = self.test_runner.generate(device, self.package_name, motifcore_script_filename)

        there_is_coverage = False

        if crash_handler.handle(device, self.result_dir, motifcore_script_filename, gen, pop, index, unique_crashes):
            pass
        else:
            # no crash, can broadcast
            output, errors, result_code = adb.shell_command(device, "am broadcast -a evolutiz.emma.COLLECT_COVERAGE",
                                            timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            if result_code != 0:
                adb.log_evaluation_result(device, self.result_dir, motifcore_script_filename, False)
                device.flag_as_malfunctioning()
                raise Exception(
                    "Unable to broadcast coverage gathering for script " + motifcore_script_filename + " in device: " + device.name)
            there_is_coverage = True

        # logger.log_progress("\nget_sequence took " + str((datetime.datetime.now() - start_time).seconds))

        adb.log_evaluation_result(device, self.result_dir, motifcore_script_filename, True)

        return ret, there_is_coverage

    def gen_individual(self, device, individual_index, gen):
        self.result_dir = RequiredFeature('result_dir').request()
        self.app_path = RequiredFeature('app_path').request()
        self.package_name = RequiredFeature('package_name').request()

        device.mark_work_start()

        suite, fitness = self.get_suite_with_fitness(device, gen, individual_index)

        device.mark_work_stop()

        individual = creator.Individual(suite)
        individual.fitness.values = fitness
        individual.index_in_generation = individual_index
        individual.generation = gen

        # logger.log_fitness_result(fitness)

        return individual
