import threading

from dependency_injection.required_feature import RequiredFeature
from devices.device_state import State
from devices.emulator import Emulator


class DeviceSetupThread(threading.Thread):

    def __init__(self, device: Emulator) -> None:
        super().__init__(name="SetupThread-"+device.name)

        self.device = device

    def run(self) -> None:
        DeviceSetupThread.setup(self.device)

    @staticmethod
    def setup(device: Emulator) -> None:
        device.state = State.setting_up

        # give test runner opportunity to install on devices
        test_runner = RequiredFeature('test_runner').request()
        test_runner.test_runner_installer.install(device)

        apk_preparer = RequiredFeature('apk_preparer').request()
        apk_preparer.install_on_device(device)

        device.needs_setup = False
        device.state = State.ready_idle
