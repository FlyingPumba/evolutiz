import os
import random
import time
from typing import List

from deap import tools
from deap.base import Toolbox

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from test_runner.test_runner import TestRunner, TestSequence
from test_runner.test_runner_installer import TestRunnerInstaller
from util import logger


class MotifcoreTestRunner(TestRunner):

    def __init__(self, use_motifgene: bool = True) -> None:
        self.use_motifgene = use_motifgene
        self.motifcore_script_path_in_devices = "/mnt/sdcard/motifcore.script"
        self.test_runner_installer = TestRunnerInstaller("motifcore",
                                                         f"{settings.WORKING_DIR}test_runner/motifcore/motifcore",
                                                         f"{settings.WORKING_DIR}test_runner/motifcore/motifcore.jar")

    def register_minimum_api(self) -> None:
        self.minimum_api = 19
        features.provide('minimum_api', self.minimum_api)

    def register_crossover_operator(self, toolbox: Toolbox) -> None:
        # the crossover between individuals is a uniform crossover
        # that means each test case has 50-50 probability of ending up in each of the new individuals
        toolbox.register("mate", tools.cxUniform, indpb=0.5)

    def register_mutation_operator(self, toolbox: Toolbox) -> None:
        toolbox.register("mutate", self.sapienz_mut_suite, indpb=0.5)

    def install_on_devices(self):
        self.test_runner_installer.install_in_all_devices()

    def run(self, device, package_name, script_name):
        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        string_seeding_flag = ""

        if self.use_motifgene:
            string_seeding_flag = f"--string-seeding /mnt/sdcard/{package_name}_strings.xml"

        motifcore_cmd = f"motifcore -p {package_name} --ignore-crashes --ignore-security-exceptions --ignore-timeouts" \
                        f" --bugreport {string_seeding_flag} -f /mnt/sdcard/{script_name} 1"

        output, errors, result_code = adb.shell_command(device, motifcore_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print(f"Test case running finished with output:\n{output}")

        if "Exception" in errors:
            device_stacktrace = errors.split("** Error: ")[1]
            raise Exception(f"An error occurred when running test case: {device_stacktrace}")

        # need to manually kill motifcore when timeout
        adb.pkill(device, "motifcore")

        if verbose_level > 0:
            logger.log_progress(f'\nMotifcore test run took: {time.time() - start_time:.2f} seconds')

    def generate(self, device, package_name, destination_file_name) -> TestSequence:
        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        motifcore_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

        string_seeding_flag = ""

        if self.use_motifgene:
            string_seeding_flag = f"--string-seeding /mnt/sdcard/{package_name}_strings.xml"

        motifcore_cmd = f"motifcore -p {package_name} --ignore-crashes --ignore-security-exceptions --ignore-timeouts" \
                        f" --bugreport {string_seeding_flag} -v {str(motifcore_events)}"

        output, errors, result_code = adb.shell_command(device, motifcore_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print(f"Test case generation finished with output:\n{output}")

        if "Exception" in errors:
            device_stacktrace = errors.split("** Error: ")[1]
            raise Exception(f"An error occurred when generating test case: {device_stacktrace}")

        # need to manually kill motifcore when timeout
        adb.pkill(device, "motifcore")

        test_content = self.retrieve_generated_test(device, destination_file_name)

        if verbose_level > 0:
            logger.log_progress(f'\nMotifcore test generation took: {time.time() - start_time:.2f} seconds '
                                f'for {motifcore_events:d} events')

        return test_content

    def retrieve_generated_test(self, device, destination_file_name) -> List[str]:
        output, errors, result_code = adb.pull(device, self.motifcore_script_path_in_devices, destination_file_name,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            raise Exception(f"Failed to retrieve motifcore script from device: {device.name}")

        # remove motifgenes from test case if they are disabled
        if not self.use_motifgene:
            os.system(f"sed -i \'/GUIGen/d\' {destination_file_name}")

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
