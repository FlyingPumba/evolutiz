from application.apk_analyser import ApkAnalyser
from application.app_instrumentator import AppInstrumentator
from dependency_injection.required_feature import RequiredFeature
from devices import adb


class ApkPreparer(object):
    def __init__(self):
        self.app_instrumentator = AppInstrumentator()
        self.apk_analyser = ApkAnalyser()

    def prepare(self):
        self.app_instrumentator.instrument()
        self.apk_analyser.analyse()

    def install_on_device(self, device):
        package_name = RequiredFeature('package_name').request()
        apk_path = RequiredFeature('apk_path').request()

        self.apk_analyser.upload_string_xml(device)
        adb.shell_command(device, "rm /mnt/sdcard/bugreport.crash")

        adb.uninstall(device, package_name)
        adb.install(device, package_name, apk_path)

        instrumentation_cmd = "am instrument " + package_name + "/" + package_name + ".EmmaInstrument.EmmaInstrumentation"
        output, errors, result_code = adb.shell_command(device, instrumentation_cmd, retry=2)
        if result_code != 0:
            raise Exception("Unable to instrument " + package_name)
