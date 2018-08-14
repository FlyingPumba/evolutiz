import multiprocessing as mp
import os

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from util import logger
from util.pickable import pickable_function


class TestRunnerInstaller(object):

    def __init__(self, test_runner_name, test_runner_executable_path, test_runner_jar_path):
        self.test_runner_name = test_runner_name
        self.test_runner_executable_path = test_runner_executable_path
        self.test_runner_jar_path = test_runner_jar_path

        self.successful_installations = 0

    def kill_test_runner_in_device(self, device):
        adb.pkill(device, self.test_runner_name)

    def kill_test_runner_in_all_devices(self):
        self.device_manager = RequiredFeature('device_manager').request()
        for device in self.device_manager.get_devices():
            self.kill_test_runner_in_device(device)

    def install_in_all_devices(self, minimum_api=None):
        self.device_manager = RequiredFeature('device_manager').request()
        self.successful_installations = 0

        logger.log_progress("\nPreparing " + self.test_runner_name + " test runner in devices.")

        devices = [device for device in self.device_manager.get_devices()
                   if minimum_api is None or device.api_level() >= minimum_api]

        if len(devices) == 0:
            raise Exception("No devices found with api level greater or equal than " + str(minimum_api))

        pool = mp.Pool(processes=len(devices))
        for device in devices:
            pool.apply_async(pickable_function,
                             args=(self, 'install_wrapper',
                                   (device,)),
                             callback=self.on_successful_installation)

        # wait for all processes to finish
        pool.close()
        pool.join()

        if self.successful_installations != len(devices):
            raise Exception("Unable to install " + self.test_runner_name + " test runner in all devices")

    def on_successful_installation(self, success):
        if success:
            self.successful_installations += 1

    def install_wrapper(self, device):
        try:
            self.install(device)
            return True
        except Exception as e:
            return False

    def install(self, device):
        adb.get_root_permissions(device)

        # remount partitions
        result_code = adb.adb_command(device, "remount", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to remount partitions on device: " + device.name)

        # make /mnt/sdcard writable
        result_code = adb.shell_command(device, "mount -o rw,remount /",
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to remount root partition on device: " + device.name)

        # TODO: remove the following lines, they are not needed anymore and are not universal across emulators/devices
        # result_code = adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard",
        #                                      timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        # if result_code != 0:
        #     device.flag_as_malfunctioning()
        #     raise Exception("Unable to install test runner on device: " + device.name)
        #
        # result_code = adb.sudo_shell_command(device, "mount -o rw,remount /system",
        #                                      timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        # if result_code != 0:
        #     device.flag_as_malfunctioning()
        #     raise Exception("Unable to install test runner on device: " + device.name)

        # push
        adb.push(device, self.test_runner_jar_path, "/system/framework/" + self.test_runner_name + ".jar",
                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        result_code = adb.shell_command(device, "chmod 777 /system/framework/" + self.test_runner_name + ".jar",
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to install test runner on device: " + device.name)

        adb.push(device, self.test_runner_executable_path, "/system/bin/" + self.test_runner_name,
                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        result_code = adb.shell_command(device, "chmod 777 /system/bin/" + self.test_runner_name,
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to install test runner on device: " + device.name)

        return True
