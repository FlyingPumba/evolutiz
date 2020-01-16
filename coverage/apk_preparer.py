import time

from application.apk_analyser import ApkAnalyser
from coverage.app_instrumentator import AppInstrumentator
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device


class ApkPreparer(object):
    def __init__(self) -> None:
        self.app_instrumentator = AppInstrumentator()
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

                instrumentation_cmd = f"am instrument {package_name}/{package_name}.EmmaInstrument.EmmaInstrumentation"
                output, errors, result_code = adb.shell_command(device, instrumentation_cmd)
                if result_code != 0:
                    raise Exception(f"Unable to instrument {package_name}")

                successful = True
                break
            except Exception as e:
                print(e)
                time.sleep(5)

        if not successful:
            raise Exception(f"Unable to setup device: {device.name}")
