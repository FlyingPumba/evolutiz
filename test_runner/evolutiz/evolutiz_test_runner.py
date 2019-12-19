# coding=utf-8
import time

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from test_runner.evolutiz.crossover import EvolutizCrossover
from test_runner.evolutiz.evolutiz_connector import EvolutizConnector
from test_runner.evolutiz.mutation import EvolutizMutation
from test_runner.evolutiz.widget_action import WidgetAction
from test_runner.evolutiz.widget_action_result import WidgetActionResult
from test_runner.test_runner import TestRunner, TestCase
from test_runner.test_runner_installer import TestRunnerInstaller
from util import logger


class EvolutizTestRunner(TestRunner):

    def __init__(self) -> None:
        super().__init__()
        self.evolutiz_script_path_in_devices = "/mnt/sdcard/evolutiz.script"
        self.test_runner_installer = TestRunnerInstaller("evolutiz",
                                                         f"{settings.WORKING_DIR}test_runner/evolutiz/evolutiz",
                                                         f"{settings.WORKING_DIR}test_runner/evolutiz/evolutiz.jar")
        self.evolutiz_connector = EvolutizConnector()

    def register_minimum_api(self) -> None:
        self.minimum_api = 28
        features.provide('minimum_api', self.minimum_api)

    def register_crossover_operator(self, toolbox):
        evlutiz_crossover = EvolutizCrossover(self.evolutiz_connector)
        toolbox.register("mate", evlutiz_crossover.crossover)

    def register_mutation_operator(self, toolbox):
        evlutiz_mutation = EvolutizMutation(self.evolutiz_connector)
        toolbox.register("mutate", evlutiz_mutation.mutation)

    def install_on_devices(self):
        self.test_runner_installer.install_in_all_devices(minimum_api=self.minimum_api)

    def run(self, device, package_name: str, script_name: str):
        assert device.api_level() >= self.minimum_api

        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        evolutiz_cmd = f"evolutiz -p {package_name} -v -v -v --throttle 200 --ignore-crashes " \
                       f"--ignore-security-exceptions --ignore-timeouts --bugreport -f /mnt/sdcard/{script_name} 1"

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

    def generate(self, device: 'Device', package_name: str, destination_file_name: str) -> TestCase:
        assert device.api_level() >= self.minimum_api

        verbose_level = RequiredFeature('verbose_level').request()
        start_time = time.time()

        self.prepare_device_for_run(device)

        evolutiz_events = settings.SEQUENCE_LENGTH_MAX
        test_case = []

        launch_result = self.evolutiz_connector.send_command(device, package_name, f"performview launch-app")
        time.sleep(1)

        for i in range(0, evolutiz_events):
            widget_action_result = WidgetAction.random(device, self.evolutiz_connector)
            test_case.append(widget_action_result)

            if widget_action_result.is_outbound():
                break

        if verbose_level > 0:
            logger.log_progress(f'\nEvolutiz test generation took: {time.time() - start_time:.2f} '
                                f'seconds for {len(test_case):d} events')

        return test_case
