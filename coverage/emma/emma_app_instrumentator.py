import os
import shutil
import xml.etree.cElementTree as ET
from typing import Tuple

from lxml import etree
from lxml.etree import _ElementUnicodeResult

import settings
from coverage.app_instrumentator import AppInstrumentator
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger
from util.command import run_cmd


class EmmaAppInstrumentator(AppInstrumentator):

    def instrument_device(self, device: Device) -> bool:
        package_name = RequiredFeature('compiled_package_name').request()
        instrumentation_cmd = f"am instrument {package_name}/{package_name}.EmmaInstrument.EmmaInstrumentation"
        output, errors, result_code = adb.shell_command(device, instrumentation_cmd)
        if result_code != 0:
            raise Exception(f"Unable to instrument {package_name}")
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

            # copy emma generated file
            result_code = os.system(f"cp bin/coverage.em {self.result_dir}/{logger.redirect_string()}")
            if result_code != 0:
                raise Exception("Unable to copy coverage.em file")

            output, errors, result_code = run_cmd(f"aapt dump badging {self.app_path} | grep package:\\ name")
            package_name = output.split("package: name=\'")[1].split("\'")[0]
            features.provide('package_name', package_name)
            features.provide('compiled_package_name', package_name)
            return

        logger.log_progress(f"\nInstrumenting app: {os.path.basename(self.app_path)}")

        # copy sources and instrument application
        instrumented_app_path, package_name = self.prepare_app_for_instrumentation()

        # compile with emma data
        result_code = os.system(f"ant clean emma debug 2>&1 >{self.result_dir}/build.log")
        if result_code != 0:
            raise Exception("Unable run ant clean emma debug")

        # copy emma generated file
        result_code = os.system(f"cp bin/coverage.em {self.result_dir}/{logger.redirect_string()}")
        if result_code != 0:
            raise Exception("Unable to copy coverage.em file")

        os.chdir(settings.WORKING_DIR)

        features.provide('package_name', package_name)
        features.provide('instrumented_app_path', instrumented_app_path)

        # assume same compiled package name as the one declard in AndroidManifest.xml file
        features.provide('compiled_package_name', package_name)

    def prepare_app_for_instrumentation(self) -> Tuple[str, str]:
        # copy sources to instrumented subjects folder
        app_name = os.path.basename(self.app_path)
        instrumented_source_path = self.instrumented_subjects_path + app_name
        os.system(f"rm -rf {instrumented_source_path}")
        os.system(f"mkdir -p {self.instrumented_subjects_path}")
        os.system(f"cp -r {self.app_path} {instrumented_source_path}")

        # get AndroidManifest path
        manifest_path = self.get_manifest_path(instrumented_source_path)

        # get package name
        package_name = self.get_package_name(manifest_path)

        # copy emma source
        source_root = self.get_source_root(manifest_path, package_name)
        os.system(f"cp -r {self.emma_instrument_path} {source_root}")

        # modify emma source
        emma_instrument_dest_path: str = f"{source_root}EmmaInstrument"
        for target in os.listdir(emma_instrument_dest_path):
            if target.endswith(".java"):
                self.alter_emma_file(f"{emma_instrument_dest_path}/{target}", package_name)

        # get & alter based on main activity based on "android.intent.action.MAIN"
        main_activity = self.get_main_activity(manifest_path)
        if main_activity.startswith("."):
            main_activity = package_name + main_activity
        elif not main_activity.startswith(package_name):
            main_activity = package_name + "." + main_activity

        # update main activity in InstrumentedActivity.java
        self.alter_InstrumentedActivity(f"{emma_instrument_dest_path}/InstrumentedActivity.java", main_activity)

        # update AndroidManifest.xml
        self.alter_AndroidManifest(manifest_path, package_name)

        self.alter_MainActivity(manifest_path, main_activity)

        # update project
        os.chdir(instrumented_source_path)
        # TODO: replace for a command that doesn't depend on old android-sdk-linux
        os.system(f"{settings.WORKING_DIR}monkey/android-sdk-linux/tools/android update project --path . "
                  f"--target {settings.ANDROID_TARGET} --subprojects{logger.redirect_string()}")

        return instrumented_source_path, package_name

    def get_source_root(self, manifest_path, package_name) -> str:
        """
        Finds the root of the source code. In other words, if package name is a.b.c, returns the path to "c" folder,
        which contains the first source files.

        There are two possibilities here.
        - Old layout: there's a "src" folder alongside the AndroidManifest.xml file
        - New layout: there's a "java" folder alongside the AndroidManifest.xml file
        In the new layout, this folder might be missing (i.e., in case where the application uses only kotlin).
        In that case, it's okey to create such folder with the appropiate structure.

        To make things even more fun, the folder estructure of package a.b.c can either be: 3 nested folders "a/b/c" or
        a single folder named "a.b.c".

        :param manifest_path:
        :param instrumented_source_path:
        :param package_name:
        :return:
        """
        manifest_folder = os.path.dirname(manifest_path)
        source_root = ""

        if os.path.exists(f"{manifest_folder}/src"):
            # old layout case
            source_root = f"{manifest_folder}/src/{'/'.join(package_name.split('.'))}/"
            source_root_variant = f"{manifest_folder}/src/{package_name}/"
            if not os.path.exists(source_root):
                if os.path.exists(source_root_variant):
                    source_root = source_root_variant
                else:
                    raise Exception(f"Unable to find source folder of app {package_name} in manifest folder {manifest_folder}"
                                    f" when trying to pre-instrument. "
                                    f"This folder should be in path: {source_root} or {source_root_variant}")
        elif os.path.exists(f"{manifest_folder}/java"):
            # new layout case
            source_root = f"{manifest_folder}/java/{'/'.join(package_name.split('.'))}/"
            source_root_variant = f"{manifest_folder}/java/{package_name}/"
            if not os.path.exists(source_root):
                if os.path.exists(source_root_variant):
                    source_root = source_root_variant
                else:
                    raise Exception(f"Unable to find source folder of app {package_name} in manifest folder {manifest_folder}"
                                    f" when trying to pre-instrument. "
                                    f"This folder should be in path: {source_root} or {source_root_variant}")
        elif os.path.exists(f"{manifest_folder}/kotlin"):
            # new layout case, but app uses only kotlin
            # create java structure
            source_root = f"{manifest_folder}/java/{'/'.join(package_name.split('.'))}/"
            os.system(f"mkdir -p {source_root}")
        else:
            raise Exception(f"Unable to find source folder of app {package_name} in manifest folder {manifest_folder}"
                            f" when trying to pre-instrument. ")

        return source_root

    def get_main_activity(self, manifest_path: str) -> _ElementUnicodeResult:
        tree = etree.parse(manifest_path)
        root = tree.getroot()
        namespace = dict(android='http://schemas.android.com/apk/res/android')
        return root.xpath(".//intent-filter/action[@android:name='android.intent.action.MAIN']/../../@android:name",
                          namespaces=namespace)[0]

    def get_package_name(self, manifest_path: str) -> str:
        tree = ET.ElementTree(file=manifest_path)
        return tree.getroot().attrib["package"]

    def get_manifest_path(self, root_path: str) -> str:
        find_manifest_cmd = f"find -L {root_path} -type f -name AndroidManifest.xml | "  # find all manifests
        find_manifest_cmd += "xargs -I {} grep -l \"android.intent.action.MAIN\" {} | "  # which contain a Main Activity
        find_manifest_cmd += "xargs -I {} grep -L wearable {} | "  # and are not a wearable app
        find_manifest_cmd += "grep -v build | grep -v androidTest"  # also, discard build and test related manifests

        output, errors, result_code = run_cmd(find_manifest_cmd)
        files = list(filter(lambda p: p != "", output.split("\n")))

        if len(files) != 1:
            raise Exception("Unable to find AndroidManifest.xml file for instrumentation")
        return files[0]

    def alter_AndroidManifest(self, path: str, package_name: str) -> None:
        is_mod = False

        content = ""
        in_stream = open(path)
        for index, line in enumerate(in_stream):
            if line.find("</application>") != -1:

                content += \
                    f'''
    
                     <!-- emma updated -->
                    <activity 
                        android:label="EmmaInstrumentationActivity" 
                        android:name="{package_name}.EmmaInstrument.InstrumentedActivity"/>
                    <receiver android:name="{package_name}.EmmaInstrument.CollectCoverageReceiver">
                    <intent-filter>
                        <action android:name="evolutiz.emma.COLLECT_COVERAGE" />
                    </intent-filter>
                </receiver>
                 <!-- emma updated -->
                {line}
                
                 <!-- emma updated -->
                 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>  
                 <instrumentation 
                    android:handleProfiling="true" 
                    android:label="EmmaInstrumentation" 
                    android:name="{package_name}.EmmaInstrument.EmmaInstrumentation" 
                    android:targetPackage="{package_name}"/>
                 <!-- emma updated -->
    
                '''
                is_mod = True
            else:
                content += line

        in_stream.close()
        os.remove(path)
        new_file = open(path, "w")
        new_file.write(content)
        new_file.close()

        if not is_mod:
            print("[Error] Failed when update AndroidManifest.xml")

    def alter_InstrumentedActivity(self, path: str, main_activity: _ElementUnicodeResult) -> None:
        content = ""

        in_stream = open(path)

        for index, line in enumerate(in_stream):
            if line.find("public class InstrumentedActivity extends pkg.name.MainActivity {") != -1:
                content += "public class InstrumentedActivity extends " + main_activity + " {\n"
            else:
                content += line

        in_stream.close()
        os.remove(path)
        new_file = open(path, "w")
        new_file.write(content)
        new_file.close()

    def alter_emma_file(self, path: str, package: str) -> None:
        with open(path) as reading_file:
            # read first line of file to advance position.
            line_to_discard = reading_file.readline()

            with open(path, 'w') as writing_file:
                # write new package name
                writing_file.write(f"package {package}.EmmaInstrument;\n")
                shutil.copyfileobj(reading_file, writing_file)

    def alter_MainActivity(self, manifest_path, main_activity) -> None:
        """
        Take care of changing MainActivity if it is written in Kotlin and is not marked as open.

        :param source_root:
        :param main_activity:
        :return:
        """
        manifest_folder = os.path.dirname(manifest_path)
        main_acivity_name = main_activity.split(".")[-1]
        output, errors, result_code = run_cmd(f"find -L {manifest_folder} -name {main_acivity_name}.*")

        main_activity_path = output.strip("\n")
        if ".kt" not in main_activity_path:
            # nothing to do here
            return

        content = ""

        in_stream = open(main_activity_path)

        for index, line in enumerate(in_stream):
            if line.find(f"class {main_acivity_name}") != -1 and line.find(f"open ") == -1:
                # append "open" modifier at begging of line
                content += f"open {line}"
            else:
                content += line

        in_stream.close()
        os.remove(main_activity_path)
        new_file = open(main_activity_path, "w")
        new_file.write(content)
        new_file.close()
