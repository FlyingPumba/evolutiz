import time

from deap import creator

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from test_suite_generation.individual_generator import IndividualGenerator


class IndividualWithoutCoverageGenerator(IndividualGenerator):

    def __init__(self):
        super(IndividualWithoutCoverageGenerator, self).__init__()

    def gen_individual(self, device, individual_index, generation):
        budget_manager = RequiredFeature('budget_manager').request()

        start_time = time.time()
        device.mark_work_start()
        suite = self.get_suite(device, generation, individual_index)
        device.mark_work_stop()

        individual = creator.Individual(suite)

        finish_time = time.time()
        elapsed_time = finish_time - start_time
        individual.creation_finish_timestamp = finish_time
        individual.creation_elapsed_time = elapsed_time

        individual.index_in_generation = individual_index
        individual.generation = generation

        budget_manager.increase_time_budget(elapsed_time)

        return individual

    def get_suite(self, device, generation, individual_index):
        test_suite = []

        for test_case_index in range(0, settings.SUITE_SIZE):
            test_case_content = self.get_sequence(device, generation, individual_index, test_case_index)
            test_suite.append(test_case_content)

        return test_suite

    def get_sequence(self, device, generation, individual_index, test_case_index):
        package_name = RequiredFeature('package_name').request()
        test_runner = RequiredFeature('test_runner').request()

        # clear data before generating new test case
        output, errors, result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT, retry=2)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Failed to clear package " + package_name + " in device: " + device.name)

        script_path = self.get_path_for_test_case(generation, individual_index, test_case_index)
        test_case_content = test_runner.generate(device, package_name, script_path)

        return test_case_content
