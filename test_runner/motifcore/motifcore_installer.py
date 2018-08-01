import multiprocessing as mp

import logger
import settings
from devices import adb

def kill_motifcore(device):
	adb.pkill(device, "motifcore")

def kill_all_motifcore(device_manager):
	for device in device_manager.get_devices():
		kill_motifcore(device)

def install_in_all_devices(device_manager):
	logger.log_progress("\nPreparing motifcore in devices.")

	pool = mp.Pool(processes=len(device_manager.get_devices()))
	for device in device_manager.get_devices():
		pool.apply_async(install_wrapper,
						 args=(settings.WORKING_DIR + "lib/motifcore.jar",
							   settings.WORKING_DIR + "resources/motifcore",
							   device))

	# wait for all processes to finish
	pool.close()
	pool.join()

def install_wrapper(motifcore_path, motifcore_script_path, device):
	try:
		install(motifcore_path, motifcore_script_path, device)
	except Exception as e:
		raise Exception("Unable to complete motifcore installation in all devices")

def install(motifcore_path, motifcore_script_path, device):
	# make /mnt/sdcard and /system writable
	result_code = adb.sudo_shell_command(device, "mount -o rw,remount rootfs /", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	result_code = adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	result_code = adb.sudo_shell_command(device, "mount -o rw,remount /system", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	# push
	filename = adb.sudo_push(device, motifcore_path, "/system/framework/motifcore.jar", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
	result_code = adb.sudo_shell_command(device, "chmod 777 /system/framework/" + filename, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	filename = adb.sudo_push(device, motifcore_script_path, "/system/bin/motifcore", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
	result_code = adb.sudo_shell_command(device, "chmod 777 /system/bin/" + filename, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
	if result_code != 0:
		adb.reboot(device)
		raise Exception("Unable to install motifcore on device: " + adb.get_device_name(device))

	# recover permission
	adb.sudo_shell_command(device, "mount -o ro,remount /system", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

	return True
