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
import time

import subprocess as sub

import logger
import settings
from devices import adb
from util import motifcore_installer
from util import pack_and_deploy
import multiprocessing as mp

installed_devices = 0
total_devices = 0

def get_devices():
	""" will also get devices ready
	:return: a list of avaiable devices names, e.g., emulator-5556
	"""
	ret = []
	p = sub.Popen('$ANDROID_HOME/platform-tools/adb devices',
	              stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
	output, errors = p.communicate()
	segs = output.split("\n")
	for seg in segs:
		device = seg.split("\t")[0].strip()
		if seg.startswith("emulator-"):
			# print "Checking if boot animation is over"
			# p = sub.Popen('$ANDROID_HOME/platform-tools/adb -s ' + device +
			#               ' shell getprop init.svc.bootanim', stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
			# output, errors = p.communicate()
			# if output.strip() != "stopped":
			# 	print "Animation is not over yet"
			# 	time.sleep(10)
			# 	print "Waiting for the emulator:", device
			# 	return get_devices()
			# else:
			# 	print "Added device"
			ret.append(device)

	assert len(ret) > 0

	return ret


def boot_devices():
	"""
	prepare the env of the device
	:return:
	"""

	current_devices = get_devices()

	logger.log_progress("\nBooting devices: " + str(0) + "/" + str(settings.DEVICE_NUM))

	for i in range(len(current_devices), settings.DEVICE_NUM):
		device_name = settings.AVD_SERIES + "_" + str(i)
		print "Booting Device:", device_name
		logger.log_progress("\rBooting devices: " + str(i+1) + "/" + str(settings.DEVICE_NUM))

		emulator = "$ANDROID_HOME/emulator/emulator"
		common_flags = " -wipe-data -no-boot-anim -writable-system -use-system-libs -qemu"

		time.sleep(0.3)
		if settings.HEADLESS:
			sub.Popen(emulator + ' -avd ' + device_name + common_flags + " -no-window", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
		else:
			sub.Popen(emulator + ' -avd ' + device_name + common_flags, stdout=sub.PIPE, stderr=sub.PIPE, shell=True)


	while len(get_devices()) < settings.DEVICE_NUM:
		print "Waiting ", settings.AVD_BOOT_DELAY, " seconds for emulators to be ready"
		time.sleep(settings.AVD_BOOT_DELAY)

def clean_sdcard():
	print "Cleaning SD card"
	for device in get_devices():
		adb.sudo_shell_command(device, "mount -o rw,remount rootfs")
		adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard")
		adb.sudo_shell_command(device, "rm -rf /mnt/sdcard/*")

def prepare_motifcore_callback(success):
	global installed_devices
	installed_devices += 1
	global total_devices
	logger.log_progress("\rPreparing motifcore in devices: " + str(installed_devices) + "/" + str(total_devices))

def prepare_motifcore():
	print "Preparing motifcore"
	devices = get_devices()

	pool = mp.Pool(processes=len(devices))

	global installed_devices
	installed_devices = 0
	global total_devices
	total_devices = len(devices)

	logger.log_progress("\nPreparing motifcore in devices: " + str(installed_devices) + "/" + str(total_devices))

	for device in devices:
		#pool.apply_async(motifcore_installer.install,
		#				 args=(settings.WORKING_DIR + "lib/motifcore.jar", settings.WORKING_DIR + "resources/motifcore", device),
		#				 callback=prepare_motifcore_callback)
		# res = pool.apply(motifcore_installer.install,
		# 				 args=(settings.WORKING_DIR + "lib/motifcore.jar", settings.WORKING_DIR + "resources/motifcore", device))
		# prepare_motifcore_callback(res)
		motifcore_installer.install(settings.WORKING_DIR + "lib/motifcore.jar", settings.WORKING_DIR + "resources/motifcore", device)

	pool.close()
	pool.join()


def pack_and_deploy_aut():
	# instrument the app under test
	pack_and_deploy.main(get_devices())
