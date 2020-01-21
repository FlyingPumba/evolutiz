import os

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger
from util.command import run_cmd


class ApkAnalyser(object):
    """This class provides functionality to discover the location of the APK.
    Once found, it also decodes de APK to extract string resources, and pushes them to the device.
    """

    def analyse(self) -> None:
        self.result_dir: str = RequiredFeature('result_dir').request()
        self.package_name: str = RequiredFeature('package_name').request()
        self.instrumented_app_path: str = RequiredFeature('instrumented_app_path').request()

        self.get_apk_path()

        print("### Working on apk:", self.package_name)
        # static analysis
        self.decoded_dir = f"{self.result_dir}/decoded-apk"
        if settings.ENABLE_STRING_SEEDING:
            logger.log_progress("\nRunning static analysis on apk")
            self.decode_apk()

    def get_apk_path(self) -> None:
        self.apk_path = None
        if self.instrumented_app_path.endswith(".apk"):
            self.apk_path = self.instrumented_app_path
        else:
            # now find its name
            output, errors, result_code = run_cmd(f"find {self.instrumented_app_path} -name *-debug.apk")
            for file_path in output.split("\n"):
                if file_path != "":
                    self.apk_path = file_path

        features.provide('apk_path', self.apk_path)
        assert self.apk_path is not None

    def decode_apk(self) -> None:
        os.system(f"java -jar {settings.WORKING_DIR}lib/apktool.jar d -f -o {self.decoded_dir} {self.apk_path}"
                  f"{logger.redirect_string()}")

    def upload_string_xml(self, device: Device) -> None:
        string_xml_path = f"{self.decoded_dir}/res/values/strings.xml"
        if settings.ENABLE_STRING_SEEDING is False or os.path.exists(string_xml_path) is False:
            # if not exist, upload dummy strings.xml
            string_xml_path = f"{settings.WORKING_DIR}resources/dummy_strings.xml"

        adb.shell_command(device, f" shell rm /mnt/sdcard/{self.package_name}_strings.xml")
        adb.push(device, string_xml_path, f"/mnt/sdcard/{self.package_name}_strings.xml")
