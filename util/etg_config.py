from configparser import ConfigParser, DEFAULTSECT


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

    def product_flavors(self):
        if 'productflavors' in self.etg_config:
            return self.etg_config['productflavors'].split(',')
        else:
            return []

    def root_project_path(self):
        return self.etg_config['rootprojectpath']

    def get_output_path(self):
        return self.etg_config['getoutputpath']
