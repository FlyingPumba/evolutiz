import time

from deap import creator

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from generation.individual import Individual
from generation.individual_generator import IndividualGenerator
from test_runner.test_event import TestCase, TestSuite
from test_runner.test_runner import TestRunner


class IndividualWithoutCoverageGenerator(IndividualGenerator):

    def __init__(self) -> None:
        super(IndividualWithoutCoverageGenerator, self).__init__()

    def gen_individual(self, device: Device, individual_index: int, generation: int) -> Individual:
        start_time = time.time()
        device.mark_work_start()
        suite = self.get_suite(device, generation, individual_index)
        device.mark_work_stop()

        individual: Individual = getattr(creator, Individual.get_name())(suite)

        finish_time = time.time()
        elapsed_time = finish_time - start_time
        individual.creation_finish_timestamp = finish_time
        individual.creation_elapsed_time = elapsed_time

        individual.index_in_generation = individual_index
        individual.generation = generation

        return individual

    def get_suite(self, device: Device, generation: int, individual_index: int) -> TestSuite:
        test_suite = []

        for test_case_index in range(0, settings.SUITE_SIZE):
            test_case_content = self.get_sequence(device, generation, individual_index, test_case_index)
            test_suite.append(test_case_content)

        return test_suite

    def get_sequence(self, device: Device, generation: int, individual_index: int, test_case_index: int) -> TestCase:
        package_name: str = RequiredFeature('compiled_package_name').request()
        test_runner: TestRunner = RequiredFeature('test_runner').request()

        # clear data before generating new test case
        output, errors, result_code = adb.shell_command(device, f"pm clear {package_name}",
                                                        timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT, retry=2)
        if result_code != 0:
            raise Exception(f"Failed to clear package {package_name} in device: {device.name}.\n{output}\n{errors}")

        script_path = self.get_path_for_test_case(generation, individual_index, test_case_index)
        test_case_content = test_runner.generate(device, package_name, script_path)

        return test_case_content
