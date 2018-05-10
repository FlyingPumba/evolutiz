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
import subprocess as sub
import time

import settings
from devices import adb
from util import motifcore_installer
from util import pack_and_deploy
import logger

def get_devices():
	""" will also get devices ready
	:return: a list of avaiable devices names, e.g., emulator-5556
	"""
	print "### get_devices..."

	# print "### killall adb"
	# os.system("kill -9 $(lsof -i:5037 | tail -n +2 | awk '{print $2}')" + logger.redirect_string())
	# os.system("killall adb" + logger.redirect_string())

	print "### adb devices"
	os.system("$ANDROID_HOME/platform-tools/adb devices" + logger.redirect_string())

	ret = []
	p = sub.Popen('$ANDROID_HOME/platform-tools/adb devices', stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
	output, errors = p.communicate()
	print output
	segs = output.split("\n")
	for seg in segs:
		try:
			status = seg.split("\t")[1].strip()
			if status == "device":
				ret.append(seg.split("\t")[0].strip())
		except:
			pass

	print "### len(ret)", len(ret)
	if len(ret) == 0:
		time.sleep(1)
		return get_devices()

	assert len(ret) > 0
	return ret


def reboot_all_devices():
	"""
	prepare the env of the device
	:return:
	"""
	logger.log_progress("\nRebooting all devices")
	for device in get_devices():
		result_code = adb.adb_command(device, "reboot", timeout=settings.ADB_FAST_COMMAND_TIMEOUT)
		if result_code != 0:
			logger.log_progress("\nUnable to reboot device: " + adb.get_device_name(device))
			logger.log_progress("\nPlease, turn it off and on manually.")
			raise Exception("Unable to reboot device: " + adb.get_device_name(device))
	# it takes 2 minutes for all devices to stabilize after reboot
	logger.log_progress("\nWaiting 2 minutes for all devices to stabilize")
	time.sleep(settings.AVD_BOOT_DELAY)
	time.sleep(settings.AVD_BOOT_DELAY)
	time.sleep(settings.AVD_BOOT_DELAY)


def disable_systemui():
	for device in get_devices():
		adb.shell_command(device, "service call activity 42 s16 com.android.systemui")


def prepare_motifcore():
	for device in get_devices():
		logger.log_progress("\nInstalling motifcore in device: " + adb.get_device_name(device))
		motifcore_installer.install(settings.WORKING_DIR + "lib/motifcore.jar", settings.WORKING_DIR + "resources/motifcore", device)
		logger.log_progress(" -> Done")


def kill_motifcore(device):
	adb.pkill(device, "motifcore")


def kill_all_motifcore():
	for device in get_devices():
		kill_motifcore(device)


def pack_and_deploy_aut():
	# instrument the app under test
	pack_and_deploy.main(get_devices())


def clean_device_app(device, package_name):
	os.system("kill -9 $(lsof -i:5037 | tail -n +2 | awk '{print $2}')" + logger.redirect_string())
	os.system("killall adb" + logger.redirect_string())
	os.system("$ANDROID_HOME/platform-tools/adb devices" + logger.redirect_string())

	print "### kill motifcore ..."
	kill_motifcore(device)
	adb.shell_command(device, "pm clear " + package_name)
	adb.shell_command(device, "am force-stop " + package_name)
	adb.shell_command(device, "uninstall " + package_name)
	adb.shell_command(device, "rm /mnt/sdcard/bugreport.crash")
	print "### clean device app finished"


if __name__ == "__main__":
	kill_all_motifcore()
