from typing import Dict, Set

from coverage.emma.emma_coverage_fetcher import EmmaCoverageFetcher
from coverage.jacoco.jacoco_app_instrumentator import JacocoAppInstrumentator
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger


class JacocoCoverageFetcher(EmmaCoverageFetcher):

    def __init__(self) -> None:
        super().__init__()
        self.coverage_ec_device_backup_path = "/mnt/sdcard/coverage.ec"

    def register_app_instrumentator(self):
        features.provide('app_instrumentator', JacocoAppInstrumentator)

    def generate_test_coverage(self,
                               device: Device,
                               script_path: str,
                               generation: int,
                               individual_index: int,
                               test_case_index: int,
                               unique_crashes: Set[str],
                               scripts_crash_status: Dict[str, bool]
                               ) -> None:
        """
        Runs a test case and returns its coverage.

        :param device:
        :param script_path:
        :param generation:
        :param individual_index:
        :param test_case_index:
        :param unique_crashes:
        :param scripts_crash_status:
        :return:
        """

        # clear app's data and state
        output, errors, result_code = adb.shell_command(device, f"pm clear {self.package_name}")
        self.output += output
        self.errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, script_path, False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{self.output}\n{self.errors}")
            raise Exception(f"Unable to clear package for script_path {script_path} in device: {device.name}")

        script_name = script_path.split("/")[-1]
        test_runner = RequiredFeature('test_runner').request()
        test_runner.run(device, self.package_name, script_name)

        self.dump_script_coverage(device, script_path, generation, individual_index, test_case_index, unique_crashes,
                                  scripts_crash_status)

    def dump_script_coverage(self,
                             device: Device,
                             script_path: str,
                             generation: int,
                             individual_index: int,
                             test_case_index: int,
                             unique_crashes: Set[str],
                             scripts_crash_status: Dict[str, bool]
                             ) -> None:
        """
        Dumps the accumulated coverage in the device to an internal file (i.e., in the sdcard).

        :param device:
        :param script_path:
        :param generation:
        :param individual_index:
        :param test_case_index:
        :param unique_crashes:
        :param scripts_crash_status:
        :return:
        """
        raise NotImplementedError

    def set_coverage_paths(self, device: Device, generation: int, individual_index: int) -> None:
        """
        Sets the appropiate value of the following variables for the generation and individual_index provided:

        - self.coverage_ec_device_path = path in the device for the coverage.ec file after dumping it.
        - self.coverage_folder_local_path = path in local computer to store the coverage files.
        - self.coverage_ec_local_path = path in local computer where the coverage.ec file is going to be stored.

        :param device:
        :param generation:
        :param individual_index:
        :return:
        """
        raise NotImplementedError

    def get_coverage(self, device: Device) -> int:
        """
        Pulls accumulated coverage from device and saves it locally.
        It also parses the result and returns the line coverage found.

        :param device:
        :return:
        """
        raise NotImplementedError
