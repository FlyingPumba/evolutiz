import os
import subprocess as sub
import xml.etree.cElementTree as ET
from lxml import etree

import settings
from util import logger


def instrument_apk(app_path, result_dir):
    logger.log_progress("\nInstrumenting app: " + os.path.basename(app_path))

    instrumented_source_path, package_name = prepare_app_for_instrumentation(app_path, result_dir)

    result_code = os.system("ant clean emma debug 2>&1 >" + result_dir + "/build.log")
    if result_code != 0: raise Exception("Unable run ant clean emma debug")

    result_code = os.system("cp bin/coverage.em " + result_dir + "/" + logger.redirect_string())
    if result_code != 0: raise Exception("Unable to copy coverage.em file")

    p = sub.Popen("ls bin/*-debug.apk", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    apk_path, errors = p.communicate()
    apk_path = apk_path.rstrip('\n')

    os.chdir(settings.WORKING_DIR)

    return instrumented_source_path, package_name


def prepare_app_for_instrumentation(app_path, result_dir):
    # copy sources to instrumented subjects folder
    app_name = os.path.basename(app_path)
    instrumented_source_path = settings.INSTRUMENTED_SUBJECTS_PATH + app_name
    os.system("rm -rf " + instrumented_source_path)
    os.system("mkdir -p " + settings.INSTRUMENTED_SUBJECTS_PATH)
    os.system("cp -r " + app_path + " " + instrumented_source_path)

    # get package name
    package_name = get_package_name(instrumented_source_path)

    # copy emma source
    source_root = instrumented_source_path + "/src/" + "/".join(package_name.split(".")) + "/"
    if not os.path.exists(source_root):
        #  TODO: maybe we can do a better search ?
        raise Exception("Unable to find src folder of app" + app_path + " when trying to pre-instrument")

    emma_instrument_original_path = settings.SUBJECTS_PATH + "EmmaInstrument"
    os.system("cp -r " + emma_instrument_original_path + " " + source_root)

    # modify emma source
    emma_instrument_dest_path = source_root + "EmmaInstrument"
    for target in os.listdir(emma_instrument_dest_path):
        if target.endswith(".java"):
            alter_emma_file(emma_instrument_dest_path + "/" + target, package_name)

    # get & alter based on main activity based on "android.intent.action.MAIN"
    main_activity = get_main_activity(instrumented_source_path + "/")
    if main_activity.startswith("."):
        main_activity = package_name + main_activity

    # update main activity in InstrumentedActivity.java
    alter_InstrumentedActivity(emma_instrument_dest_path + "/InstrumentedActivity.java", main_activity)

    # update AndroidManifest.xml
    alter_AndroidManifest(instrumented_source_path + "/AndroidManifest.xml", package_name)

    # update project
    os.chdir(instrumented_source_path)
    # TODO: replace for a command that doesn't depend on old android-sdk-linux
    os.system(settings.WORKING_DIR + "monkey/android-sdk-linux/tools/android update project --path . --target " +
              settings.ANDROID_TARGET + " --subprojects" + logger.redirect_string())

    return instrumented_source_path, package_name


def get_main_activity(root_path):
    manifest = root_path + "AndroidManifest.xml"

    tree = etree.parse(manifest)
    root = tree.getroot()
    namespace = dict(android='http://schemas.android.com/apk/res/android')
    return root.xpath(".//intent-filter/action[@android:name='android.intent.action.MAIN']/../../@android:name",
                      namespaces=namespace)[0]


def get_package_name(root_path):
    # p = sub.Popen(
    #     settings.AAPT + " d xmltree " + apk_path + " AndroidManifest.xml | grep package | awk 'BEGIN {FS=\"\\\"\"}{print $2}'",
    #     stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    # package_name, errors = p.communicate()
    # package_name = package_name.rstrip('\n')

    manifest = root_path + "/AndroidManifest.xml"

    tree = ET.ElementTree(file=manifest)

    return tree.getroot().attrib["package"]


def alter_AndroidManifest(path, package_name):
    is_mod = False

    content = ""
    in_stream = open(path)
    for index, line in enumerate(in_stream):
        if line.find("</application>") != -1:

            content += \
                '''

                 <!-- emma updated -->
                <activity android:label="EmmaInstrumentationActivity" android:name="''' + package_name + '''.EmmaInstrument.InstrumentedActivity"/>
			<receiver android:name="''' + package_name + '''.EmmaInstrument.SMSInstrumentedReceiver">
				<intent-filter>
				<action android:name="edu.gatech.m3.emma.COLLECT_COVERAGE" />
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

    if is_mod == False:
        print "[Error] Failed when update AndroidManifest.xml"


def alter_InstrumentedActivity(path, main_activity):
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


def alter_emma_file(path, package):
    content = ""

    in_stream = open(path)

    for index, line in enumerate(in_stream):
        if index == 0:
            content += "package " + package + ".EmmaInstrument;\n"
        else:
            content += line

    in_stream.close()
    os.remove(path)
    new_file = open(path, "w")
    new_file.write(content)
    new_file.close()
