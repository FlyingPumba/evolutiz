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
        features.provide('app_instrumentator', EllaAppInstrumentator)

    def get_suite_coverage(
            self,
            scripts: List[str],
            device: Device,
            generation: int,
            individual_index: int
    ) -> CoverageResult:

        self.verbose_level = RequiredFeature('verbose_level').request()
        self.compiled_package_name: str = RequiredFeature('compiled_package_name').request()
        self.package_name: str = RequiredFeature('package_name').request()
        self.result_dir: str = RequiredFeature('result_dir').request()

        unique_crashes: Set[str] = set()
        scripts_crash_status: Dict[str, bool] = {}
        self.output = ""
        self.errors = ""

        # stop target app in the device (just in case)
        adb.shell_command(device, f"am force-stop {self.compiled_package_name}")

        self.set_coverage_paths(device, generation, individual_index)

        # upload the test scripts to device
        adb.push_all(device, scripts, "/mnt/sdcard")

        # run scripts
        for test_case_index, script_path in enumerate(scripts):
            self.generate_test_coverage(device,
                                        script_path,
                                        generation,
                                        individual_index,
                                        test_case_index,
                                        unique_crashes,
                                        scripts_crash_status)

        # collect coverage data
        coverage = self.get_coverage(device)

        return coverage, unique_crashes, scripts_crash_status

    def generate_test_coverage(self,
                               device: Device,
                               script_path: str,
                               generation: int,
                               individual_index: int,
                               test_case_index: int,
                               unique_crashes: Set[str],
                               scripts_crash_status: Dict[str, bool]
                               ) -> None:
        # clear app's data and state
        output, errors, result_code = adb.shell_command(device, f"pm clear {self.compiled_package_name}")
        self.output += output
        self.errors += errors
        if result_code != 0:
            adb.log_evaluation_result(device, self.result_dir, script_path, False)
            if self.verbose_level > 0:
                logger.log_progress(f"\n{self.output}\n{self.errors}")
            raise Exception(f"Unable to clear package for script_path {script_path} in device: {device.name}")

        # copy the covids file to the coverage_folder_local_path
        output, errors, result_code = run_cmd(
            f"cp {self.ella_coverage_ids_file_path} {self.coverage_folder_local_path}/")
        if result_code != 0:
            raise Exception(
                f"Unable to copy the coverage ids file for test script, path is: {self.ella_coverage_ids_file_path}")

        # Run test case using the requested test runner.
        # Internally, the test runner will take care of calling the methods AppInstrumentator#setup_for_test_run and
        # AppInstrumentator#teardown_after_test_run, which will start and stop the ELLA server correspondingly.
        script_name = script_path.split("/")[-1]
        test_runner = RequiredFeature('test_runner').request()
        test_runner.run(device, self.compiled_package_name, script_name)

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
        if crash_handler.handle(device, script_path, generation, individual_index, test_case_index, unique_crashes):
            scripts_crash_status[script_path] = True
        else:
            # no crash, collect coverage
            scripts_crash_status[script_path] = False

            adb.log_evaluation_result(device, self.result_dir, script_path, True)

            # save the coverage.dat file of this test script
            output, errors, result_code = run_cmd(f"mv {self.get_coverage_dat_path(device)} {self.coverage_folder_local_path}/")
            if result_code != 0:
                raise Exception(f"Unable to move the coverage dat file for test script, path is: {self.get_coverage_dat_path(device)}")

    def set_coverage_paths(self, device: Device, generation: int, individual_index: int) -> None:
        self.coverage_folder_local_path = self.prepare_coverage_folder(generation, individual_index)
        self.ella_coverage_ids_file_path = f"{self.get_current_apk_output_folder(device)}/covids"

    def prepare_coverage_folder(self, generation: int, individual_index: int) -> str:
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")

        coverage_folder_name = f"{str(generation)}.{str(individual_index)}.{ts}"
        coverage_folder_path = f"{self.result_dir}/coverage/{coverage_folder_name}"

        os.system(f"mkdir -p {coverage_folder_path}")
        return coverage_folder_path

    def get_coverage(self, device: Device) -> int:
        # get the unique ids of the methods listed in all the coverage.dat files
        output, errors, result_code = run_cmd(
            f"cat {self.coverage_folder_local_path}/coverage.dat* | grep -v \"#\" | sort -u | wc -l")
        number_of_methods_covered = float(output.strip().rstrip('\n'))

        output, errors, result_code = run_cmd(
            f"cat {self.coverage_folder_local_path}/covids | wc -l")
        total_number_of_methods = float(output.strip().rstrip('\n'))

        method_coverage = int(number_of_methods_covered / total_number_of_methods * 100)

        return method_coverage
