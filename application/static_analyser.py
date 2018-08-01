import os
from datetime import datetime as dt

import logger
import settings


def decode_apk(input_apk, output_dir):
	# os.chdir(output_dir)
	# print output_dir
	start_time = dt.now()

	os.system("java -jar " + settings.WORKING_DIR + "lib/apktool.jar d -f " + input_apk + logger.redirect_string())

	# logger.log_progress("\ndecode_apk took " + str((dt.now() - start_time).seconds))


def upload_string_xml(device, decoded_dir, package_name):
	start_time = dt.now()

	string_xml_path = decoded_dir + "/res/values/strings.xml"
	if settings.ENABLE_STRING_SEEDING is False or os.path.exists(string_xml_path) is False:
		# if not exist, upload dummy strings.xml
		string_xml_path = settings.WORKING_DIR + "resources/dummy_strings.xml"
	os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " shell rm /mnt/sdcard/" + package_name + "_strings.xml" + logger.redirect_string())
	os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " push " + string_xml_path + " /mnt/sdcard/" + package_name + "_strings.xml" + logger.redirect_string())

	# logger.log_progress("\nupload_string_xml took " + str((dt.now() - start_time).seconds))

