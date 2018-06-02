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
from devices import any_device, adb
import settings


# global results for mp callback
results = []
idle_devices = []
rebooting_devices = {}
total_individuals = 0
remaining_individuals_to_evaluate = 0

def process_results(data):
	individual, device = data
	if individual == False:
		logger.log_progress("\nInit population in parallel: failed to generate_individual on device: " + adb.get_device_name(device))
		# device was unable to complete the generation of individual, an thus was rebooted
		# store time of rebooting, so we can calculate when the device is going to be back online
		global rebooting_devices
		rebooting_devices[device] = time.time()

		global remaining_individuals_to_evaluate
		remaining_individuals_to_evaluate += 1
	else:
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
	global total_individuals
	total_individuals = n

	global results
	while len(results) > 0:
		results.pop()

	global idle_devices
	while len(idle_devices) > 0:
		idle_devices.pop()

	global rebooting_devices
	rebooting_devices.clear()

	global remaining_individuals_to_evaluate
	remaining_individuals_to_evaluate = total_individuals

	# get idle devices
	idle_devices.extend(any_device.get_devices())
	total_devices = len(idle_devices)

	# 2. aissign tasks to devices
	logger.log_progress("\nInit population in parallel: " + str(len(results)) + "/" + str(total_individuals))

	pool = mp.Pool(processes=total_devices)
	while not (remaining_individuals_to_evaluate == 0 and len(idle_devices) == total_devices):
		check_devices_that_finished_rebooting()

		while len(idle_devices) == 0:
			check_devices_that_finished_rebooting()
			print "Waiting for a device to become idle"
			print "idle_devices: " + str(idle_devices)
			print "rebooting_devices: " + str(rebooting_devices)
			time.sleep(2)

		if remaining_individuals_to_evaluate > 0:
			remaining_individuals_to_evaluate -= 1
			device = idle_devices.pop(0)
			pool.apply_async(func, args=(device, result_dir, package_name), callback=process_results)
		else:
			print "There are not individuals remaining to evaluate"
			print "idle_devices: " + str(idle_devices)
			print "rebooting_devices: " + str(rebooting_devices)
			time.sleep(2)

	# should wait for all processes finish
	pool.close()
	pool.join()

	ret = []
	ret.extend(results)

	return ret


def check_devices_that_finished_rebooting():
	global idle_devices
	global rebooting_devices

	current_devices = any_device.get_devices()
	for device in current_devices:
		if device in rebooting_devices:
			current_time = time.time()
			reboot_time = rebooting_devices[device]

			# check if device was rebooted more than 2 minutes ago
			if current_time - reboot_time >= 60 * 2:
				print "Found that device " + adb.get_device_name(device) + " just finished rebooting"
				rebooting_devices.pop(device)
				idle_devices.append(device)