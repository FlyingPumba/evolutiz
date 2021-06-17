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

    def register_app_instrumentator(self):
        features.provide('app_instrumentator', JacocoAppInstrumentator)

    def generate_test_coverage(
            self,
            device: Device,
            coverage_folder_local_path: str,
            accumulated_output: str,
            accumulated_errors: str,
            script_path: str,
            generation: int,
            individual_index: int,
            test_case_index: int,
            unique_crashes: Set[str],
            scripts_crash_status: Dict[str, bool]
    ) -> bool:
        compiled_package_name: str = RequiredFeature('compiled_package_name').request()
        result_dir: str = RequiredFeature('result_dir').request()

        # clear app's data and state
        output, errors, result_code = adb.shell_command(device, f"pm clear {compiled_package_name}")
        accumulated_output += output
        accumulated_errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, script_path, False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
            raise Exception(f"Unable to clear package for script_path {script_path} in device: {device.name}")

        script_name = script_path.split("/")[-1]
        test_runner = RequiredFeature('test_runner').request()
        test_runner.run(device, compiled_package_name, script_name)

        return self.dump_script_coverage(
            device,
            coverage_folder_local_path,
            accumulated_output,
            accumulated_errors,
            script_path,
            generation,
            individual_index,
            test_case_index,
            unique_crashes,
            scripts_crash_status)

    def get_coverage(
            self,
            device: Device,
            coverage_folder_local_path: str,
            accumulated_output: str,
            accumulated_errors: str,
    ) -> int:
        result_dir: str = RequiredFeature('result_dir').request()
        coverage_ec_local_path = f"{coverage_folder_local_path}/coverage.ec"

        # pull coverage.ec file from device
        jacoco_coverage_class_files_path = RequiredFeature('jacoco_coverage_class_files_path').request()
        output, errors, result_code = adb.pull(device,
                                               self.coverage_ec_device_backup_path,
                                               coverage_ec_local_path)
        accumulated_output += output
        accumulated_errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, "pull-coverage", False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
            raise Exception(f"Unable to pull coverage for device: {device.name}")

        # process coverage.ec file
        app_path = RequiredFeature('app_path').request()
        jacoco_cmd = f"java -jar {settings.WORKING_DIR}lib/jacococli.jar " \
                   f"report coverage.ec " \
                   f"--classfiles {jacoco_coverage_class_files_path} " \
                   f"--sourcefiles {settings.WORKING_DIR}{app_path}/src " \
                   f"--xml jacoco_report.xml " \
                   f"--html jacoco_html_report"

        output, errors, result_code = run_cmd(jacoco_cmd, cwd=coverage_folder_local_path)
        accumulated_output += output
        accumulated_errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, "process-coverage", False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
            raise Exception(f"Unable to process coverage.ec file fetched from device: {device.name}")

        # parse generated html to extract global line coverage
        html_path = f"{coverage_folder_local_path}/jacoco_html_report/index.html"
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