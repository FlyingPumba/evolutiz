import os
import subprocess
from datetime import datetime as dt

import logger
import settings
from analysers import static_analyser
from devices import adb

installed_devices = 0
total_devices = 0

def push_apk_and_string_xml(device, decoded_dir, package_name, apk_path):
    start_time = dt.now()
    static_analyser.upload_string_xml(device, decoded_dir, package_name)

    print "### Installing apk:", apk_path
    adb.shell_command(device, "rm /mnt/sdcard/bugreport.crash")
    adb.uninstall(device, package_name)
    adb.install(device, package_name, apk_path)

    # logger.log_progress("\npush_apk_and_string_xml on device " + device + " took " + str((dt.now() - start_time).seconds))

    return True, apk_path, device

def process_results(result):
    if not result[0]:
        logger.log_progress("\nInstalling apk on devices: Failed to install apk " + result[1] + " on device: " + result[2])
        return
    global installed_devices
    installed_devices += 1
    global total_devices
    #logger.log_progress("\rInstalling apk on devices: " + str(installed_devices) + "/" + str(total_devices))

def prepare_apk(devices, instrumented_app_dir, result_dir):
    package_name, apk_path = get_package_name(instrumented_app_dir)
    # for css subjects
    if instrumented_app_dir.endswith(".apk"):
        instrumented_app_dir += "_output"
        os.system("mkdir -p " + instrumented_app_dir)
    print "### Working on apk:", package_name
    # static analysis
    if settings.ENABLE_STRING_SEEDING:
        logger.log_progress("\nRunning static analysis on apk")
        output_dir = None
        if instrumented_app_dir.endswith(".apk_output"):
            output_dir = instrumented_app_dir
        else:
            output_dir = instrumented_app_dir + "/bin"
        static_analyser.decode_apk(apk_path, output_dir)

    decoded_dir = None
    if instrumented_app_dir.endswith(".apk_output"):
        decoded_dir = instrumented_app_dir + "/" + apk_path.split("/")[-1].split(".apk")[0]
    else:
        decoded_dir = instrumented_app_dir + "/bin/" + apk_path.split("/")[-1].split(".apk")[0]

    global installed_devices
    installed_devices = 0
    global total_devices
    total_devices = len(devices)

    for device in devices:
    	logger.log_progress("\nInstalling apk on device: " + adb.get_device_name(device))
        result = push_apk_and_string_xml(device, decoded_dir, package_name, apk_path)
        process_results(result)

    os.system("rm -rf " + result_dir + "/intermediate")
    os.system("mkdir -p " + result_dir + "/intermediate")
    os.system("rm -rf " + result_dir + "/crashes")
    os.system("mkdir -p " + result_dir + "/crashes")
    os.system("rm -rf " + result_dir + "/coverages")
    os.system("mkdir -p " + result_dir + "/coverages")
    return package_name, (installed_devices == total_devices)


def get_package_name(path):
    start_time = dt.now()
    apk_path = None
    if path.endswith(".apk"):
        apk_path = path
    else:
        # build the apk
        # settings.PROJECT_FOLDER = path
        # any_device.pack_and_deploy_aut()

        # now find its name
        for file_name in os.listdir(path + "/bin"):
            if file_name == "bugroid-instrumented.apk":
                apk_path = path + "/bin/bugroid-instrumented.apk"
                break
            elif file_name.endswith("-debug.apk"):
                apk_path = path + "/bin/" + file_name

    assert apk_path is not None

    get_package_cmd = "$ANDROID_HOME/build-tools/27.0.3/aapt d xmltree " + apk_path + " AndroidManifest.xml | grep package= | awk 'BEGIN {FS=\"\\\"\"}{print $2}'"
    package_name = subprocess.Popen(get_package_cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()

    # logger.log_progress("\nget_package_name took " + str((dt.now() - start_time).seconds))
    return package_name, apk_path
