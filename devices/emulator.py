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
		if seg.startswith("emulator-") and "offline" not in seg:
			print "Checking if boot animation is over for device " + device
			p = sub.Popen('$ANDROID_HOME/platform-tools/adb -s ' + device +
			              ' shell getprop init.svc.bootanim', stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
			output, errors = p.communicate()
			if output.strip() != "stopped":
				print "Animation is not over yet"
			else:
				print "Checking package manager is ready for device " + device
				p = sub.Popen('$ANDROID_HOME/platform-tools/adb -s ' + device +
							  ' shell pm list packages', stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
				output, errors = p.communicate()
				if "Error: Could not access the Package Manager" not in output.strip():
					print "Added device"
					ret.append(device)

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
		logger.log_progress("\rBooting devices: " + str(i + 1) + "/" + str(settings.DEVICE_NUM))

		emulator = "export QEMU_AUDIO_DRV=none && $ANDROID_HOME/emulator/emulator"

		logs = " > " + device_name + ".log 2>" + device_name + ".err"

		time.sleep(0.3)
		if settings.HEADLESS:
			# -no-window flag can't be at the end
			flags = " -wipe-data -no-window -no-boot-anim -writable-system -verbose -debug all"
			sub.Popen(emulator + ' -avd ' + device_name + flags + logs, stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
		else:
			flags = " -wipe-data -no-boot-anim -writable-system -verbose -debug all"
			sub.Popen(emulator + ' -avd ' + device_name + flags + logs, stdout=sub.PIPE, stderr=sub.PIPE, shell=True)

		print "Waiting ", settings.AVD_BOOT_DELAY, " seconds for emulators to be ready"
		time.sleep(settings.AVD_BOOT_DELAY)

	logger.log_progress(
		"\nWaiting for devices to be ready: " + str(len(get_devices())) + "/" + str(settings.DEVICE_NUM))
	while len(get_devices()) < settings.DEVICE_NUM:
		logger.log_progress("\rWaiting for devices to be ready: " + str(len(get_devices())) + "/" + str(settings.DEVICE_NUM))
		time.sleep(15)

	logger.log_progress(
		"\rWaiting for devices to be ready: " + str(len(get_devices())) + "/" + str(settings.DEVICE_NUM))


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
	for device in get_devices():
		motifcore_installer.install(settings.WORKING_DIR + "lib/motifcore.jar", settings.WORKING_DIR + "resources/motifcore", device)


def pack_and_deploy_aut():
	# instrument the app under test
	pack_and_deploy.main(get_devices())
