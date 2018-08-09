from dependency_injection.required_feature import RequiredFeature
import subprocess as sub

class AvdManager(object):

    def __init__(self):
        self.avd_series = RequiredFeature('avd_series').request()
        self.avdmanager_path = RequiredFeature('avdmanager_path').request()

    def get_avd_name_for_emulator_port(self, port):
        avd_index = (port - 5554) / 2
        return self.avd_series + "_" + str(avd_index)

    def avd_name_exists(self, avd_name):
        p = sub.Popen(self.avdmanager_path + " list avd", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
        output, errors = p.communicate()

        if errors.strip() != '':
            raise Exception("There was an error running avdmanager command: " + errors)

        found = False
        lines = output.split("\n")
        for line in lines:
            if "Name: " + avd_name in line:
                found = True
                break

        return found