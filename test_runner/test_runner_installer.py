import multiprocessing as mp

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

    def kill_test_runner_in_device(self, device):
        adb.pkill(device, self.test_runner_name)

    def kill_test_runner_in_all_devices(self):
        self.device_manager = RequiredFeature('device_manager').request()
        for device in self.device_manager.get_devices():
            self.kill_test_runner_in_device(device)

    def install_in_all_devices(self):
        self.device_manager = RequiredFeature('device_manager').request()
        logger.log_progress("\nPreparing " + self.test_runner_name + " test runner in devices.")

        devices = self.device_manager.get_devices()
        pool = mp.Pool(processes=len(devices))
        for device in devices:
            pool.apply_async(pickable_function,
                             args=(self, 'install_wrapper',
                                   (device,)))

        # wait for all processes to finish
        pool.close()
        pool.join()

    def install_wrapper(self, device):
        try:
            self.install(device)
        except Exception as e:
            raise Exception("Unable to complete test runner installation in all devices")

    def install(self, device):
        # make /mnt/sdcard and /system writable
        result_code = adb.sudo_shell_command(device, "mount -o rw,remount rootfs /",
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to install test runner on device: " + device.name)

        result_code = adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard",
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to install test runner on device: " + device.name)

        result_code = adb.sudo_shell_command(device, "mount -o rw,remount /system",
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to install test runner on device: " + device.name)

        # push
        filename = adb.sudo_push(device, self.test_runner_jar_path, "/system/framework/" + self.test_runner_name + ".jar",
                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        result_code = adb.sudo_shell_command(device, "chmod 777 /system/framework/" + filename,
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to install test runner on device: " + device.name)

        filename = adb.sudo_push(device, self.test_runner_executable_path, "/system/bin/" + self.test_runner_name,
                                 timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        result_code = adb.sudo_shell_command(device, "chmod 777 /system/bin/" + filename,
                                             timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception("Unable to install test runner on device: " + device.name)

        # recover permission
        adb.sudo_shell_command(device, "mount -o ro,remount /system", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        return True
