from typing import TYPE_CHECKING, Optional

import settings
from devices import adb
from devices.device import Device
from devices.device_state import State
from util import logger

if TYPE_CHECKING:
    from devices.device_manager import DeviceManager

class RealDevice(Device):

    def __init__(self, device_manager: 'DeviceManager', device_name: str, state: State = State.unknown):
        Device.__init__(self, device_manager, device_name, state)

        self.adb_port = 5037

    def boot(self, port: Optional[int]= None) -> None:
        """There is no way to contact a real device if it's shutdown."""
        Device.boot(self)

    def shutdown(self) -> None:
        Device.shutdown(self)

        adb.shell_command(self, "reboot -p")

    def reboot(self) -> None:
        Device.reboot(self)

        output, errors, result_code = adb.adb_command(self, "reboot", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_progress(f"\nUnable to reboot device: {self.name}")
            logger.log_progress("\nPlease, turn it off and on manually.")
            raise Exception(f"Unable to reboot device: {self.name}")
