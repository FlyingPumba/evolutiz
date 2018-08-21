import os

import settings
from application import static_analyser
from devices import adb
from devices.mapper_on_devices import MapperOnDevices
from util import logger


def push_apk_and_string_xml(device, decoded_dir, package_name, apk_path):
    static_analyser.upload_string_xml(device, decoded_dir, package_name)
    adb.shell_command(device, "rm /mnt/sdcard/bugreport.crash", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    adb.uninstall(device, package_name)
    adb.install(device, package_name, apk_path)

def prepare_apk(instrumented_app_path, package_name, result_dir):
    apk_path = get_apk_path(instrumented_app_path)
    if instrumented_app_path.endswith(".apk"):
        instrumented_app_path += "_output"
        os.system("mkdir -p " + instrumented_app_path)

    print "### Working on apk:", package_name
    # static analysis
    if settings.ENABLE_STRING_SEEDING:
        logger.log_progress("\nRunning static analysis on apk")
        output_dir = None
        if instrumented_app_path.endswith(".apk_output"):
            output_dir = instrumented_app_path
        else:
            output_dir = instrumented_app_path + "/bin"
        static_analyser.decode_apk(apk_path, output_dir)

    decoded_dir = None
    if instrumented_app_path.endswith(".apk_output"):
        decoded_dir = instrumented_app_path + "/" + apk_path.split("/")[-1].split(".apk")[0]
    else:
        decoded_dir = instrumented_app_path + "/bin/" + apk_path.split("/")[-1].split(".apk")[0]

    logger.log_progress("\nInstalling apk")

    mapper = MapperOnDevices(push_apk_and_string_xml, extra_args=(decoded_dir, package_name, apk_path,))
    mapper.run()

    return package_name


def get_apk_path(path):
    apk_path = None
    if path.endswith(".apk"):
        apk_path = path
    else:
        # now find its name
        for file_name in os.listdir(path + "/bin"):
            if file_name.endswith("-debug.apk"):
                apk_path = path + "/bin/" + file_name

    assert apk_path is not None
    return apk_path
