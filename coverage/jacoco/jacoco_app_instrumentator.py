import os

import settings
from coverage.emma.emma_app_instrumentator import EmmaAppInstrumentator
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices.device import Device
from util import logger
from util.command import run_cmd


class JacocoAppInstrumentator(EmmaAppInstrumentator):

    def instrument_device(self, device: Device) -> bool:
        # do nothing
        return True

    def instrument(self) -> None:
        self.app_path: str = RequiredFeature('app_path').request()
        self.result_dir: str = RequiredFeature('result_dir').request()
        self.instrumented_subjects_path: str = RequiredFeature('instrumented_subjects_path').request()
        self.emma_instrument_path: str = RequiredFeature('emma_instrument_path').request()

        # first, check if we should assume apps are already instrumented
        assume_subjects_instrumented = RequiredFeature('assume_subjects_instrumented').request()
        if assume_subjects_instrumented:
            features.provide('instrumented_app_path', self.app_path)

            output, errors, result_code = run_cmd(f"aapt dump badging {self.app_path} | grep package:\\ name")
            package_name = output.split("package: name=\'")[1].split("\'")[0]
            features.provide('package_name', package_name)
            return

        logger.log_progress(f"\nInstrumenting app: {os.path.basename(self.app_path)}")

        # copy sources and instrument application
        instrumented_app_path, package_name = self.prepare_app_for_instrumentation()

        features.provide('package_name', package_name)
        features.provide('instrumented_app_path', instrumented_app_path)

        self.instrument_gradle_file(instrumented_app_path, package_name)

        result_code = os.system(f"./gradlew assembleDebug 2>&1 >{self.result_dir}/build.log")
        if result_code != 0:
            raise Exception("Unable run assembleDebug")

        os.chdir(settings.WORKING_DIR)

    def instrument_gradle_file(self, instrumented_app_path, package_name):
        build_gradle_path = self.find_build_gradle_path(instrumented_app_path)

        # check which changes need to be made to the build.gradle file
        add_jacoco_plugin = False
        enable_test_coverage = False
        output, errors, result_code = run_cmd(f"cat {build_gradle_path} | grep \"apply plugin: 'jacoco'\"")
        if output.strip() == "":
            add_jacoco_plugin = True

        output, errors, result_code = run_cmd(f"cat {build_gradle_path} | grep \"testCoverageEnabled = true\"")
        if output.strip() == "":
            enable_test_coverage = True

        self.modify_gradle_file_if_needed(build_gradle_path, package_name, add_jacoco_plugin, enable_test_coverage)

    def modify_gradle_file_if_needed(self, build_gradle_path, package_name, add_jacoco_plugin, enable_test_coverage) -> None:
        """
        This method takes care of adding jacoco plugin and enabling test coverage to the build.gradle file if required.
        It also parses the "debug" build type config, in search of applicationIdSuffix properties that might change the
        package name once installed in the emulator.

        :param build_gradle_path:
        :param add_jacoco_plugin:
        :param enable_test_coverage:
        :return:
        """
        is_mod = False
        suffix_found = False

        content = ""
        in_stream = open(build_gradle_path)
        for index, line in enumerate(in_stream):
            if line.find("com.android.application") != -1 and add_jacoco_plugin:
                content += line
                content += \
                            """
/* ADDED for instrumentation begin */
apply plugin: 'jacoco'


jacoco {
    toolVersion = "0.8.2"
}

/* ADDED for instrumentation end */
        """
                is_mod = True
            elif line.find("debug {") != -1 and enable_test_coverage:
                content += line
                content += \
                    """
/* ADDED test coverage enabled for instrumentation begin */
            testCoverageEnabled = true
/* ADDED test coverage enabled for instrumentation end */
"""
                is_mod = True
            else:
                content += line

                if line.find("applicationIdSuffix = \"") != -1:
                    suffix: str = line.split("applicationIdSuffix = \"")[1]
                    suffix = suffix.strip("\"\n")
                    suffix_found = True
                    features.provide('compiled_package_name', f"{package_name}{suffix}")

        in_stream.close()
        os.remove(build_gradle_path)
        new_file = open(build_gradle_path, "w")
        new_file.write(content)
        new_file.close()

        if not suffix_found:
            # assume same compiled package name as the one declard in AndroidManifest.xml file
            features.provide('compiled_package_name', package_name)

        if not is_mod and (add_jacoco_plugin or enable_test_coverage):
            print(f"[Error] Failed to update build.gradle file {build_gradle_path}")

    def find_build_gradle_path(self, instrumented_app_path):
        find_gradle_path_cmd = f"grep -l -R \"'com.android.application'\" {settings.WORKING_DIR}{instrumented_app_path} "
        find_gradle_path_cmd += "| xargs -I {} grep -L \"com.google.android.support:wearable\" {}"
        find_gradle_path_cmd += "| xargs -I {} grep -L \"com.google.android.wearable:wearable\" {}"
        find_gradle_path_cmd += "| grep \"build.gradle$\""

        output, errors, result_code = run_cmd(find_gradle_path_cmd)
        grep_result = list(filter(lambda p: p != "", output.split("\n")))
        if len(grep_result) != 1:
            raise Exception("Unable to find build.gradle file in instrumented app path")

        return grep_result[0]
