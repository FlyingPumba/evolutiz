import datetime
import os
from typing import Dict, Set, List

from coverage.coverage_fetcher import CoverageFetcher, CoverageResult
from coverage.ella.ella import Ella
from coverage.ella.ella_app_instrumentator import EllaAppInstrumentator
from crashes import crash_handler
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger
from util.command import run_cmd


class EllaCoverageFetcher(CoverageFetcher, Ella):

    def __init__(self) -> None:
        super(CoverageFetcher, self).__init__()
        super(Ella, self).__init__()

    def register_app_instrumentator(self):
        features.provide('app_instrumentator', EllaAppInstrumentator())

    def get_suite_coverage(
            self,
            scripts: List[str],
            device: Device,
            generation: int,
            individual_index: int
    ) -> CoverageResult:
        self.verbose_level = RequiredFeature('verbose_level').request()
        compiled_package_name: str = RequiredFeature('compiled_package_name').request()

        unique_crashes: Set[str] = set()
        scripts_crash_status: Dict[str, bool] = {}
        accumulated_output = ""
        accumulated_errors = ""

        # stop target app in the device (just in case)
        adb.shell_command(device, f"am force-stop {compiled_package_name}")

        coverage_folder_local_path = self.prepare_coverage_folder(generation, individual_index)

        # upload the test scripts to device
        adb.push_all(device, scripts, "/mnt/sdcard")

        # run scripts
        for test_case_index, script_path in enumerate(scripts):
            self.generate_test_coverage(device,
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
        accumulated_output += str(output)
        accumulated_errors += str(errors)
        if result_code != 0:
            adb.log_evaluation_result(device, result_dir, script_path, False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{accumulated_output}\n{accumulated_errors}")
            raise Exception(f"Unable to clear package for script_path {script_path} in device: {device.name}")

        # copy the covids file to the coverage_folder_local_path
        ella_coverage_ids_file_path = f"{self.get_current_apk_output_folder(device)}/covids"
        output, errors, result_code = run_cmd(
            f"cp {ella_coverage_ids_file_path} {coverage_folder_local_path}/")
        if result_code != 0:
            raise Exception(
                f"Unable to copy the coverage ids file for test script, path is: {ella_coverage_ids_file_path}")

        # Run test case using the requested test runner.
        # Internally, the test runner will take care of calling the methods AppInstrumentator#setup_for_test_run and
        # AppInstrumentator#teardown_after_test_run, which will start and stop the ELLA server correspondingly.
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
        result_dir: str = RequiredFeature('result_dir').request()

        if crash_handler.handle(device, script_path, generation, individual_index, test_case_index, unique_crashes):
            scripts_crash_status[script_path] = True
            return False
        else:
            # no crash, collect coverage
            scripts_crash_status[script_path] = False

            adb.log_evaluation_result(device, result_dir, script_path, True)

            # save the coverage.dat file of this test script
            output, errors, result_code = run_cmd(f"mv {self.get_coverage_dat_path(device)} {coverage_folder_local_path}/")
            if result_code != 0:
                raise Exception(f"Unable to move the coverage dat file for test script, path is: {self.get_coverage_dat_path(device)}")

            return True

    def prepare_coverage_folder(self, generation: int, individual_index: int) -> str:
        result_dir: str = RequiredFeature('result_dir').request()

        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")

        coverage_folder_name = f"{str(generation)}.{str(individual_index)}.{ts}"
        coverage_folder_local_path = f"{result_dir}/coverage/{coverage_folder_name}"

        os.system(f"mkdir -p {coverage_folder_local_path}")
        return coverage_folder_local_path

    def get_coverage(
            self,
            device: Device,
            coverage_folder_local_path: str,
            accumulated_output: str,
            accumulated_errors: str,
    ) -> int:
        # get the unique ids of the methods listed in all the coverage.dat files
        output, errors, result_code = run_cmd(
            f"cat {coverage_folder_local_path}/coverage.dat* | grep -v \"#\" | sort -u | wc -l")
        accumulated_output += output
        accumulated_errors += errors

        number_of_methods_covered = float(output.strip().rstrip('\n'))

        output, errors, result_code = run_cmd(
            f"cat {coverage_folder_local_path}/covids | wc -l")
        accumulated_output += output
        accumulated_errors += errors

        total_number_of_methods = float(output.strip().rstrip('\n'))

        method_coverage = int(number_of_methods_covered / total_number_of_methods * 100)

        return method_coverage
