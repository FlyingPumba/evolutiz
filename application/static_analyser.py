import os

import settings
from devices import adb
from util import logger


def decode_apk(input_apk, output_dir):
    os.system("java -jar " + settings.WORKING_DIR + "lib/apktool.jar d -f " + input_apk + logger.redirect_string())


def upload_string_xml(device, decoded_dir, package_name):
    string_xml_path = decoded_dir + "/res/values/strings.xml"
    if settings.ENABLE_STRING_SEEDING is False or os.path.exists(string_xml_path) is False:
        # if not exist, upload dummy strings.xml
        string_xml_path = settings.WORKING_DIR + "resources/dummy_strings.xml"
    adb.shell_command(device, " shell rm /mnt/sdcard/" + package_name + "_strings.xml", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    adb.push(device, string_xml_path, "/mnt/sdcard/" + package_name + "_strings.xml", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
