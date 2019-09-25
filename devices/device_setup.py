import threading
from typing import TYPE_CHECKING

from dependency_injection.required_feature import RequiredFeature
from devices.device_state import State

if TYPE_CHECKING:
    from devices.device import Device

class DeviceSetupThread(threading.Thread):

    def __init__(self, device: 'Device') -> None:
        super().__init__(name=f"SetupThread-{device.name}")

        self.device = device

    def run(self) -> None:
        DeviceSetupThread.setup(self.device)

    @staticmethod
    def setup(device: 'Device') -> None:
        device.state = State.setting_up

        # give test runner opportunity to install on devices
        test_runner = RequiredFeature('test_runner').request()
        test_runner.test_runner_installer.install(device)

        apk_preparer = RequiredFeature('apk_preparer').request()
        apk_preparer.install_on_device(device)

        device.needs_setup = False
        device.state = State.ready_idle
