import os

from coverage.emma.emma_app_instrumentator import EmmaAppInstrumentator
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.command import run_cmd


class JacocoAppInstrumentator(EmmaAppInstrumentator):

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

        self.instrument_gradle_file(instrumented_app_path)

        features.provide('package_name', package_name)
        features.provide('instrumented_app_path', instrumented_app_path)

    def instrument_gradle_file(self, instrumented_app_path):
        build_gradle_path = self.find_build_gradle_path(instrumented_app_path)

        # check which changes need to be made to the build.gradle file
        add_jacoco_plugin = False
        enable_test_coverage = False
        output, errors, result_code = run_cmd(f"cat {build_gradle_path} | grep jacoco")
        if output.strip() == "":
            add_jacoco_plugin = True

        output, errors, result_code = run_cmd(f"cat {build_gradle_path} | grep \"testCoverageEnabled = true\"")
        if output.strip() == "":
            enable_test_coverage = True

        self.modify_gradle_file_if_needed(build_gradle_path, add_jacoco_plugin, enable_test_coverage)

    def modify_gradle_file_if_needed(self, build_gradle_path, add_jacoco_plugin, enable_test_coverage) -> None:
        if not add_jacoco_plugin and not enable_test_coverage:
            # nothing to do here
            return

        is_mod = False

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
            elif line.find("debug") != -1 and enable_test_coverage:
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

        in_stream.close()
        os.remove(build_gradle_path)
        new_file = open(build_gradle_path, "w")
        new_file.write(content)
        new_file.close()

        if not is_mod:
            print(f"[Error] Failed to update build.gradle file {build_gradle_path}")

    def find_build_gradle_path(self, instrumented_app_path):
        find_gradle_path_cmd = f"grep -l -R \"'com.android.application'\" {instrumented_app_path} "
        find_gradle_path_cmd += "| xargs -I {} grep -L \"com.google.android.support:wearable\" {}"
        find_gradle_path_cmd += "| xargs -I {} grep -L \"com.google.android.wearable:wearable\" {}"
        find_gradle_path_cmd += "| grep \"build.gradle$\""

        output, errors, result_code = run_cmd(find_gradle_path_cmd)
        grep_result = output.split("\n")
        if len(grep_result) != 1:
            raise Exception("Unable to find build.gradle file in instrumented app path")

        return grep_result[0]
