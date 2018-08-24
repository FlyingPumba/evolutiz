import datetime
import os
import random

from deap import tools

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from test_runner.test_runner_installer import TestRunnerInstaller
from test_runner.test_runner import TestRunner


class EvolutizTestRunner(TestRunner):

    def __init__(self):
        self.EVOLUTIZ_SCRIPT_PATH_IN_DEVICE = "/mnt/sdcard/evolutiz.script"
        self.minimum_api = 28
        self.test_runner_installer = TestRunnerInstaller("evolutiz",
                                                         settings.WORKING_DIR + "test_runner/evolutiz/evolutiz",
                                                         settings.WORKING_DIR + "test_runner/evolutiz/evolutiz.jar")

    def register_crossover_operator(self, toolbox):
        # the crossover between individuals is a uniform crossover
        # that means each test case has 50-50 probability of ending up in each of the new individuals
        toolbox.register("mate", self.crosover_test_suites)

    def register_mutation_operator(self, toolbox):
        toolbox.register("mutate", self.mutate_test_suite)

    def install_on_devices(self):
        self.test_runner_installer.install_in_all_devices(minimum_api=self.minimum_api)

    def crosover_test_suites(self, ind1, ind2):
        """Perform uniform crossover with probability 0.5 between each individual.
        Perform also one-point crossover for each test case pair that is going to switch places."""
        cxpb = 0.5

        size = min(len(ind1), len(ind2))
        for i in xrange(size):
            if random.random() < cxpb:
                ind1[i], ind2[i] = tools.cxOnePoint(ind2[i], ind1[i])

        return ind1, ind2

    def mutate_test_suite(self, device, package_name, individual):
        # mutate each test case with probability MUTPB
        for i in range(len(individual)):
            if random.random() < settings.MUTPB:
                individual[i] = self.mutate_test_case(device, package_name, individual[i])

        return individual

    def mutate_test_case(self, device, package_name, test_case):
        assert device.api_level() >= self.minimum_api
        result_dir = RequiredFeature('result_dir').request()

        # write individual to local file
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
        local_src_filename = result_dir + "/intermediate/offspring." + ts
        self.write_test_case_to_file(test_case, local_src_filename)

        # push individual to device
        remote_src_filename = "/mnt/sdcard/offspring." + ts
        result_code = adb.push(device, local_src_filename, remote_src_filename,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # call evolutiz test runner
        remote_dst_filename = "/mnt/sdcard/offspring.out." + ts
        evolutiz_cmd = "evolutiz -p " + package_name \
                       + " -v -v -v --throttle 200 --dry --mutate " \
                       + " -f " + remote_src_filename \
                       + " -o " + remote_dst_filename + " 1"

        adb.sudo_shell_command(device, evolutiz_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)
        adb.pkill(device, "evolutiz")

        # fetch mutated individual
        local_dst_filename = result_dir + "/intermediate/offspring.out." + ts
        result_code = adb.pull(device, remote_dst_filename, local_dst_filename,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # get content from local file
        mutated_test_case = self.get_test_case_from_file(local_dst_filename)

        return mutated_test_case

    def run(self, device, package_name, script_name):
        assert device.api_level() >= self.minimum_api
        self.prepare_device_for_run(device)

        evolutiz_cmd = "evolutiz -p " + package_name \
                       + " -v -v -v --throttle 200 --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                       + " -f /mnt/sdcard/" + script_name + " 1"

        adb.sudo_shell_command(device, evolutiz_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

    def generate(self, device, package_name, destination_file_name):
        assert device.api_level() >= self.minimum_api
        self.prepare_device_for_run(device)

        evolutiz_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

        evolutiz_cmd = "evolutiz -p " + package_name \
                       + " -v -v -v --throttle 200 --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " \
                       + " -o " + self.EVOLUTIZ_SCRIPT_PATH_IN_DEVICE \
                       + " -v " + str(evolutiz_events)

        adb.shell_command(device, evolutiz_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

        # need to manually kill evolutiz when timeout
        adb.pkill(device, "evolutiz")

        return self.retrieve_generated_test(device, destination_file_name)

    def retrieve_generated_test(self, device, destination_file_name):
        result_code = adb.pull(device, self.EVOLUTIZ_SCRIPT_PATH_IN_DEVICE, destination_file_name,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Failed to retrieve evolutiz script from device: " + device.name)

        test_content = []

        script = open(destination_file_name)
        is_content = False
        is_skipped_first = False
        for line in script:
            line = line.strip()
            if line.find("start data >>") != -1:
                is_content = True
                continue
            if is_content and line != "":
                if not is_skipped_first:
                    is_skipped_first = True
                    continue
                if is_skipped_first:
                    test_content.append(line)

        script.close()
        return test_content

    def write_test_case_to_file(self, content, filename):
        # check that directory exists before creating file
        dirname = os.path.dirname(filename)
        if not os.path.exists(dirname):
            os.makedirs(dirname)
        with open(filename, "w") as script:
            script.write(settings.SCRIPT_HEADER)
            for line in content:
                script.write(line + "\n")

    def get_test_case_from_file(self, filename):
        test_content = []

        script = open(filename)
        is_content = False
        is_skipped_first = False
        for line in script:
            line = line.strip()
            if line.find("start data >>") != -1:
                is_content = True
                continue
            if is_content and line != "":
                if not is_skipped_first:
                    is_skipped_first = True
                    continue
                if is_skipped_first:
                    test_content.append(line)

        script.close()
        return test_content
