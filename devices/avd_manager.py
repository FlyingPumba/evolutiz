import traceback
from subprocess import TimeoutExpired

from dependency_injection.required_feature import RequiredFeature
from util.command import run_cmd


class AvdManager(object):

    def __init__(self):
        self.avd_series = RequiredFeature('avd_series').request()
        self.avd_manager_path = RequiredFeature('avd_manager_path').request()
        self.avd_names = []

    def get_avd_name_for_emulator_port(self, port):
        avd_index = int((port - 5554) / 2)
        return self.avd_series + "_" + str(avd_index)

    def refresh_avd_names(self):
        list_avd_cmd = self.avd_manager_path + " list avd"
        try:
            output, errors, result_code = run_cmd(list_avd_cmd)
        except TimeoutExpired as e:
            stack_trace = traceback.format_exc()
            raise Exception("There was an error running avdmanager command: " + stack_trace)

        if errors.strip() != '':
            raise Exception("There was an error running avdmanager command: " + errors)

        lines = output.split("\n")
        for line in lines:
            if "Name: " in line:
                avd_name = line.split(':')[1].strip()
                self.avd_names.append(avd_name)

    def avd_name_exists(self, avd_name):
        if len(self.avd_names) == 0:
            self.refresh_avd_names()

        return avd_name in self.avd_names
