import sys
import time
from typing import Tuple, Set, Dict, Any

import numpy
from deap import creator

import settings
from coverage.emma_coverage import EmmaCoverage
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from generation.individual import Individual
from generation.individual_generator import IndividualGenerator, TestSuite
from test_runner.test_runner import TestCase, TestRunner


class IndividualWithCoverageGenerator(IndividualGenerator, EmmaCoverage):

    def __init__(self):
        super(IndividualWithCoverageGenerator, self).__init__()

    def gen_individual(self, device, individual_index, generation) -> Any:
        start_time = time.time()
        device.mark_work_start()
        suite, fitness = self.get_suite_with_fitness(device, generation, individual_index)
        device.mark_work_stop()

        individual: Individual = getattr(creator, Individual.get_name())(suite)
        individual.fitness.values = fitness

        finish_time = time.time()
        elapsed_time = finish_time - start_time
        individual.creation_finish_timestamp = finish_time
        individual.creation_elapsed_time = elapsed_time

        individual.evaluation_finish_timestamp = finish_time
        # the following will indicate that generation and evaluation occurred at the same time
        individual.evaluation_elapsed_time = 0

        individual.index_in_generation = individual_index
        individual.generation = generation

        return individual

    def get_suite_with_fitness(self, device, generation, individual_index) -> Tuple[TestSuite, Tuple[float, float, int]]:
        self.package_name: str = RequiredFeature('package_name').request()
        self.result_dir: str = RequiredFeature('result_dir').request()

        test_suite = []
        lengths = []
        unique_crashes: Set[str] = set()
        scripts_crash_status: Dict[str, bool] = {}

        self.there_is_coverage = False
        self.set_coverage_paths(device, generation, individual_index)
        adb.shell_command(device, f"am force-stop {self.package_name}")

        # run scripts
        for test_case_index in range(0, settings.SUITE_SIZE):
            script_path = self.get_path_for_test_case(generation, individual_index, test_case_index)
            test_content = self.generate_test_and_coverage(device, script_path, generation, individual_index,
                                                           test_case_index, unique_crashes, scripts_crash_status)

            test_suite.append(test_content)
            if scripts_crash_status[script_path]:
                lengths.append(len(test_content))

        # collect fitness data
        coverage = 0
        if self.there_is_coverage:
            coverage = self.get_coverage(device)

        crashes = len(unique_crashes)

        length = sys.maxsize
        if len(lengths) > 0:
            length = numpy.mean(lengths)

        return test_suite, (coverage, length, crashes)

    def generate_test_and_coverage(self,
                                   device: Device,
                                   script_path: str,
                                   generation: int,
                                   individual_index: int,
                                   test_case_index: int,
                                   unique_crashes: Set[str],
                                   scripts_crash_status: Dict[str, bool]
                                   ) -> TestCase:

        # clear app's data and state
        output, errors, result_code = adb.shell_command(device, f"pm clear {self.package_name}")
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, script_path, False)
            raise Exception(f"Unable to clear package for script_path {script_path} in device: {device.name}")

        # generate test case
        test_runner: TestRunner = RequiredFeature('test_runner').request()
        test_content = test_runner.generate(device, self.package_name, script_path)

        self.dump_script_coverage(device, script_path, generation, individual_index, test_case_index, unique_crashes,
                                  scripts_crash_status)

        return test_content
