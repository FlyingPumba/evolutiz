# coding=utf-8

from devices.device import Device
from test_runner.test_event import TestCase


class TestRunner(object):
    def __init__(self) -> None:
        pass

    def prepare_device_for_run(self, device: Device) -> None:
        # commented for performance improvements
        # device.set_bluetooth_state(True)
        # device.set_wifi_state(True)
        # device.set_location_state(True)
        pass

    def register_minimum_api(self) -> None:
        raise NotImplementedError

    def write_test_case_to_file(self, test_case: TestCase, filename: str) -> None:
        raise NotImplementedError

    def get_test_case_from_file(self, filename: str) -> TestCase:
        raise NotImplementedError

    def generate(self, device: Device, package_name: str, script_path: str) -> TestCase:
        raise NotImplementedError
