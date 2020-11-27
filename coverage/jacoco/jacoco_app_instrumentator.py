import os
from pathlib import Path

import settings
from coverage.emma.emma_app_instrumentator import EmmaAppInstrumentator
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices.device import Device
from util import logger
from util.command import run_cmd
from util.etg_config import ETGConfig


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

            self.prepare_files_for_coverage()
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

        self.prepare_files_for_coverage()

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

        if add_jacoco_plugin:
            self.add_jacoco_plugin_to_gradle_file(build_gradle_path, package_name)

        if enable_test_coverage:
            self.enable_test_coverage_in_gradle_file(build_gradle_path, package_name)

        self.provide_compiled_package_name(build_gradle_path, package_name)

    def add_jacoco_plugin_to_gradle_file(self, build_gradle_path, package_name) -> None:
        """
        This method takes care of adding jacoco plugin.

        :param build_gradle_path:
        :param package_name:
        :return:
        """
        is_mod = False
        jacoco_one_line_config_added = False

        content = ""
        in_stream = open(build_gradle_path)
        for index, line in enumerate(in_stream):

            if jacoco_one_line_config_added:
                content += line

                if line.find("}") != -1 and jacoco_one_line_config_added:
                    # "plugins {" section is being closed, add missing jacoco configuration
                    content += \
                        """
/* ADDED for instrumentation begin */

jacoco {
toolVersion = "0.8.2"
}

/* ADDED for instrumentation end */
    """
                    jacoco_one_line_config_added = False
                continue

            if line.find("apply plugin") != -1 and line.find("com.android.application") != -1:
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
            elif line.find("id") != -1 and line.find("com.android.application") != -1:
                content += line
                content += """
/* ADDED for instrumentation begin */
id 'jacoco'
/* ADDED for instrumentation end */
        """
                jacoco_one_line_config_added = True
                is_mod = True
            else:
                content += line

        in_stream.close()
        os.remove(build_gradle_path)
        new_file = open(build_gradle_path, "w")
        new_file.write(content)
        new_file.close()

        if not is_mod:
            print(f"[Error] Failed to add jacoco plugin to build.gradle file {build_gradle_path}")

    def enable_test_coverage_in_gradle_file(self, build_gradle_path, package_name) -> None:
        """
        This method takes care of enabling test coverage in the build.gradle file.

        :param build_gradle_path:
        :param package_name:
        :return:
        """

        add_debug_section = False
        output, errors, result_code = run_cmd(f"cat {build_gradle_path} | grep \"debug {{\"")
        if output.strip() == "":
            add_debug_section = True

        add_build_type_section = False
        output, errors, result_code = run_cmd(f"cat {build_gradle_path} | grep \"buildTypes {{\"")
        if output.strip() == "":
            add_build_type_section = True

        is_mod = False

        content = ""
        in_stream = open(build_gradle_path)
        for index, line in enumerate(in_stream):
            if add_build_type_section:
                if line.find("android {") != -1:
                    content += line
                    content += \
                        """
/* ADDED test coverage enabled for instrumentation begin */
buildTypes {
    debug {
                testCoverageEnabled = true
    }
}
/* ADDED test coverage enabled for instrumentation end */
"""
                    is_mod = True
                else:
                    content += line
            elif add_debug_section:
                if line.find("buildTypes {") != -1:
                    content += line
                    content += \
                        """
/* ADDED test coverage enabled for instrumentation begin */
debug {
            testCoverageEnabled = true
}
/* ADDED test coverage enabled for instrumentation end */
"""
                    is_mod = True
                else:
                    content += line
            else:
                if line.find("debug {") != -1:
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
            print(f"[Error] Failed to enable test coverage in build.gradle file {build_gradle_path}")

    def provide_compiled_package_name(self, build_gradle_path, package_name) -> None:
        """
        This method takes care of parsing the "debug" build type config, in search of applicationIdSuffix properties
        that might change the package name once installed in the emulator.

        If the application has an ETG config file, it will use that instead.

        :param build_gradle_path:
        :param package_name:
        :return:
        """
        etg_config_path = "etg.config"
        if os.path.isfile(etg_config_path):
            etg_config = ETGConfig(etg_config_path)
            features.provide('compiled_package_name', etg_config.compiled_package_name())
            return

        suffix_found = False

        in_stream = open(build_gradle_path)
        for index, line in enumerate(in_stream):

            if line.find("applicationIdSuffix = \"") != -1:
                suffix: str = line.split("applicationIdSuffix = \"")[1]
                suffix = suffix.strip("\"\n")
                suffix_found = True
                features.provide('compiled_package_name', f"{package_name}{suffix}")
                break

        in_stream.close()

        if not suffix_found:
            # assume same compiled package name as the one declard in AndroidManifest.xml file
            features.provide('compiled_package_name', package_name)


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


    def prepare_files_for_coverage(self):
        instrumented_app_path = RequiredFeature('instrumented_app_path').request()
        self.jacoco_coverage_class_files_path = f"{settings.WORKING_DIR}{instrumented_app_path}/classes"
        features.provide('jacoco_coverage_class_files_path', self.jacoco_coverage_class_files_path)

        etg_config_path = f"{instrumented_app_path}/etg.config"
        if not os.path.isfile(etg_config_path):
            raise Exception("Unable to find ETG config for JaCoCo coverage fetcher")

        etg_config = ETGConfig(etg_config_path)

        package_name_path = '/'.join(etg_config.package_name().split('.'))

        app_folder_path = etg_config.get_application_folder_path()

        # do we have a .jar
        output, errors, result_code = run_cmd(f"find {app_folder_path} -name \"*.jar\" -type f | grep app_classes")
        app_classes_jar_path = output.strip("\n")
        if app_classes_jar_path != "":
            # unzip this jar so it is taken into account in next find command
            unzip_folder = f"{os.path.dirname(app_classes_jar_path)}/unzipped"
            run_cmd(f"rm -r {unzip_folder}")
            run_cmd(f"unzip {app_classes_jar_path} -d {unzip_folder}")

        # find out all .class files
        output, errors, result_code = run_cmd(f"find {app_folder_path} -name \"*.class\" -type f")
        class_files = output.split("\n")

        # figure out build variant
        build_variant = "debug"
        build_variant_path = build_variant
        product_flavors = etg_config.product_flavors()
        if len(product_flavors) > 0:
            product_flavors_combined = ""
            product_flavors_combined_path = ""
            for i, flavor in enumerate(product_flavors):
                if i == 0:
                    product_flavors_combined += flavor.lower()
                else:
                    product_flavors_combined += flavor.capitalize()

                product_flavors_combined_path += "/"
                product_flavors_combined_path += flavor.lower()

            build_variant = product_flavors_combined + etg_config.build_type().capitalize()
            build_variant_path = product_flavors_combined_path + "/" + etg_config.build_type() + "/"

            if package_name_path.endswith(product_flavors_combined):
                package_name_path = package_name_path.split("/" + product_flavors_combined)[0]

        filtered_class_files = []
        for class_file in class_files:
            if (build_variant in class_file or build_variant_path in class_file) \
                    and not "AndroidTest" in class_file \
                    and not "androidTest" in class_file \
                    and not "UnitTest" in class_file \
                    and not "R$" in class_file \
                    and not "R.class" in class_file \
                    and not "BuildConfig.class" in class_file \
                    and not "/EmmaInstrument/" in class_file \
                    and not "/jacoco_instrumented_classes/" in class_file \
                    and not "/jacoco/" in class_file \
                    and not "/transforms/" in class_file \
                    and not "/kapt3/" in class_file:
                filtered_class_files.append(class_file)

        run_cmd(f"mkdir -p {self.jacoco_coverage_class_files_path}")

        # Proceed to find the root folder where package name start for each class file. For example:
        # 'subjects/com.a42crash.iarcuschin.a42crash/app/build/tmp/kotlin-classes/debug/com/a42crash/iarcuschin/a42crash/MainActivity.class'
        # -> We should copy from the /debug/ folder onwards
        # 'subjects/com.a42crash.iarcuschin.a42crash/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes/com/a42crash/iarcuschin/a42crash/MainActivity_ViewBinding.class'
        # -> We should copy from the /classes/ folder onwards

        class_folders = []
        for class_file in filtered_class_files:
            should_inspect = True
            for folder in class_folders:
                if class_file.startswith(folder):
                    should_inspect = False
                    break
            if not should_inspect:
                # we already have the folder for this class file
                continue

            aux = Path(class_file).parent
            class_file_folder = str(aux.absolute())
            while not class_file_folder.endswith("subjects"):
                if class_file_folder.endswith(package_name_path):
                    class_folder = class_file_folder.split(package_name_path)[0]

                    if not class_folder in class_folders:
                        class_folders.append(class_folder)

                    break
                else:
                    aux = aux.parent
                    class_file_folder = str(aux.absolute())

                    if "/" == class_file_folder:
                        break

        # copy class folders with their directory structure
        run_cmd(f"mkdir -p {self.jacoco_coverage_class_files_path}/{package_name_path}")
        for class_folder in class_folders:
            run_cmd("rsync -a --prune-empty-dirs --exclude=\"*EmmaInstrument*/\" " +
                    "--exclude=\"*AndroidTest*/\" --exclude=\"*UnitTest*/\" --exclude=\"*kapt3*/\" " +
                    "--exclude=\"*jacoco_instrumented_classes*/\" --exclude=\"R\\$*.class\" " +
                    "--exclude=\"*jacoco*/\" --exclude=\"*androidTest*/\" --exclude=\"*transforms*/\" " +
                    "--exclude=\"BuildConfig.class\" --exclude=\"R.class\" --include=\"*.class\" " +
                    "--include=\"*/\" --exclude=\"*\" " +
                    f"{class_folder}/{package_name_path}/ {self.jacoco_coverage_class_files_path}/{package_name_path}")

        # # Find and copy source root folder
        # app_folder = Path(class_folders[0]).parent
        # while True:
        #     # File[] files = app_folder.listFiles();
        #     files = app_folder.glob('**/*')
        #
        #     found = False
        #     if files != None:
        #         for file in files:
        #             if "src" == file.name:
        #                 app_folder = file
        #                 found = True
        #                 break
        #
        #     if found:
        #         break
        #
        #     app_folder = app_folder.parent
        #     if app_folder.absolute().endswith(etg_config.package_name()):
        #         raise Exception(f"Unable to find source root folder for package name " + etg_config.package_name())
        #
        # run_cmd(f"cp -r  {app_folder.absolute()}/main/java {self.jacoco_coverage_class_files_path}")
        #
        # main_folder = Path(app_folder.absolute() + "/main")
        # main_files = main_folder.glob('**/*')
        # if main_files != None and "kotlin" in main_files:
        #     run_cmd("cp -r  {source_root}/main/kotlin {mate_server_src_folder_path}")
