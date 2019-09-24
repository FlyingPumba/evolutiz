import os
import shutil
import xml.etree.cElementTree as ET

from lxml import etree

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.command import run_cmd


class AppInstrumentator(object):

    def instrument(self) -> None:
        self.app_path = RequiredFeature('app_path').request()
        self.result_dir = RequiredFeature('result_dir').request()
        self.instrumented_subjects_path = RequiredFeature('instrumented_subjects_path').request()
        self.emma_instrument_path = RequiredFeature('emma_instrument_path').request()

        # first, check if we should assume apps are already instrumented
        assume_subjects_instrumented = RequiredFeature('assume_subjects_instrumented').request()
        if assume_subjects_instrumented:
            features.provide('instrumented_app_path', self.app_path)

            output, errors, result_code = run_cmd("aapt dump badging " + self.app_path + " | grep package:\ name")
            package_name = output.split("package: name=\'")[1].split("\'")[0]
            features.provide('package_name', package_name)
            return

        logger.log_progress("\nInstrumenting app: " + os.path.basename(self.app_path))

        # copy sources and instrument application
        instrumented_app_path, package_name = self.prepare_app_for_instrumentation()

        # compile with emma data
        result_code = os.system("ant clean emma debug 2>&1 >" + self.result_dir + "/build.log")
        if result_code != 0:
            raise Exception("Unable run ant clean emma debug")

        # copy emma generated file
        result_code = os.system("cp bin/coverage.em " + self.result_dir + "/" + logger.redirect_string())
        if result_code != 0:
            raise Exception("Unable to copy coverage.em file")

        os.chdir(settings.WORKING_DIR)

        features.provide('package_name', package_name)
        features.provide('instrumented_app_path', instrumented_app_path)

    def prepare_app_for_instrumentation(self):
        # copy sources to instrumented subjects folder
        app_name = os.path.basename(self.app_path)
        instrumented_source_path = self.instrumented_subjects_path + app_name
        os.system("rm -rf " + instrumented_source_path)
        os.system("mkdir -p " + self.instrumented_subjects_path)
        os.system("cp -r " + self.app_path + " " + instrumented_source_path)

        # get package name
        package_name = self.get_package_name(instrumented_source_path)

        # copy emma source
        source_root = instrumented_source_path + "/src/" + "/".join(package_name.split(".")) + "/"
        if not os.path.exists(source_root):
            #  TODO: maybe we can do a better search ?
            raise Exception("Unable to find src folder of app " + self.app_path +
                            " when trying to pre-instrument. This folder should be in path: " + source_root)

        os.system("cp -r " + self.emma_instrument_path + " " + source_root)

        # modify emma source
        emma_instrument_dest_path = source_root + "EmmaInstrument"
        for target in os.listdir(emma_instrument_dest_path):
            if target.endswith(".java"):
                self.alter_emma_file(emma_instrument_dest_path + "/" + target, package_name)

        # get & alter based on main activity based on "android.intent.action.MAIN"
        main_activity = self.get_main_activity(instrumented_source_path + "/")
        if main_activity.startswith("."):
            main_activity = package_name + main_activity
        elif not main_activity.startswith(package_name):
            main_activity = package_name + "." + main_activity

        # update main activity in InstrumentedActivity.java
        self.alter_InstrumentedActivity(emma_instrument_dest_path + "/InstrumentedActivity.java", main_activity)

        # update AndroidManifest.xml
        self.alter_AndroidManifest(instrumented_source_path + "/AndroidManifest.xml", package_name)

        # update project
        os.chdir(instrumented_source_path)
        # TODO: replace for a command that doesn't depend on old android-sdk-linux
        os.system(settings.WORKING_DIR + "monkey/android-sdk-linux/tools/android update project --path . --target " +
                  settings.ANDROID_TARGET + " --subprojects" + logger.redirect_string())

        return instrumented_source_path, package_name

    def get_main_activity(self, root_path):
        manifest = root_path + "AndroidManifest.xml"

        tree = etree.parse(manifest)
        root = tree.getroot()
        namespace = dict(android='http://schemas.android.com/apk/res/android')
        return root.xpath(".//intent-filter/action[@android:name='android.intent.action.MAIN']/../../@android:name",
                          namespaces=namespace)[0]

    def get_package_name(self, root_path):
        manifest = root_path + "/AndroidManifest.xml"

        tree = ET.ElementTree(file=manifest)

        return tree.getroot().attrib["package"]

    def alter_AndroidManifest(self, path, package_name):
        is_mod = False

        content = ""
        in_stream = open(path)
        for index, line in enumerate(in_stream):
            if line.find("</application>") != -1:

                content += \
                    '''
    
                     <!-- emma updated -->
                    <activity android:label="EmmaInstrumentationActivity" android:name="''' + package_name + '''.EmmaInstrument.InstrumentedActivity"/>
                <receiver android:name="''' + package_name + '''.EmmaInstrument.CollectCoverageReceiver">
                    <intent-filter>
                    <action android:name="evolutiz.emma.COLLECT_COVERAGE" />
                    </intent-filter>
                </receiver>
                 <!-- emma updated -->
    
                ''' + line + '''
                 <!-- emma updated -->
                 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>  
                 <instrumentation android:handleProfiling="true" android:label="EmmaInstrumentation" android:name="''' + package_name + '''.EmmaInstrument.EmmaInstrumentation" android:targetPackage="''' + package_name + '''"/>
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

    def alter_InstrumentedActivity(self, path, main_activity):
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

    def alter_emma_file(self, path, package):
        with open(path) as reading_file:
            # read first line of file to advance position.
            line_to_discard = reading_file.readline()

            with open(path, 'w') as writing_file:
                # write new package name
                writing_file.write("package " + package + ".EmmaInstrument;\n")
                shutil.copyfileobj(reading_file, writing_file)
