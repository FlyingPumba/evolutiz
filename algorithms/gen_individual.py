import datetime
import os
import time
import random
import traceback

from deap import creator

import logger
import settings
from crashes import crash_handler
from devices import adb

class CanNotInitSeqException(Exception):
	pass

def get_suite(use_motifgene, device, result_dir, package_name):
	start_time = datetime.datetime.now()

	ret = []
	unique_crashes = set()

	for i in range(0, settings.SUITE_SIZE):
		seq = get_sequence(use_motifgene, device, result_dir, package_name, i, unique_crashes)
		ret.append(seq)

	# logger.log_progress("\nget_suite took " + str((datetime.datetime.now() - start_time).seconds))

	return ret


def get_sequence(use_motifgene, device, result_dir, package_name, index, unique_crashes):
	start_time = datetime.datetime.now()

	std_out_file = result_dir + "/intermediate/" + "output.stdout"
	random.seed()

	motifcore_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

	ret = []

	# clear data
	result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Failed to clear package " + package_name + " in device: " + adb.get_device_name(device))

	adb.set_bluetooth_state(device, True, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
	adb.set_wifi_state(device, True, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
	adb.set_location_state(device, True, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)

	# start motifcore
	string_seeding_flag = ""

	if use_motifgene:
		string_seeding_flag = "--string-seeding /mnt/sdcard/" + package_name + "_strings.xml"

	motifcore_cmd = "motifcore -p " + package_name + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport " + string_seeding_flag + " -v " + str(
		motifcore_events)
	adb.shell_command(device, motifcore_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

	# access the generated script, should ignore the first launch activity
	script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
	ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

	motifcore_script_filename = result_dir + "/intermediate/" + script_name + ".init." + ts + "." + str(index)

	# need to kill motifcore when timeout
	adb.pkill(device, "motifcore")

	result_code = adb.pull(device, settings.MOTIFCORE_SCRIPT_PATH, motifcore_script_filename, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Failed to retrieve motifcore script from device: " + adb.get_device_name(device))

	# remove motifgenes from test case if they are disabled
	if not use_motifgene:
		os.system("sed -i '/GUIGen/d' " + motifcore_script_filename)

	script = open(motifcore_script_filename)
	is_content = False
	is_skipped_first = False
	for line in script:
		line = line.strip()
		if line.find("start data >>") != -1:
			is_content = True
			continue
		if is_content and line != "":
			if not is_skipped_first:
				is_skipped_first = True
				continue
			if is_skipped_first:
				ret.append(line)

	script.close()

	# deal with crash
	crash_handler.handle(device, result_dir, motifcore_script_filename, "init", ts, index, unique_crashes)

	# logger.log_progress("\nget_sequence took " + str((datetime.datetime.now() - start_time).seconds))

	return ret


def gen_individual(use_motifgene, device, result_dir, package_name):
	try:
		suite = get_suite(use_motifgene, device, result_dir, package_name)
		return creator.Individual(suite), device

	except Exception as e:
		print e
		traceback.print_exc()
		return False, device