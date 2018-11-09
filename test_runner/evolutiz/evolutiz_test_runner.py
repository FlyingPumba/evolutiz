# coding=utf-8
import datetime
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


class EvolutizTestRunner(TestRunner):

    def __init__(self):
        self.evolutiz_script_path_in_devices = "/mnt/sdcard/evolutiz.script"
        self.test_runner_installer = TestRunnerInstaller("evolutiz",
                                                         settings.WORKING_DIR + "test_runner/evolutiz/evolutiz",
                                                         settings.WORKING_DIR + "test_runner/evolutiz/evolutiz.jar")
    def register_minimum_api(self):
        self.minimum_api = 28
        features.provide('minimum_api', self.minimum_api)

    def register_crossover_operator(self, toolbox):
        toolbox.register("mate", tools.cxOnePoint)

    def register_mutation_operator(self, toolbox):
        toolbox.register("mutate", self.mutate_test_suite)

    def install_on_devices(self):
        self.test_runner_installer.install_in_all_devices(minimum_api=self.minimum_api)

    def mutate_test_suite(self, device, package_name, individual):
        """Implements the mutation operator of test suites as described in [ArcuriF13]_.

        The mutation operator for test suites works both at test suite and test case levels: When a test suite T is
        mutated, each of its test cases is mutated with probability 1/|T|. Then, with probability σ = 0.1, a new test
        case is added to the test suite. If it is added, then a second test case is added with probability σ^2 , and so
        on until the ith test case is not added (which happens with probability 1 − σ^i ). Test cases are added only if
        the limit N has not been reached.

        When a test case is chosen to be mutated, we apply a number of mutations at random in between 1 and m, for some
        constant m (which is a parameter that needs to be tuned). For each of these mutations on a test case (which are
        applied sequentially), we apply three different operations with probability 1/3 in order: remove, change and
        insert.

        When removing statements out of a test case of length l, each statement is removed with probability 1/l.
        Removing a statement might invalidate dependencies within the test case, which we attempt to repair; if this
        repair fails, then dependent statements are also deleted. When applying the change mutation, each statement is
        changed with probability 1/l. A change means it is replaced with a different statement that retains the validity
        of the test case; e.g., a different method call with the same return type. When inserting statements, we first
        insert a new statement with probability σ' = 0.5 at a random position. If it is added, then a second statement
        is added with probability σ'^2 , and so on until the ith statement is not inserted. If after applying these
        mutation operators a test case t has no statement left (i.e., all have been removed), then t is removed from T.

        .. [ArcuriF13] A. Arcuri and G. Fraser, “Parameter tuning or default values? An empirical investigation in
         search-based software engineering,” Empirical Software Engineering, vol. 18, no. 3, pp. 594–623, Jun. 2013.

        :param device: the device where the test suite will be mutated.
        :param package_name: the package name of the application being tested.
        :param individual: the test suite.
        :return: mutated test suite.
        """
        result_dir = RequiredFeature('result_dir').request()

        # mutate test cases
        test_suite_size = settings.SUITE_SIZE
        test_case_mutation_pb = 1/float(test_suite_size)

        for i in range(len(individual)):
            if random.random() < test_case_mutation_pb:
                mutated_test_case = self.mutate_test_case(device, package_name, individual[i])
                if len(mutated_test_case) > 0:
                    individual[i] = mutated_test_case
                else:
                    del individual[i]

        # add test cases
        sigma = float(0.1)
        test_case_addition_pb = sigma

        pb = random.random()
        while pb < test_case_mutation_pb and len(individual) < test_suite_size:

            ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
            local_dst_filename = result_dir + "/intermediate/offspring." + ts
            self.generate(device, package_name, local_dst_filename)
            individual.append(self.get_test_case_content_from_file(local_dst_filename))

            test_case_addition_pb = test_case_addition_pb * sigma
            pb = random.random()

        return individual,

    def mutate_test_case(self, device, package_name, test_case):
        assert device.api_level() >= self.minimum_api
        result_dir = RequiredFeature('result_dir').request()

        # write individual to local file
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
        local_src_filename = result_dir + "/intermediate/offspring." + ts
        self.write_test_case_to_file(test_case, local_src_filename)

        # push individual to device
        remote_src_filename = "/mnt/sdcard/offspring." + ts
        output, errors, result_code = adb.push(device, local_src_filename, remote_src_filename,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # call evolutiz test runner
        remote_dst_filename = "/mnt/sdcard/offspring.out." + ts
        evolutiz_cmd = "evolutiz -p " + package_name \
                       + " -v -v -v --throttle 200 --dry --mutate " \
                       + " -f " + remote_src_filename \
                       + " -o " + remote_dst_filename + " 1"

        adb.sudo_shell_command(device, evolutiz_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        adb.pkill(device, "evolutiz")

        # fetch mutated individual
        local_dst_filename = result_dir + "/intermediate/offspring.out." + ts
        output, errors, result_code = adb.pull(device, remote_dst_filename, local_dst_filename,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # get content from local file
        mutated_test_case = self.get_test_case_content_from_file(local_dst_filename)

        return mutated_test_case

    def run(self, device, package_name, script_name):
        assert device.api_level() >= self.minimum_api

        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        evolutiz_cmd = "evolutiz -p " + package_name \
                       + " -v -v -v --throttle 200 --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                       + " -f /mnt/sdcard/" + script_name + " 1"

        output, errors, result_code = adb.shell_command(device, evolutiz_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print("Test case running finished with output:\n" + output)

        if "Exception" in errors:
            device_stacktrace = errors.split("** Error: ")[1]
            raise Exception("An error occurred when running test case: " + device_stacktrace)

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

        if verbose_level > 0:
            logger.log_progress('\nEvolutiz test run took: %.2f seconds' % (time.time() - start_time))

    def generate(self, device, package_name, destination_file_name):
        assert device.api_level() >= self.minimum_api

        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        evolutiz_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

        evolutiz_cmd = "evolutiz -p " + package_name \
                       + " -v -v -v --throttle 200 --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                       + " -o " + self.evolutiz_script_path_in_devices \
                       + " -v " + str(evolutiz_events)

        output, errors, result_code = adb.shell_command(device, evolutiz_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
        if verbose_level > 1:
            print("Test case generation finished with output:\n" + output)

        if "Exception" in errors:
            device_stacktrace = errors.split("** Error: ")[1]
            raise Exception("An error occurred when generating test case: " + device_stacktrace)

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

        test_content = self.retrieve_generated_test(device, destination_file_name)

        if verbose_level > 0:
            logger.log_progress('\nEvolutiz test generation took: %.2f seconds for %d events' % (time.time() - start_time, evolutiz_events))

        return test_content

    def retrieve_generated_test(self, device, destination_file_name):
        output, errors, result_code = adb.pull(device, self.evolutiz_script_path_in_devices, destination_file_name,
                                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            raise Exception("Failed to retrieve evolutiz script from device: " + device.name)

        return self.get_test_case_content_from_file(destination_file_name)
