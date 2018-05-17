# Copyright (c) 2016-present, Ke Mao. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#
#     * The names of the contributors may not be used to endorse or
#       promote products derived from this software without specific
#       prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


import os
import datetime
import time

from lxml import html
from bs4 import UnicodeDammit

import logger
import settings
from crashes import crash_handler
from devices import adb

def extract_coverage(path):
	with open(path, 'rb') as file:
		content = file.read()
		doc = UnicodeDammit(content, is_html=True)

	parser = html.HTMLParser(encoding=doc.original_encoding)
	root = html.document_fromstring(content, parser=parser)
	return root.xpath('/html/body/table[2]/tr[2]/td[5]/text()')[0].strip()


# return accumulative coverage and average length
def get_suite_coverage(is_motifgene_enabled, scripts, device, result_dir, apk_dir, package_name, gen, pop):
	unique_crashes = set()

	# clean states
	adb.shell_command(device, "am force-stop " + package_name)
	adb.shell_command(device, "pm clear " + package_name)

	application_files = "/data/data/" + package_name + "/files"
	coverage_path_in_device = application_files + "/coverage.ec"
	coverage_backup_path_before_clear = "/mnt/sdcard/coverage.ec"

	adb.shell_command(device, "rm -f " + coverage_path_in_device)
	adb.shell_command(device, "rm -f " + coverage_backup_path_before_clear)

	ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")
	coverage_folder = str(gen) + "." + str(pop) + "." + ts

	os.chdir(result_dir)
	os.system("mkdir -p coverages/" + coverage_folder)
	os.system("cp coverage.em coverages/" + coverage_folder + logger.redirect_string())
	os.chdir("coverages/" + coverage_folder)


	there_is_coverage = False

	# run scripts
	for index, script in enumerate(scripts):
		result_code = adb.shell_command(device, "am instrument " + package_name + "/" + package_name + ".EmmaInstrument.EmmaInstrumentation", timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
		if result_code != 0:
			log_evaluation_result(device, result_dir, script, False)
			adb.reboot(device)
			raise Exception("Unable to instrument " + package_name)

		result_code = adb.push(device, script, "/mnt/sdcard/", timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
		if result_code != 0:
			log_evaluation_result(device, result_dir, script, False)
			adb.reboot(device)
			raise Exception("Unable to push motifcore script " + script + " to device: " + adb.get_device_name(device))

		script_name = script.split("/")[-1]

		run_script_using_motifcore(is_motifgene_enabled, device, package_name, script_name)

		if crash_handler.handle(device, result_dir, script, gen, pop, index, unique_crashes):
			pass
		else:
			# no crash, can broadcast
			result_code = adb.shell_command(device, "am broadcast -a edu.gatech.m3.emma.COLLECT_COVERAGE", timeout=settings.COVERAGE_BROADCAST_TIMEOUT)
			if result_code != 0:
				log_evaluation_result(device, result_dir, script, False)
				adb.reboot(device)
				raise Exception("Unable to broadcast coverage gathering for script " + script + " in device: " + adb.get_device_name(device))
			there_is_coverage = True

			tries = 0
			max_tries = 10
			found_coverage_file = False
			while tries < max_tries:
				if not adb.exists_file(device, coverage_path_in_device, timeout=settings.ADB_FAST_COMMAND_TIMEOUT):
					time.sleep(15)
					tries += 1
				else:
					found_coverage_file = True
					break

			if not found_coverage_file:
				log_evaluation_result(device, result_dir, script, False)
				adb.reboot(device)
				raise Exception("Coverage broadcast was sent for script " + script + " in device: " + adb.get_device_name(device) + " but there is not file: " + coverage_path_in_device)

			# save coverage.ec file to /mnt/sdcard before clearing app (files are deleted)
			result_code = adb.sudo_shell_command(device, "cp -p " + coverage_path_in_device + " " + coverage_backup_path_before_clear, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
			if result_code != 0:
				log_evaluation_result(device, result_dir, script, False)
				adb.reboot(device)
				raise Exception("Unable to retrieve coverage.ec file after coverage broadcast for script " + script + " in  device: " + adb.get_device_name(device))

		# close app
		result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_PM_CLEAR_COMMAND_TIMEOUT)
		if result_code != 0:
			log_evaluation_result(device, result_dir, script, False)
			adb.reboot(device)
			raise Exception("Unable to clear package for script " + script + " in device: " + adb.get_device_name(device))


		# restore the coverage.ec file from /mnt/sdcard to app files
		result_code = adb.sudo_shell_command(device, "mkdir " + application_files, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
		if result_code != 0:
			log_evaluation_result(device, result_dir, script, False)
			adb.reboot(device)
			raise Exception("Unable to create application files directory for script " + script + " in device: " + adb.get_device_name(device))

		if there_is_coverage:
			result_code = adb.sudo_shell_command(device, "cp -p " + coverage_backup_path_before_clear + " " + coverage_path_in_device, timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
			if result_code != 0:
				log_evaluation_result(device, result_dir, script, False)
				adb.reboot(device)
				raise Exception("Unable to copy backup coverage.ec file in sdcard for script " + script + " in device: " + adb.get_device_name(device))


		log_evaluation_result(device, result_dir, script, True)

	print "### Getting EMMA coverage.ec and report ..."
	result_code = adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_PM_CLEAR_COMMAND_TIMEOUT)
	if result_code != 0:
		log_evaluation_result(device, result_dir, "clear-package", False)
		adb.reboot(device)
		raise Exception("Unable to clear package " + package_name + " in device: " + adb.get_device_name(device))

	if there_is_coverage:
		result_code = adb.pull(device, coverage_backup_path_before_clear, "coverage.ec", timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
		if result_code != 0:
			log_evaluation_result(device, result_dir, "pull-coverage", False)
			adb.reboot(device)
			raise Exception("Unable to pull coverage for device: " + adb.get_device_name(device))

		os.system("java -cp " + settings.WORKING_DIR + "lib/emma.jar emma report -r html -in coverage.em,coverage.ec -sp " + apk_dir + "/src " + logger.redirect_string())

		html_file = result_dir + "/coverages/" + coverage_folder + "/coverage/index.html"
		coverage_str = extract_coverage(html_file)

		if coverage_str.find("%") != -1:
			return int(coverage_str.split("%")[0]), len(unique_crashes)
		else:
			log_evaluation_result(device, result_dir, "html-extract-coverage", False)
			return 0, len(unique_crashes)
	else:
		return 0, len(unique_crashes)


def run_script_using_motifcore(use_motifgene, device, package_name, script_name):
	adb.set_bluetooth_state(device, True)
	adb.set_wifi_state(device, True)
	adb.set_location_state(device, True)

	string_seeding_flag = ""

	if use_motifgene:
		string_seeding_flag = "--string-seeding /mnt/sdcard/" + package_name + "_strings.xml"

	motifcore_cmd = "motifcore -p " + package_name \
					+ " --ignore-crashes --ignore-security-exceptions --ignore-timeouts --bugreport "\
					+ string_seeding_flag + " -f /mnt/sdcard/" + script_name + " 1"

	adb.sudo_shell_command(device, motifcore_cmd, timeout=settings.MOTIFCORE_EVAL_TIMEOUT, log_output=False)

	# need to manually kill motifcore when timeout
	adb.pkill(device, "motifcore")

def log_evaluation_result(device, result_dir, script, success):
	device_adb_log_file = result_dir + "/" + adb.get_device_name(device) + "-evaluations.log"
	os.system("echo \"" + str(success) + " -> " + script + "\" >> " + device_adb_log_file)