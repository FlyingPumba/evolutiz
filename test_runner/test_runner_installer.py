import traceback
from typing import Optional

from concurrency.mapper_on_devices import MapperOnDevices
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util import logger


class TestRunnerInstaller(object):

    def __init__(self, test_runner_name: str, test_runner_executable_path: str, test_runner_jar_path: str) -> None:
        self.test_runner_name = test_runner_name
        self.test_runner_executable_path = test_runner_executable_path
        self.test_runner_jar_path = test_runner_jar_path

    def kill_test_runner_in_device(self, device: Device) -> None:
        adb.pkill(device, self.test_runner_name)

    def kill_test_runner_in_all_devices(self) -> None:
        device_manager = RequiredFeature('device_manager').request()
        for device in device_manager.get_devices():
            self.kill_test_runner_in_device(device)

    def install_in_all_devices(self, minimum_api: Optional[int] = None) -> None:
        logger.log_progress(f"\nPreparing {self.test_runner_name} test runner in devices.")

        mapper = MapperOnDevices(self.install, minimum_api=minimum_api)

        try:
            mapper.run()
        except Exception as e:
            str = traceback.format_exc()
            print(str)

    def install(self, device: Device) -> bool:
        adb.get_root_permissions(device)

        # remount partitions
        output, errors, result_code = adb.adb_command(device, "remount", retry=5)
        if result_code != 0:
            raise Exception(f"Unable to remount partitions on device: {device.name} - {errors} - {output}")

        # make /mnt/sdcard writable
        output, errors, result_code = adb.shell_command(device, "mount -o rw,remount /")
        if result_code != 0:
            raise Exception(f"Unable to remount root partition on device: {device.name}")

        # push
        adb.push(device, self.test_runner_jar_path, f"/system/framework/{self.test_runner_name}.jar")
        output, errors, result_code = adb.shell_command(
            device,
            f"chmod 777 /system/framework/{self.test_runner_name}.jar"
        )
        if result_code != 0:
            raise Exception(f"Unable to install test runner on device: {device.name}")

        adb.push(device, self.test_runner_executable_path, f"/system/bin/{self.test_runner_name}")
        output, errors, result_code = adb.shell_command(device, f"chmod 777 /system/bin/{self.test_runner_name}")
        if result_code != 0:
            raise Exception(f"Unable to install test runner on device: {device.name}")

        return True
