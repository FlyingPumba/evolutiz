import subprocess as sub

import settings
from dependency_injection.required_feature import RequiredFeature
from util.command import run_cmd


class AvdManager(object):

    def __init__(self):
        self.avd_series = RequiredFeature('avd_series').request()
        self.avdmanager_path = RequiredFeature('avdmanager_path').request()

    def get_avd_name_for_emulator_port(self, port):
        avd_index = (port - 5554) / 2
        return self.avd_series + "_" + str(avd_index)

    def avd_name_exists(self, avd_name):
        list_avd_cmd = self.avdmanager_path + " list avd"
        cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + list_avd_cmd

        output, errors = run_cmd(cmd)

        if errors.strip() != '':
            raise Exception("There was an error running avdmanager command: " + errors)

        found = False
        lines = output.split("\n")
        for line in lines:
            if "Name: " + avd_name in line:
                found = True
                break

        return found