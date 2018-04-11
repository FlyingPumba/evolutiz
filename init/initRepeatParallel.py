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


import multiprocessing as mp
import time

import logger
from devices import any_device
import settings


# global results for mp callback
results = []
idle_devices = []
total_individuals = 0

def process_results(data):
	if data == False:
		logger.log_progress("\nInit population in parallel: failed to generate_individual")
		return

	individual, device = data

	global results
	results.append(individual)

	global idle_devices
	idle_devices.append(device)

	global total_individuals
	logger.log_progress("\rInit population in parallel: " + str(len(results)) + "/" + str(total_individuals))


def initPop(func, n, result_dir, package_name):
	"""Call the function *container* with a generator function corresponding
	to the calling *n* times the function *func*.
	"""
	# init global states

	if settings.DEBUG:
		print "### Init population in parallel"
		print "n=", n
		print "idle devices=", idle_devices

	global total_individuals
	global results
	total_individuals = n
	logger.log_progress("\nInit population in parallel: " + str(len(results)) + "/" + str(total_individuals))

	ret = []
	while len(results) > 0:
		results.pop()
	while len(idle_devices) > 0:
		idle_devices.pop()

	# 1. get idle devices
	idle_devices.extend(any_device.get_devices())

	if settings.DEBUG:
		print "idle devices after extending from any_device.get_devices()=", idle_devices

	# 2. aissign tasks to devices
	pool = mp.Pool(processes=len(idle_devices))
	for i in range(0, n):
		while len(idle_devices) == 0:
			time.sleep(0.1)

		if settings.DEBUG:
			print "### Call apply_async"
		# pool.apply_async(func, args=(idle_devices.pop(0), toolbox), callback=process_results)

		res = pool.apply(func, args=(idle_devices.pop(0), result_dir, package_name))
		process_results(res)

	# should wait for all processes finish
	pool.close()
	pool.join()
	ret.extend(results)

	return ret
