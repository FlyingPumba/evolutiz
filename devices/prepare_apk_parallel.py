import multiprocessing as mp
import os
import subprocess

import settings
from analysers import static_analyser


def push_apk_and_string_xml(device, decoded_dir, package_name, apk_path):
    static_analyser.upload_string_xml(device, decoded_dir, package_name)

    print "### Installing apk:", apk_path
    os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " shell rm /mnt/sdcard/bugreport.crash")
    os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " uninstall " + package_name)
    os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " install " + apk_path)


def prepare_apk(devices, instrumented_app_dir):
    package_name, apk_path = get_package_name(instrumented_app_dir)
    # for css subjects
    if instrumented_app_dir.endswith(".apk"):
        instrumented_app_dir += "_output"
        os.system("mkdir -p " + instrumented_app_dir)
    print "### Working on apk:", package_name
    # static analysis
    if settings.ENABLE_STRING_SEEDING:
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

    pool = mp.Pool(processes=len(devices))

    for device in devices:
        pool.apply_async(push_apk_and_string_xml,
                         args=(device, decoded_dir, package_name, apk_path))

    pool.close()
    pool.join()  # intermediate should be in app folder

    os.system("rm -rf " + instrumented_app_dir + "/intermediate")
    os.system("mkdir -p " + instrumented_app_dir + "/intermediate")
    os.system("rm -rf " + instrumented_app_dir + "/crashes")
    os.system("mkdir -p " + instrumented_app_dir + "/crashes")
    os.system("rm -rf " + instrumented_app_dir + "/coverages")
    os.system("mkdir -p " + instrumented_app_dir + "/coverages")
    return package_name


def get_package_name(path):
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

    get_package_cmd = "$ANDROID_HOME/build-tools/26.0.1/aapt d xmltree " + apk_path + " AndroidManifest.xml | grep package= | awk 'BEGIN {FS=\"\\\"\"}{print $2}'"
    package_name = subprocess.Popen(get_package_cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    return package_name, apk_path
