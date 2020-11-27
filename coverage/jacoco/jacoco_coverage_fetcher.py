from typing import Dict, Set

import settings
from coverage.emma.emma_coverage_fetcher import EmmaCoverageFetcher
from coverage.jacoco.jacoco_app_instrumentator import JacocoAppInstrumentator
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger
from util.command import run_cmd


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
        output, errors, result_code = adb.shell_command(device, f"pm clear {self.compiled_package_name}")
        self.output += output
        self.errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, script_path, False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{self.output}\n{self.errors}")
            raise Exception(f"Unable to clear package for script_path {script_path} in device: {device.name}")

        script_name = script_path.split("/")[-1]
        test_runner = RequiredFeature('test_runner').request()
        test_runner.run(device, self.compiled_package_name, script_name)

        self.dump_script_coverage(device, script_path, generation, individual_index, test_case_index, unique_crashes,
                                  scripts_crash_status)

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
        application_files = f"/data/data/{self.compiled_package_name}/files"

        self.coverage_ec_device_path = f"{application_files}/coverage.ec"
        self.clean_coverage_files_in_device(device)

        self.coverage_folder_local_path = self.prepare_coverage_folder(generation, individual_index)
        self.coverage_ec_local_path = f"{self.coverage_folder_local_path}/coverage.ec"

    def get_coverage(self, device: Device) -> int:
        """
        Pulls accumulated coverage from device and saves it locally.
        It also parses the result and returns the line coverage found.

        :param device:
        :return:
        """
        # pull coverage.ec file from device
        jacoco_coverage_class_files_path = RequiredFeature('jacoco_coverage_class_files_path').request()
        output, errors, result_code = adb.pull(device,
                                               self.coverage_ec_device_backup_path,
                                               self.coverage_ec_local_path)
        self.output += output
        self.errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, "pull-coverage", False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{self.output}\n{self.errors}")
            raise Exception(f"Unable to pull coverage for device: {device.name}")

        # process coverage.ec file
        app_path = RequiredFeature('app_path').request()
        jacoco_cmd = f"java -jar {settings.WORKING_DIR}lib/jacococli.jar " \
                   f"report coverage.ec " \
                   f"--classfiles {jacoco_coverage_class_files_path} " \
                   f"--sourcefiles {settings.WORKING_DIR}{app_path}/src " \
                   f"--xml jacoco_report.xml " \
                   f"--html jacoco_html_report"

        output, errors, result_code = run_cmd(jacoco_cmd, cwd=self.coverage_folder_local_path)
        self.output += output
        self.errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, "process-coverage", False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{self.output}\n{self.errors}")
            raise Exception(f"Unable to process coverage.ec file fetched from device: {device.name}")

        # parse generated html to extract global line coverage
        html_path = f"{self.coverage_folder_local_path}/jacoco_html_report/index.html"
        return self.extract_coverage(html_path)

    def extract_coverage(self, html_path: str) -> int:
        xpath_missed_lines = "html/body/table/tfoot/tr/td[8]/text()"
        xpath_missed_lines_cmd = f"xmllint --html -xpath \"{xpath_missed_lines}\" {html_path}"
        output, errors, result_code = run_cmd(xpath_missed_lines_cmd)
        missed_lines_str = output.strip("\n")

        xpath_total_lines = "html/body/table/tfoot/tr/td[9]/text()"
        xpath_total_lines_cmd = f"xmllint --html -xpath \"{xpath_total_lines}\" {html_path}"
        output, errors, result_code = run_cmd(xpath_total_lines_cmd)
        total_lines_str = output.strip("\n")

        missed_lines = float(missed_lines_str.replace(",", ""))
        total_lines = float(total_lines_str.replace(",", ""))
        covered_lines = total_lines - missed_lines

        coverage = int(covered_lines / total_lines * 100)
        return coverage