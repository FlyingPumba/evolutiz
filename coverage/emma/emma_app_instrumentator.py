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

    def instrument_device(self, device: Device):
        instrumentation_cmd = f"am instrument {package_name}/{package_name}.EmmaInstrument.EmmaInstrumentation"
        output, errors, result_code = adb.shell_command(device, instrumentation_cmd)
        if result_code != 0:
            raise Exception(f"Unable to instrument {package_name}")

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
        source_root: str = f"{instrumented_source_path}/src/{'/'.join(package_name.split('.'))}/"
        if not os.path.exists(source_root):
            #  TODO: maybe we can do a better search ?
            raise Exception(f"Unable to find src folder of app {self.app_path} when trying to pre-instrument. "
                            f"This folder should be in path: {source_root}")

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

        # update project
        os.chdir(instrumented_source_path)
        # TODO: replace for a command that doesn't depend on old android-sdk-linux
        os.system(f"{settings.WORKING_DIR}monkey/android-sdk-linux/tools/android update project --path . "
                  f"--target {settings.ANDROID_TARGET} --subprojects{logger.redirect_string()}")

        return instrumented_source_path, package_name

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
        output, errors, result_code = run_cmd(f"find {root_path} -type f -name AndroidManifest.xml | grep -v build")
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
