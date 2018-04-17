import datetime
import os
import time
import random
import traceback

import numpy
from deap import creator

import logger
import settings
from crashes import crash_handler
from devices import adb

from lxml import html
from bs4 import UnicodeDammit

class CanNotInitSeqException(Exception):
	pass

def get_suite_with_coverage(device, result_dir, apk_dir, package_name, gen, pop):
	start_time = datetime.datetime.now()

	ret = []
	unique_crashes = set()

	# clean states
	adb.shell_command(device, "am force-stop " + package_name)
	adb.shell_command(device, "pm clear " + package_name)

	# delete previous coverage files
	application_files = "/data/data/" + package_name + "/files"
	coverage_path_in_device = application_files + "/coverage.ec"
	coverage_backup_path_before_clear = "/mnt/sdcard/coverage.ec"

	adb.shell_command(device, "rm " + coverage_path_in_device)
	adb.shell_command(device, "rm " + coverage_backup_path_before_clear)

	# create folder to store coverage of suite
	ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
	coverage_folder = str(gen) + "." + str(pop) + "." + ts

	os.chdir(result_dir)
	os.system("mkdir -p coverages/" + coverage_folder)
	os.system("cp coverage.em coverages/" + coverage_folder + logger.redirect_string())
	os.chdir("coverages/" + coverage_folder)

	lengths = []

	for i in range(0, settings.SUITE_SIZE):

		adb.shell_command(device,
						  "am instrument " + package_name + "/" + package_name + ".EmmaInstrument.EmmaInstrumentation")

		seq = get_sequence(device, result_dir, package_name,
						   gen, pop, i, unique_crashes)

		if len(seq) <= 2:
			logger.log("Cannot get sequence via MotifCore.")

		ret.append(seq)
		lengths.append(len(seq))

		# save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
		adb.shell_command(device, "cp -p " + coverage_path_in_device + " " + coverage_backup_path_before_clear)
		# close app and clear data
		adb.shell_command(device, "pm clear " + package_name)
		# restore the coverage.ec file from /mnt/sdcard to app files
		adb.shell_command(device, "mkdir " + application_files)
		adb.shell_command(device, "cp -p " + coverage_backup_path_before_clear + " " + coverage_path_in_device)

	print "### Getting EMMA coverage.ec and report ..."
	adb.shell_command(device, "pm clear " + package_name)
	time.sleep(0.5)
	adb.pull(device, coverage_backup_path_before_clear, "coverage.ec")
	os.system(
		"java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em,coverage.ec -sp " + apk_dir + "/src " + logger.redirect_string())

	html_file = result_dir + "/coverages/" + coverage_folder + "/coverage/index.html"

	coverage = 0
	crashes = len(unique_crashes)
	length = numpy.mean(lengths)

	try:
		coverage_str = extract_coverage(html_file)

		if coverage_str.find("%") != -1:
			coverage = int(coverage_str.split("%")[0])
	except Exception, e:
		logger.log_progress("Exception occurred trying to extract coverage from html file" + e.strerror)

	# logger.log_progress("\nget_suite took " + str((datetime.datetime.now() - start_time).seconds))

	return ret, (coverage, length, crashes)


def get_sequence(device, result_dir, package_name,
				 gen, pop, index, unique_crashes):
	start_time = datetime.datetime.now()

	random.seed()

	motifcore_events = random.randint(settings.SEQUENCE_LENGTH_MIN, settings.SEQUENCE_LENGTH_MAX)

	ret = []

	# start motifcore
	print "... Start generating a sequence"
	motifcore_cmd = "motifcore -p " + package_name + " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport --string-seeding /mnt/sdcard/" + package_name + "_strings.xml -v " + str(
		motifcore_events)
	adb.shell_command(device, motifcore_cmd, timeout=True)

	print "... Finish generating a sequence"
	# need to manually kill motifcore when timeout
	adb.pkill(device, "motifcore")

	# access the generated script, should ignore the first launch activity
	script_name = settings.MOTIFCORE_SCRIPT_PATH.split("/")[-1]
	ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
	motifcore_script_filename = script_name + ".init." + str(gen) + "." + str(pop) + "." + ts

	# retrieve file from device
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

	if crash_handler.handle(device, result_dir, motifcore_script_filename, gen, pop, index, unique_crashes):
		pass
	else:
		# no crash, can broadcast
		adb.shell_command(device, "am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE")

	# logger.log_progress("\nget_sequence took " + str((datetime.datetime.now() - start_time).seconds))

	return ret


def gen_individual(device, result_dir, apk_dir, package_name, gen, pop):
	try:
		if settings.DEBUG:
			print "Generate Individual on device, ", device
		suite, coverage = get_suite_with_coverage(device, result_dir, apk_dir, package_name, gen, pop)
		if settings.DEBUG:
			print "Finished generating individual"
		ind = creator.Individual(suite)
		ind.fitness.values = coverage

		return ind, device

	except Exception as e:
		traceback.print_exc(file=logger.orig_stdout)
		print e
		return False


def extract_coverage(path):
	with open(path, 'rb') as file:
		content = file.read()
		doc = UnicodeDammit(content, is_html=True)

	parser = html.HTMLParser(encoding=doc.original_encoding)
	root = html.document_fromstring(content, parser=parser)
	return root.xpath('/html/body/table[2]/tr[2]/td[5]/text()')[0].strip()