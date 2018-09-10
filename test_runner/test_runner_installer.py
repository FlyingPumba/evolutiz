import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from concurrency.mapper_on_devices import MapperOnDevices
from util import logger


class TestRunnerInstaller(object):

    def __init__(self, test_runner_name, test_runner_executable_path, test_runner_jar_path):
        self.test_runner_name = test_runner_name
        self.test_runner_executable_path = test_runner_executable_path
        self.test_runner_jar_path = test_runner_jar_path

    def kill_test_runner_in_device(self, device):
        adb.pkill(device, self.test_runner_name)

    def kill_test_runner_in_all_devices(self):
        device_manager = RequiredFeature('device_manager').request()
        for device in device_manager.get_devices():
            self.kill_test_runner_in_device(device)

    def install_in_all_devices(self, minimum_api=None):
        logger.log_progress("\nPreparing " + self.test_runner_name + " test runner in devices.")

        mapper = MapperOnDevices(self.install, minimum_api=minimum_api)
        mapper.run()

    def install(self, device):
        adb.get_root_permissions(device)

        # remount partitions
        output, errors, result_code = adb.adb_command(device, "remount", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
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
