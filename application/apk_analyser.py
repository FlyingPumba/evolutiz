import os

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger
from util.command import run_cmd
from util.etg_config import ETGConfig


class ApkAnalyser(object):
    """This class provides functionality to discover the location of the APK.
    Once found, it also decodes de APK to extract string resources, and pushes them to the device.
    """

    def analyse(self) -> None:
        self.result_dir: str = RequiredFeature('result_dir').request()
        self.package_name: str = RequiredFeature('compiled_package_name').request()
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
            output, errors, result_code = run_cmd(f"find -L {self.instrumented_app_path} -name *.apk | grep -v androidTest | grep -v unaligned")
            apk_paths = []
            for file_path in output.split("\n"):
                if file_path != "":
                    apk_paths.append(file_path)

            if len(apk_paths) == 0:
                raise Exception(f"No APKs found inside folder {self.instrumented_app_path} after build.")

            if len(apk_paths) > 1:
                # try to filter APKs based on ETG.config file (might not be present)
                etg_config_path = f"{self.instrumented_app_path}/etg.config"
                if os.path.isfile(etg_config_path):
                    etg_config = ETGConfig(etg_config_path)

                    # try to filter by build type
                    build_type = etg_config.build_type()
                    apk_paths = list(filter(lambda path: f"/{build_type}/" in path, apk_paths))

                    # try to filter by product flavors
                    product_flavors = etg_config.product_flavors()
                    if len(product_flavors) > 0:
                        product_flavors_combined = ''
                        for index, flavor in enumerate(product_flavors):
                            if index == 0:
                                product_flavors_combined += flavor.lower()
                            else:
                                product_flavors_combined += flavor.capitalize()

                        apk_paths = list(filter(lambda path: f"/{product_flavors_combined}/" in path, apk_paths))

                        if len(apk_paths) == 0:
                            raise Exception(f"Evolutiz was unable to determine which APK inside folder "
                                            f"{self.instrumented_app_path} should it use, since neither of them satisfy the "
                                            f"combined product flavor provided: {product_flavors_combined} in the ETG config "
                                            f"file")
                else:
                    # TODO: provide more info about ETG config files
                    raise Exception(f"There are several APKs found inside folder {self.instrumented_app_path} after "
                                    f"build. Evolutiz was unable to determine which one should it use. "
                                    f"You can help it by providing an ETG config file at the root of the app's folder.")

            self.apk_path = apk_paths[0]

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

        adb.shell_command(device, f" rm /mnt/sdcard/{self.package_name}_strings.xml")
        adb.push(device, string_xml_path, f"/mnt/sdcard/{self.package_name}_strings.xml")
