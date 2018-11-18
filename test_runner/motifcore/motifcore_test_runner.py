import os
import random
import time

from deap import tools

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from test_runner.test_runner import TestRunner
from test_runner.test_runner_installer import TestRunnerInstaller
from util import logger


class MotifcoreTestRunner(TestRunner):

    def __init__(self, use_motifgene=True):
        self.use_motifgene = use_motifgene
        self.motifcore_script_path_in_devices = "/mnt/sdcard/motifcore.script"
        self.test_runner_installer = TestRunnerInstaller("motifcore",
                                                         settings.WORKING_DIR + "test_runner/motifcore/motifcore",
                                                         settings.WORKING_DIR + "test_runner/motifcore/motifcore.jar")

    def register_minimum_api(self):
        self.minimum_api = 19
        features.provide('minimum_api', self.minimum_api)

    def register_crossover_operator(self, toolbox):
        # the crossover between individuals is a uniform crossover
        # that means each test case has 50-50 probability of ending up in each of the new individuals
        toolbox.register("mate", tools.cxUniform, indpb=0.5)

    def register_mutation_operator(self, toolbox):
        toolbox.register("mutate", self.sapienz_mut_suite, indpb=0.5)

    def install_on_devices(self):
        self.test_runner_installer.install_in_all_devices()

    def run(self, device, package_name, script_name):
        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        string_seeding_flag = ""

        if self.use_motifgene:
            string_seeding_flag = "--string-seeding /mnt/sdcard/" + package_name + "_strings.xml"

        motifcore_cmd = "motifcore -p " + package_name \
                        + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                        + string_seeding_flag + " -f /mnt/sdcard/" + script_name + " 1"

        output, errors, result_code = adb.shell_command(device, motifcore_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print("Test case running finished with output:\n" + output)

        if "Exception" in errors:
            device_stacktrace = errors.split("** Error: ")[1]
            raise Exception("An error occurred when running test case: " + device_stacktrace)

        # need to manually kill motifcore when timeout
        adb.pkill(device, "motifcore")

        if verbose_level > 0:
            logger.log_progress('\nMotifcore test run took: %.2f seconds' % (time.time() - start_time))

    def generate(self, device, package_name, destination_file_name):
        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        motifcore_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

        string_seeding_flag = ""

        if self.use_motifgene:
            string_seeding_flag = "--string-seeding /mnt/sdcard/" + package_name + "_strings.xml"

        motifcore_cmd = "motifcore -p " + package_name \
                        + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                        + string_seeding_flag + " -v " + str(motifcore_events)

        output, errors, result_code = adb.shell_command(device, motifcore_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print("Test case generation finished with output:\n" + output)

        if "Exception" in errors:
            device_stacktrace = errors.split("** Error: ")[1]
            raise Exception("An error occurred when generating test case: " + device_stacktrace)

        # need to manually kill motifcore when timeout
        adb.pkill(device, "motifcore")

        test_content = self.retrieve_generated_test(device, destination_file_name)

        if verbose_level > 0:
            logger.log_progress('\nMotifcore test generation took: %.2f seconds for %d events' % (time.time() - start_time, motifcore_events))

        return test_content

    def retrieve_generated_test(self, device, destination_file_name):
        output, errors, result_code = adb.pull(device, self.motifcore_script_path_in_devices, destination_file_name,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            raise Exception("Failed to retrieve motifcore script from device: " + device.name)

        # remove motifgenes from test case if they are disabled
        if not self.use_motifgene:
            os.system("sed -i '/GUIGen/d' " + destination_file_name)

        return self.get_test_case_content_from_file(destination_file_name)

    def sapienz_mut_suite(self, individual, indpb):
        # shuffle seq
        individual, = tools.mutShuffleIndexes(individual, indpb)

        # crossover inside the suite
        # perform one point crossover between odd and even pair positions of test cases
        for i in range(1, len(individual), 2):
            if random.random() < settings.MUTPB:
                if len(individual[i - 1]) <= 2:
                    continue
                if len(individual[i]) <= 2:
                    continue

                individual[i - 1], individual[i] = tools.cxOnePoint(individual[i - 1], individual[i])

        # shuffle events inside each test case
        for i in range(len(individual)):
            if random.random() < settings.MUTPB:
                if len(individual[i]) <= 2:
                    continue
                individual[i], = tools.mutShuffleIndexes(individual[i], indpb)

        return individual,
