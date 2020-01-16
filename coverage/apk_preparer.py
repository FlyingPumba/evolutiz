import time

from application.apk_analyser import ApkAnalyser
from coverage.emma.emma_app_instrumentator import EmmaAppInstrumentator
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device


class ApkPreparer(object):
    def __init__(self) -> None:
        self.app_instrumentator = RequiredFeature('app_instrumentator').request()
        self.apk_analyser = ApkAnalyser()

    def prepare(self) -> None:
        self.app_instrumentator.instrument()
        self.apk_analyser.analyse()

    def install_on_device(self, device: Device) -> None:
        package_name: str = RequiredFeature('package_name').request()
        apk_path: str = RequiredFeature('apk_path').request()

        successful = False
        for i in range(3):
            try:
                self.apk_analyser.upload_string_xml(device)
                adb.shell_command(device, "rm /mnt/sdcard/bugreport.crash")

                adb.uninstall(device, package_name)
                adb.install(device, package_name, apk_path)

                successful = self.app_instrumentator.instrument_device(device)
                if successful:
                    break
            except Exception as e:
                print(e)
                time.sleep(5)

        if not successful:
            raise Exception(f"Unable to setup device: {device.name}")
