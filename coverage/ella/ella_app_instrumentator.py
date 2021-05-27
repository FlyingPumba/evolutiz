import os
import time

import settings
from coverage.app_instrumentator import AppInstrumentator
from coverage.ella.ella import Ella
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger
from util.command import run_cmd


class EllaAppInstrumentator(AppInstrumentator, Ella):

    def __init__(self) -> None:
        super(AppInstrumentator, self).__init__()
        super(Ella, self).__init__()

    def setup_for_test_run(self, device: Device) -> None:
        self.start_ella(device)
        # wait a bit after starting ELLA server
        time.sleep(3)

    def teardown_after_test_run(self, device: Device) -> None:
        # wait a bit before stopping ELLA server, in case there is coverage left to download
        time.sleep(3)
        self.stop_ella(device)

    def instrument(self) -> None:
        self.app_path: str = RequiredFeature('app_path').request()
        self.result_dir: str = RequiredFeature('result_dir').request()
        self.instrumented_subjects_path: str = RequiredFeature('instrumented_subjects_path').request()

        if not self.app_path.endswith(".apk"):
            cause = "ELLA instrumentation only works with APKs.\n" \
                    f"Provided app path was: {self.app_path}"
            logger.log_progress(cause)
            raise Exception(cause)

        apk_filename = os.path.basename(self.app_path)
        logger.log_progress(f"\nInstrumenting app: {apk_filename}")

        # prepare the ella-customized jars
        output, errors, result_code = run_cmd(f"python gen-ella-wrappers.py", cwd=self.ella_folder_path)
        if result_code != 0:
            raise Exception("Unable generate ELLA wrappers")

        output, errors, result_code = run_cmd(f"ant clean build", cwd=self.ella_folder_path)
        if result_code != 0:
            raise Exception("Unable compile ELLA JARs")

        # perform the instrumentation of the APK
        output, errors, result_code = run_cmd(f"./ella.sh i {self.app_path}", cwd=self.ella_folder_path)
        if result_code != 0:
            # TODO: maybe try with --aapt2?
            raise Exception(f"Unable instrument {self.app_path} APK using ELLA")

        os.chdir(settings.WORKING_DIR)

        # find instrumented APK in ELLA's output folder
        output, errors, result_code = run_cmd(f"find {self.get_current_apk_output_folder()} -type f -name \"*.apk\"")
        instrumented_app_path = output.rstrip('\n')
        features.provide('instrumented_app_path', instrumented_app_path)

        # get package and Main activity name directly from the original APK
        output, errors, result_code = run_cmd(f"aapt dump badging {self.app_path} | grep package:\\ name")
        package_name = output.split("package: name=\'")[1].split("\'")[0]
        features.provide('package_name', package_name)
        features.provide('compiled_package_name', package_name)

        output, errors, result_code = run_cmd(
            f"aapt dump badging {self.app_path} | grep launchable-activity | cut -d' ' -f2 | cut -d\"'\" -f 2")
        main_activity_name = output.rstrip('\n')
        features.provide('main_activity', main_activity_name)

    def instrument_device(self, device: Device) -> bool:
        # build the TCP relay Android app
        output, errors, result_code = run_cmd(f"./gradlew assembleDebug", cwd=self.tcp_relay_android)
        if result_code != 0:
            raise Exception("Unable build the TCP relay Android app")

        output, errors, result_code = run_cmd(f"find {self.tcp_relay_android} -type f -name \"*apk\"")
        if result_code != 0:
            raise Exception("Unable find the APK for the TCP relay Android app")

        tcp_relay_apk_path = output.rstrip('\n')

        adb.uninstall(device, self.tcp_relay_android_package_name)
        adb.install(device, self.tcp_relay_android_package_name, tcp_relay_apk_path)

        return True
