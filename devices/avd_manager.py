import traceback
from subprocess import TimeoutExpired
from typing import List

from dependency_injection.required_feature import RequiredFeature
from util.command import run_cmd


class AvdManager(object):

    def __init__(self) -> None:
        self.avd_series: str = RequiredFeature('avd_series').request()
        self.avd_manager_path: str = RequiredFeature('avd_manager_path').request()
        self.avd_names: List[str] = []

    def get_avd_name_for_emulator_port(self, port: int) -> str:
        avd_index = int((port - 5554) / 2)
        # FIX: this is not always the case
        return f"{self.avd_series}_{str(avd_index)}"

    def refresh_avd_names(self) -> None:
        list_avd_cmd = f"{self.avd_manager_path} list avd"
        try:
            output, errors, result_code = run_cmd(list_avd_cmd)
        except TimeoutExpired as e:
            stack_trace = traceback.format_exc()
            raise Exception(f"There was an error running avdmanager command: {str(stack_trace)}")

        if errors.strip() != '':
            raise Exception(f"There was an error running avdmanager command: {errors}")

        lines = output.split("\n")
        for line in lines:
            if "Name: " in line:
                avd_name = line.split(':')[1].strip()
                self.avd_names.append(avd_name)

    def avd_name_exists(self, avd_name: str) -> bool:
        if len(self.avd_names) == 0:
            self.refresh_avd_names()

        return avd_name in self.avd_names
