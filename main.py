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
import platform
import subprocess
import sys

from deap import tools, base

import settings
from algorithms import eaMuPlusLambdaParallel
from algorithms.eaStandardParallel import eaStandardParallel
from algorithms.gen_individual import gen_individual
from analysers import static_analyser
from devices import any_device
from init import initRepeatParallel


def main(instrumented_app_dir, eaStrategy):
	"""
	Test one apk
	:param instrumented_app_dir: The instrumentation folder of the app | apk file path for closed-source app
	"""

	host_system = platform.system()
	if host_system == "Darwin":
		print "Running on Mac OS"
		settings.TIMEOUT_CMD = "gtimeout"
	elif host_system == "Linux":
		print "Running on Linux"
	else:
		print "Runnning on unknown OS"

	print "Preparing devices ..."
	any_device.boot_devices()
	any_device.prepare_motifcore()
	any_device.clean_sdcard()

	# log the devices
	devices = any_device.get_devices()

	# get package name and prepare apk if necessary
	package_name = prepare_apk(devices, instrumented_app_dir)

	# register common functions in toolbox
	toolbox = base.Toolbox()
	toolbox.register("individual", gen_individual)
	toolbox.register("population", initRepeatParallel.initPop, list, toolbox.individual)

	# hof = tools.HallOfFame(6)
	# pareto front can be large, there is a similarity option parameter
	hof = tools.ParetoFront()

	# genetic algorithm
	eaStrategy.setup(toolbox, instrumented_app_dir, package_name)
	population = eaStrategy.evolve()

	print "\n\n\n### Finished main"

	print "\n\n\n### Writting final population"
	for ind in population:
		print "Individual with fitness: ", ind.fitness


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
	# will use dummy 0 if disabled
	for device in devices:
		decoded_dir = None
		if instrumented_app_dir.endswith(".apk_output"):
			decoded_dir = instrumented_app_dir + "/" + apk_path.split("/")[-1].split(".apk")[0]
		else:
			decoded_dir = instrumented_app_dir + "/bin/" + apk_path.split("/")[-1].split(".apk")[0]
		static_analyser.upload_string_xml(device, decoded_dir, package_name)

		print "### Installing apk:", apk_path
		os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " shell rm /mnt/sdcard/bugreport.crash")
		os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " uninstall " + package_name)
		os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " install " + apk_path)

	# intermediate should be in app folder
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

if __name__ == "__main__":
	app_dir = sys.argv[1]
	genetic_algorithm = sys.argv[2]
	eaStrategy = None
	if genetic_algorithm == "standard":
		eaStrategy = eaStandardParallel()
	elif genetic_algorithm == "onePlusLambda":
		# population, logbook = eaMuPlusLambdaParallel.evolve(population, toolbox, settings.POPULATION_SIZE,
		# 													settings.OFFSPRING_SIZE,
		# 													cxpb=settings.CXPB, mutpb=settings.MUTPB,
		# 													ngen=settings.GENERATION,
		# 													apk_dir=instrumented_app_dir,
		# 													package_name=package_name,
		# 													halloffame=hof, verbose=settings.DEBUG)
		pass
	main(app_dir, eaStrategy)
