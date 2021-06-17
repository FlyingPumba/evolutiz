from typing import Dict, Set, Tuple, List

from devices.device import Device

CoverageResult = Tuple[int, Set[str], Dict[str, bool]]

class CoverageFetcher(object):

    def __init__(self) -> None:
        self.coverage_ec_device_backup_path = "/mnt/sdcard/coverage.ec"

    def register_app_instrumentator(self):
        raise NotImplementedError

    def get_suite_coverage(
            self,
            scripts: List[str],
            device: Device,
            generation: int,
            individual_index: int
    ) -> CoverageResult:
        """
        Runs a test suite and returns its coverage.

        :param scripts:
        :param device:
        :param generation:
        :param individual_index:
        :return:
        """
        raise NotImplementedError

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
        """
        Runs a test case and returns whether the coverage was generated successfully.

        :param device:
        :param coverage_folder_local_path:
        :param accumulated_output:
        :param accumulated_errors:
        :param script_path:
        :param generation:
        :param individual_index:
        :param test_case_index:
        :param unique_crashes:
        :param scripts_crash_status:
        :return:
        """
        raise NotImplementedError

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
        """
        Dumps the accumulated coverage in the device to an internal file (i.e., in the sdcard).
        Returns whether the coverage was dumped successfully.

        :param device:
        :param coverage_folder_local_path:
        :param accumulated_output:
        :param accumulated_errors:
        :param script_path:
        :param generation:
        :param individual_index:
        :param test_case_index:
        :param unique_crashes:
        :param scripts_crash_status:
        :return:
        """
        raise NotImplementedError

    def prepare_coverage_folder(self, generation: int, individual_index: int) -> str:
        """
        Prepare the coverage folder in which to save the coverage results for an individual in a generation.
        :param device:
        :param generation:
        :param individual_index:
        :return:
        """
        raise NotImplementedError

    def get_coverage(
            self,
            device: Device,
            coverage_folder_local_path: str,
            accumulated_output: str,
            accumulated_errors: str,
    ) -> int:
        """
        Pulls accumulated coverage from device and saves it locally.
        It also parses the result and returns the line coverage found.

        :param device:
        :param coverage_folder_local_path:
        :param accumulated_output:
        :param accumulated_errors:
        :return:
        """
        raise NotImplementedError
