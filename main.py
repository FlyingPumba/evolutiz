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


import platform
import sys
import time
from datetime import datetime

import os
from deap import tools, base

import logger
import settings
from algorithms.eaDynaMosaParallel import eaDynaMosaParallel
from algorithms.eaMonotonicParallel import eaMonotonicParallel
from algorithms.eaMosaParallel import eaMosaParallel
from algorithms.eaMuPlusLambdaParallel import eaMuPlusLambdaParallel
from algorithms.eaOnePlusLambdaCommaLambdaParallel import eaOnePlusLambdaCommaLambdaParallel
from algorithms.eaStandardParallel import eaStandardParallel
from algorithms.eaSteadyStateParallel import eaSteadyStateParallel
from algorithms.gen_individual import gen_individual
from devices import any_device
from devices.prepare_apk_parallel import prepare_apk
from init import initRepeatParallel

start_time = None
apk_dir = None
package_name = None

def main(instrumented_app_dir, eaStrategy):
	"""
	Test one apk
	:param instrumented_app_dir: The instrumentation folder of the app | apk file path for closed-source app
	"""

	global apk_dir
	apk_dir = instrumented_app_dir

	logger.prepare()
	logger.clear_progress()
	logger.log_progress("Sapienz")

	# platform specific settings
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

	# start time budget
	global start_time
	start_time = time.time()

	any_device.prepare_motifcore()
	any_device.clean_sdcard()

	# log the devices
	devices = any_device.get_devices()

	# get package name and prepare apk if necessary
	global package_name
	package_name = prepare_apk(devices, instrumented_app_dir)
	logger.log_progress("\nWorking on package: " + package_name)

	# register common functions in toolbox
	toolbox = base.Toolbox()
	toolbox.register("individual", gen_individual)
	toolbox.register("population", initRepeatParallel.initPop, list, toolbox.individual)
	toolbox.register("time_budget_available", time_budget_available)
	toolbox.register("get_apk_dir", get_apk_dir)
	toolbox.register("get_package_name", get_package_name)

	# hof = tools.HallOfFame(6)
	# pareto front can be large, there is a similarity option parameter
	hof = tools.ParetoFront()

	# genetic algorithm
	eaStrategy.setup(toolbox)
	population = eaStrategy.evolve()

	print "\n### Finished main"

	# recover stdout and stderr
	logger.restore()

def time_budget_available():
	current_time = time.time()
	elapsed_time = current_time - start_time
	return elapsed_time < settings.SEARCH_BUDGET_IN_SECONDS

def get_apk_dir():
	global apk_dir
	return apk_dir

def get_package_name():
	global package_name
	return package_name

if __name__ == "__main__":
	app_dir = sys.argv[1]
	genetic_algorithm = sys.argv[2]
	eaStrategy = None
	if genetic_algorithm == "standard":
		eaStrategy = eaStandardParallel()
	elif genetic_algorithm == "monotonic":
		eaStrategy = eaMonotonicParallel()
	elif genetic_algorithm == "steady":
		eaStrategy = eaSteadyStateParallel()
	elif genetic_algorithm == "muPlusLambda":
		eaStrategy = eaMuPlusLambdaParallel()
	elif genetic_algorithm == "onePlusLambdaCommaLambda":
		eaStrategy = eaOnePlusLambdaCommaLambdaParallel()
	elif genetic_algorithm == "mosa":
		eaStrategy = eaMosaParallel()
	elif genetic_algorithm == "dynaMosa":
		eaStrategy = eaDynaMosaParallel()
	main(app_dir, eaStrategy)
