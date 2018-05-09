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
import traceback

import logger
from devices import adb

def install(motifcore_path, motifcore_script_path, device):
	# make /mnt/sdcard and /system writable
	result_code = adb.sudo_shell_command(device, "mount -o rw,remount rootfs /", timeout=True)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	result_code = adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard", timeout=True)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	result_code = adb.sudo_shell_command(device, "mount -o rw,remount /system", timeout=True)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	# push
	filename = adb.sudo_push(device, motifcore_path, "/system/framework/motifcore.jar", timeout=True)
	result_code = adb.sudo_shell_command(device, "chmod 777 /system/framework/" + filename, timeout=True)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	filename = adb.sudo_push(device, motifcore_script_path, "/system/bin/motifcore")
	result_code = adb.sudo_shell_command(device, "chmod 777 /system/bin/" + filename, timeout=True)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	# recover permission
	adb.sudo_shell_command(device, "mount -o ro,remount /system", timeout=True)

	return True
