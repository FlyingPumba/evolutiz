# coding=utf-8
import os
import random
import time
from typing import List, Tuple

from deap import tools
from deap.base import Toolbox

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from generation.individual import Individual
from test_runner.motifcore.motifcore_action import MotifcoreAction
from test_runner.test_event import TestCase, TestEvent
from test_runner.test_runner import TestRunner
from test_runner.test_runner_installer import TestRunnerInstaller
from util import logger


class EvolutizAtomicTestRunner(TestRunner):

    def __init__(self, use_motifgene: bool = True) -> None:
        super().__init__()
        self.use_motifgene = use_motifgene
        self.evolutiz_script_path_in_devices = "/mnt/sdcard/evolutiz.script"
        self.throttle = 500
        self.test_runner_installer = TestRunnerInstaller("evolutiz",
                                                         f"{settings.WORKING_DIR}test_runner/evolutiz/evolutiz",
                                                         f"{settings.WORKING_DIR}test_runner/evolutiz/evolutiz.jar")

    def register_minimum_api(self) -> None:
        self.minimum_api = 28
        features.provide('minimum_api', self.minimum_api)

    def register_crossover_operator(self, toolbox: Toolbox) -> None:
        # the crossover between individuals is a uniform crossover
        # that means each test case has 50-50 probability of ending up in each of the new individuals
        toolbox.register("mate", tools.cxUniform, indpb=0.5)

    def register_mutation_operator(self, toolbox: Toolbox) -> None:
        toolbox.register("mutate", self.sapienz_mut_suite, indpb=0.5)

    def install_on_devices(self) -> None:
        self.test_runner_installer.install_in_all_devices()

    def run(self, device: Device, package_name: str, script_name: str) -> None:
        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        string_seeding_flag = ""

        # if self.use_motifgene:
        #     string_seeding_flag = f"--string-seeding /mnt/sdcard/{package_name}_strings.xml"

        evolutiz_cmd = f"evolutiz -p {package_name} --ignore-crashes --ignore-security-exceptions --ignore-timeouts" \
                        f" --throttle {self.throttle}" \
                        f" --bugreport {string_seeding_flag} -f /mnt/sdcard/{script_name} 1"

        output, errors, result_code = adb.shell_command(device, evolutiz_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print(f"Test case running finished with output:\n{output}")

        if "Exception" in errors:
            device_stacktrace = errors.split("** Error: ")[1]
            raise Exception(f"An error occurred when running test case: {device_stacktrace}")

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

        if verbose_level > 0:
            logger.log_progress(f'\nEvolutiz test run took: {time.time() - start_time:.2f} seconds')

    def generate(self, device: Device, package_name: str, destination_file_name: str) -> TestCase:
        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        evolutiz_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

        string_seeding_flag = ""

        # String seeding is NOT supported yet
        # if self.use_motifgene:
        #     string_seeding_flag = f"--string-seeding /mnt/sdcard/{package_name}_strings.xml"

        evolutiz_cmd = f"evolutiz -p {package_name} --ignore-crashes --ignore-security-exceptions --ignore-timeouts" \
                       f" --bugreport {string_seeding_flag} -o {self.evolutiz_script_path_in_devices}" \
                       f" --throttle {self.throttle} -v {str(evolutiz_events)}"

        output, errors, result_code = adb.shell_command(device, evolutiz_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print(f"Test case generation finished with output:\n{output}")

        if "Exception" in errors:
            if "** Error: " in errors:
                device_stacktrace = errors.split("** Error: ")[1]
                raise Exception(f"An error occurred when generating test case: {device_stacktrace}")
            else:
                raise Exception(f"An error occurred when generating test case: {errors}")

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

        time.sleep(5)

        test_case: TestCase = self.retrieve_generated_test(device, destination_file_name)

        if verbose_level > 0:
            logger.log_progress(f'\nEvolutiz test generation took: {time.time() - start_time:.2f} seconds '
                                f'for {evolutiz_events:d} events')

        return test_case

    def retrieve_generated_test(self, device: Device, destination_file_name: str) -> TestCase:
        output, errors, result_code = adb.pull(device, self.evolutiz_script_path_in_devices, destination_file_name,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            raise Exception(f"Failed to retrieve evolutiz script from device: {device.name}")

        # remove motifgenes from test case if they are disabled
        if not self.use_motifgene:
            os.system(f"sed -i \'/GUIGen/d\' {destination_file_name}")

        return self.get_test_case_from_file(destination_file_name)

    def sapienz_mut_suite(self, individual: Individual, indpb: float) -> Tuple[Individual]:
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

    def write_test_case_to_file(self, test_case: TestCase, filename: str) -> None:
        with open(filename, "w") as script:
            script.write(settings.SCRIPT_HEADER)
            script.write("\n".join(map(lambda t: str(t), test_case)))
            script.write("\n")

    def get_test_case_from_file(self, filename: str) -> TestCase:
        test_case = []

        with open(filename) as script:
            lines = script.read().split('\n')

        is_content = False
        is_skipped_first = False
        for line in lines:
            line = line.strip()
            if line.find("start data >>") != -1:
                is_content = True
                continue
            if is_content and line != "":
                if not is_skipped_first:
                    is_skipped_first = True
                    continue
                if is_skipped_first:
                    event: TestEvent = MotifcoreAction(line)
                    test_case.append(event)

        script.close()
        return test_case
