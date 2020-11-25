from pathlib import Path
from configparser import ConfigParser, DEFAULTSECT
from typing import List

from util.command import run_cmd


class ETGConfig(object):

    def __init__(self, etg_config_file):
        with open(etg_config_file, 'r') as f:
            config_string = f"[{DEFAULTSECT}]\n" + f.read()
        config = ConfigParser()
        config.read_string(config_string)
        self.etg_config = dict(config.items(DEFAULTSECT))

    def json_path(self):
        return self.etg_config['jsonpath']

    def package_name(self):
        return self.etg_config['packagename']

    def test_package_name(self):
        if 'testpackagename' in self.etg_config:
            return self.etg_config['testpackagename']
        else:
            return self.etg_config['packagename']

    def compiled_package_name(self):
        if 'compiledpackagename' in self.etg_config:
            return self.etg_config['compiledpackagename']
        else:
            return self.etg_config['packagename']

    def compiled_test_package_name(self):
        if 'compiledtestpackagename' in self.etg_config:
            return self.etg_config['compiledtestpackagename']
        else:
            return self.etg_config['packagename'] + '.test'

    def build_type(self):
        if 'buildtype' in self.etg_config:
            return self.etg_config['buildtype']
        else:
            return 'debug'

    def product_flavors(self) -> List[str]:
        if 'productflavors' in self.etg_config:
            return self.etg_config['productflavors'].split(',')
        else:
            return []

    def root_project_path(self):
        return self.etg_config['rootprojectpath']

    def get_output_path(self):
        return self.etg_config['getoutputpath']

    def get_application_folder_path(self):
        grep_cmd = f"grep -l -R \"'com.android.application'\" {self.root_project_path()} "
        grep_cmd += "| xargs -I {} grep -L \"com.google.android.support:wearable\" {}"
        grep_cmd += "| xargs -I {} grep -L \"com.google.android.wearable:wearable\" {}"
        grep_cmd += "| grep \"build.gradle$\""

        output, errors, result_code = run_cmd(grep_cmd)
        grep_result = output.strip("\n")

        if grep_result == "":
            raise Exception("Unable to find application path inside project.")

        return str(Path(grep_result).parent) + "/"
