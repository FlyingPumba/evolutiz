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

def get_suite(device, apk_dir, package_name):
	start_time = datetime.datetime.now()

	ret = []
	unique_crashes = set()
	if settings.DEBUG:
		print " ... Generating suite of size", settings.SUITE_SIZE
	for i in range(0, settings.SUITE_SIZE):
		# get_sequence may return empty sequence
		if settings.DEBUG:
			print "Individual ", i
		seq = []
		repeated = 0
		while len(seq) <= 2:
			seq = get_sequence(device, apk_dir, package_name, i, unique_crashes)
			# print seq
			repeated += 1
			if repeated > 20:
				print "Cannot get sequence via MotifCore."
				raise CanNotInitSeqException("Cannot get sequence via MotifCore.")
		ret.append(seq)

	if settings.DEBUG:
		print "... Exiting get_suite method"

	# logger.log_progress("\nget_suite took " + str((datetime.datetime.now() - start_time).seconds))

	return ret


def get_sequence(device, apk_dir, package_name, index, unique_crashes):
	start_time = datetime.datetime.now()

	std_out_file = apk_dir + "/intermediate/" + "output.stdout"
	random.seed()

	motifcore_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

	while True:
		try:
			ret = []

			# clear data
			adb.shell_command(device, "pm clear " + package_name)

			# start motifcore
			print "... Start generating a sequence"
			motifcore_cmd = "motifcore -p " + package_name + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport --string-seeding /mnt/sdcard/" + package_name + "_strings.xml -v " + str(
				motifcore_events)
			adb.shell_command(device, motifcore_cmd, timeout=True)

			print "... Finish generating a sequence"
			# access the generated script, should ignore the first launch activity
			script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
			ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

			motifcore_script_filename = apk_dir + "/intermediate/" + script_name + ".init." + ts + "." + str(index)

			# need to kill motifcore when timeout
			adb.pkill(device, "motifcore")

			adb.pull(device, settings.MOTIFCORE_SCRIPT_PATH, motifcore_script_filename)
			script = open(motifcore_script_filename)
			is_content = False
			is_skipped_first = False
			for line in script:
				line = line.strip()
				if line.find("start data >>") != -1:
					is_content = True
					continue
				if is_content and line != "":
					if is_skipped_first == False:
						is_skipped_first = True
						continue
					if is_skipped_first:
						ret.append(line)

			script.close()
			break
		except Exception as e:
			logger.log_progress("There was an error trying to pull motifcore script: " + motifcore_script_filename)
			# traceback.print_exc(file=logger.orig_stdout)
			print e
			time.sleep(1)
			continue

	# deal with crash
	crash_handler.handle(device, apk_dir, motifcore_script_filename, "init", ts, index, unique_crashes)

	print "... Exiting get_sequence method"

	# logger.log_progress("\nget_sequence took " + str((datetime.datetime.now() - start_time).seconds))

	return ret


def gen_individual(device, apk_dir, package_name):
	try:
		if settings.DEBUG:
			print "Generate Individual on device, ", device
		suite = get_suite(device, apk_dir, package_name)
		if settings.DEBUG:
			print "Finished generating individual"
		return (creator.Individual(suite), device)
	except Exception as e:
		traceback.print_exc(file=logger.orig_stdout)
		print e
		return False