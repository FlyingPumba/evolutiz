# coding=utf-8
import random
import time
from typing import Tuple

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from test_runner.evolutiz.evolutiz_connector import EvolutizConnector
from test_runner.evolutiz.widget_action import WidgetAction
from test_runner.evolutiz.widget_action_result import EvolutizTestCase, EvolutizTestSuite


class EvolutizMutation(object):

    def __init__(self, evolutiz_connector: EvolutizConnector) -> None:
        self.evolutiz_connector = evolutiz_connector

    def mutation(self, individual: EvolutizTestSuite) -> Tuple[EvolutizTestSuite]:
        """Implements a mutation function for test suites.
        It consists of randomly choosing a test case of the test suite and mutating it.
        """
        device_manager = RequiredFeature('device_manager').request()
        devices = device_manager.get_idle_devices()
        device = random.choice(devices)

        device.mark_work_start()

        random_index = random.randint(0, len(individual)-1)
        individual[random_index] = self.mutate_test_case(device, individual[random_index])

        device.mark_work_stop()

        return individual,

    def mutate_test_case(self, device: Device, test_case: EvolutizTestCase) -> EvolutizTestCase:
        """Implements a cut point mutation function for test cases.
        """
        # Copy actions from original test case up until a cut point
        package_name = RequiredFeature('package_name').request()

        cut_point = random.randint(1, len(test_case)) # TODO: or is it random.randint(1, len(test_case)-1)
        mutated_test_case = test_case[:cut_point-1]

        adb.shell_command(device, f"pm clear {package_name}")
        self.evolutiz_connector.send_command(device, package_name, f"performview launch-app")
        time.sleep(1)

        # re-execute said actions
        for widget_action_result in mutated_test_case:
            success = widget_action_result.widget_action.execute(device, self.evolutiz_connector)
            if not success:
                raise Exception("Unable to re-execute test case in mutation operator")

        # from there onwards, execute random actions until we've reached max number of events or we have left the app
        while len(mutated_test_case) < settings.SEQUENCE_LENGTH_MAX:
            widget_action_result = WidgetAction.random(device, self.evolutiz_connector)
            mutated_test_case.append(widget_action_result)

            if widget_action_result.is_outbound():
                break

        return mutated_test_case

    #
    # def mutate_test_suite(self, device, package_name, individual):
    #     """Implements the mutation operator of test suites as described in [ArcuriF13]_.
    #
    #     The mutation operator for test suites works both at test suite and test case levels: When a test suite T is
    #     mutated, each of its test cases is mutated with probability 1/|T|. Then, with probability σ = 0.1, a new test
    #     case is added to the test suite. If it is added, then a second test case is added with probability σ^2 , and so
    #     on until the ith test case is not added (which happens with probability 1 − σ^i ). Test cases are added only if
    #     the limit N has not been reached.
    #
    #     When a test case is chosen to be mutated, we apply a number of mutations at random in between 1 and m, for some
    #     constant m (which is a parameter that needs to be tuned). For each of these mutations on a test case (which are
    #     applied sequentially), we apply three different operations with probability 1/3 in order: remove, change and
    #     insert.
    #
    #     When removing statements out of a test case of length l, each statement is removed with probability 1/l.
    #     Removing a statement might invalidate dependencies within the test case, which we attempt to repair; if this
    #     repair fails, then dependent statements are also deleted. When applying the change mutation, each statement is
    #     changed with probability 1/l. A change means it is replaced with a different statement that retains the validity
    #     of the test case; e.g., a different method call with the same return type. When inserting statements, we first
    #     insert a new statement with probability σ' = 0.5 at a random position. If it is added, then a second statement
    #     is added with probability σ'^2 , and so on until the ith statement is not inserted. If after applying these
    #     mutation operators a test case t has no statement left (i.e., all have been removed), then t is removed from T.
    #
    #     .. [ArcuriF13] A. Arcuri and G. Fraser, “Parameter tuning or default values? An empirical investigation in
    #      search-based software engineering,” Empirical Software Engineering, vol. 18, no. 3, pp. 594–623, Jun. 2013.
    #
    #     :param device: the device where the test suite will be mutated.
    #     :param package_name: the package name of the application being tested.
    #     :param individual: the test suite.
    #     :return: mutated test suite.
    #     """
    #     result_dir: str = RequiredFeature('result_dir').request()
    #
    #     # mutate test cases
    #     test_suite_size = settings.SUITE_SIZE
    #     test_case_mutation_pb = 1/float(test_suite_size)
    #
    #     for i in range(len(individual)):
    #         if random.random() < test_case_mutation_pb:
    #             mutated_test_case = self.mutate_test_case(device, package_name, individual[i])
    #             if len(mutated_test_case) > 0:
    #                 individual[i] = mutated_test_case
    #             else:
    #                 del individual[i]
    #
    #     # add test cases
    #     sigma = float(0.1)
    #     test_case_addition_pb = sigma
    #
    #     pb = random.random()
    #     while pb < test_case_mutation_pb and len(individual) < test_suite_size:
    #
    #         ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
    #         local_dst_filename = f"{result_dir}/intermediate/offspring.{ts}"
    #         test_content = self.generate(device, package_name, local_dst_filename)
    #         individual.append(test_content)
    #
    #         test_case_addition_pb = test_case_addition_pb * sigma
    #         pb = random.random()
    #
    #     return individual,
    #
    # def mutate_test_case(self, device, package_name: str, test_case):
    #     # assert device.api_level() >= self.minimum_api
    #
    #     result_dir: str = RequiredFeature('result_dir').request()
    #
    #     # write individual to local file
    #     ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
    #     local_src_filename: str = f"{result_dir}/intermediate/offspring.{ts}"
    #     self.write_test_case_to_file(test_case, local_src_filename)
    #
    #     # push individual to device
    #     remote_src_filename: str = f"/mnt/sdcard/offspring.{ts}"
    #     output, errors, result_code = adb.push(device, local_src_filename, remote_src_filename,
    #                            timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    #
    #     # call evolutiz test runner
    #     remote_dst_filename = f"/mnt/sdcard/offspring.out.{ts}"
    #     evolutiz_cmd = f"evolutiz -p {package_name} -v -v -v --throttle 200 --dry --mutate" \
    #                    f" -f {remote_src_filename} -o {remote_dst_filename} 1"
    #
    #     adb.sudo_shell_command(device, evolutiz_cmd, timeout=settings.TEST_CASE_EVAL_TIMEOUT)
    #     adb.pkill(device, "evolutiz")
    #
    #     # fetch mutated individual
    #     local_dst_filename = f"{result_dir}/intermediate/offspring.out.{ts}"
    #     output, errors, result_code = adb.pull(device, remote_dst_filename, local_dst_filename,
    #                            timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    #
    #     # get content from local file
    #     mutated_test_case = self.get_test_case_content_from_file(local_dst_filename)
    #
    #     return mutated_test_case