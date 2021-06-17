import datetime
import os
from typing import Dict, Set, List

from bs4 import UnicodeDammit
from lxml import html

import settings
from coverage.coverage_fetcher import CoverageFetcher, CoverageResult
from coverage.emma.emma_app_instrumentator import EmmaAppInstrumentator
from crashes import crash_handler
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger
from util.command import run_cmd

class EmmaCoverageFetcher(CoverageFetcher):

    def __init__(self) -> None:
        super().__init__()
        self.coverage_ec_device_backup_path = "/mnt/sdcard/coverage.ec"

    def register_app_instrumentator(self):
        features.provide('app_instrumentator', EmmaAppInstrumentator)

    def get_suite_coverage(
            self,
            scripts: List[str],
            device: Device,
            generation: int,
            individual_index: int
    ) -> CoverageResult:
        self.verbose_level = RequiredFeature('verbose_level').request()
        compiled_package_name: str = RequiredFeature('compiled_package_name').request()
        result_dir: str = RequiredFeature('result_dir').request()

        unique_crashes: Set[str] = set()
        scripts_crash_status: Dict[str, bool] = {}
        accumulated_output = ""
        accumulated_errors = ""

        self.clean_coverage_files_in_device(device)

        coverage_folder_local_path = self.prepare_coverage_folder(generation, individual_index)

        # copy coverage.em file to local folder
        coverage_em_local_path = f"{coverage_folder_local_path}/coverage.em"
        os.system(f"cp {result_dir}/coverage.em {coverage_em_local_path}{logger.redirect_string()}")

        adb.shell_command(device, f"am force-stop {compiled_package_name}")

        adb.push_all(device, scripts, "/mnt/sdcard")

        # run scripts
        there_is_coverage = False
        for test_case_index, script_path in enumerate(scripts):
            there_is_coverage = self.generate_test_coverage(
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

        # collect coverage data
        coverage = 0
        if there_is_coverage:
            coverage = self.get_coverage(device,
                                         coverage_folder_local_path,
                                         accumulated_output,
                                         accumulated_errors)

        return coverage, unique_crashes, scripts_crash_status

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

        output, errors, result_code = adb.shell_command(
            device,
            f"am instrument {compiled_package_name}/{compiled_package_name}.EmmaInstrument.EmmaInstrumentation"
        )

        accumulated_output += output
        accumulated_errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, script_path, False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
            raise Exception(f"Unable to instrument for script_path {script_path} in device: {device.name}")

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

    def dump_script_coverage(
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
        package_name: str = RequiredFeature('package_name').request()
        result_dir: str = RequiredFeature('result_dir').request()

        application_files = f"/data/data/{compiled_package_name}/files"
        coverage_ec_device_path = f"{application_files}/coverage.ec"

        if crash_handler.handle(device, script_path, generation, individual_index, test_case_index, unique_crashes):
            scripts_crash_status[script_path] = True
            return False
        else:
            # no crash, can broadcast
            scripts_crash_status[script_path] = False

            # don't check result_code of the following command, since it can fail if this is the first time running emma
            # for this test suite and there is no coverage.ec file in /mnt/sdcard folder.
            adb.sudo_shell_command(
                device,
                f"cp -p {self.coverage_ec_device_backup_path} {coverage_ec_device_path}"
            )

            broadcast = f"am broadcast -a evolutiz.emma.COLLECT_COVERAGE " \
                        f"-n {compiled_package_name}/{package_name}.EmmaInstrument.CollectCoverageReceiver"
            output, errors, result_code = adb.shell_command(device, broadcast, timeout=60)
            accumulated_output += output
            accumulated_errors += errors
            if "Exception" in errors:
                adb.log_evaluation_result(device, result_dir, script_path, False)
                if self.verbose_level > 0:
                    logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
                raise Exception(f"Unable to broadcast coverage gathering for "
                                f"script_path {script_path} in device: {device.name}")

            if not adb.exists_file(device, coverage_ec_device_path):
                adb.log_evaluation_result(device, result_dir, script_path, False)
                if self.verbose_level > 0:
                    logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
                raise Exception(f"Coverage broadcast was sent for script_path {script_path} "
                                f"in device: {device.name} but there is not file: {coverage_ec_device_path}")

            adb.log_evaluation_result(device, result_dir, script_path, True)

            # save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
            cp_command = f"cp -p {coverage_ec_device_path} {self.coverage_ec_device_backup_path}"
            output, errors, result_code = adb.sudo_shell_command(device, cp_command)
            accumulated_output += output
            accumulated_errors += errors
            if result_code != 0:
                adb.log_evaluation_result(device, result_dir, script_path, False)
                if self.verbose_level > 0:
                    logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
                raise Exception(f"Unable to retrieve coverage.ec file after coverage broadcast for "
                                f"script_path {script_path} in  device: {device.name}")

            return True

    def clean_coverage_files_in_device(self, device: Device) -> None:
        compiled_package_name: str = RequiredFeature('compiled_package_name').request()

        application_files = f"/data/data/{compiled_package_name}/files"
        coverage_ec_device_path = f"{application_files}/coverage.ec"

        adb.shell_command(device, f"rm -f {coverage_ec_device_path}")
        adb.shell_command(device, f"rm -f {self.coverage_ec_device_backup_path}")

    def prepare_coverage_folder(self, generation: int, individual_index: int) -> str:
        result_dir: str = RequiredFeature('result_dir').request()

        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")

        coverage_folder_name = f"{str(generation)}.{str(individual_index)}.{ts}"
        coverage_folder_path = f"{result_dir}/coverage/{coverage_folder_name}"

        os.system(f"mkdir -p {coverage_folder_path}")
        return coverage_folder_path

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
        output, errors, result_code = adb.pull(device, self.coverage_ec_device_backup_path, coverage_ec_local_path)
        accumulated_output += output
        accumulated_errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, "pull-coverage", False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
            raise Exception(f"Unable to pull coverage for device: {device.name}")

        # process coverage.ec file
        app_path = RequiredFeature('app_path').request()
        emma_cmd = f"java -cp {settings.WORKING_DIR}lib/emma.jar emma report -r html" \
                   f" -in coverage.em,coverage.ec -sp {settings.WORKING_DIR}{app_path}/src"
        output, errors, result_code = run_cmd(emma_cmd, cwd=coverage_folder_local_path)
        accumulated_output += output
        accumulated_errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, "process-coverage", False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
            raise Exception(f"Unable to process coverage.ec file fetched from device: {device.name}")

        # parse generated html to extract global line coverage
        html_path = f"{coverage_folder_local_path}/coverage/index.html"
        coverage_str = self.extract_coverage(html_path)

        aux = coverage_str.split("%")
        coverage = int(aux[0])

        return coverage

    def extract_coverage(self, html_path: str) -> str:
        with open(html_path, 'rb') as file:
            content = file.read()
            doc = UnicodeDammit(content, is_html=True)

        parser = html.HTMLParser(encoding=doc.original_encoding)
        root = html.document_fromstring(content, parser=parser)
        xpath: List[str] = root.xpath('/html/body/table[2]/tr[2]/td[5]/text()')
        return xpath[0].strip()
