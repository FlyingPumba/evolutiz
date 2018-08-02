import subprocess as sub

import settings
from devices import adb
from devices.device import Device, State
from util import logger


class RealDevice(Device):

    def __init__(self, device_manager, device_name, state=State.unknown):
        Device.__init__(self, device_manager, device_name, state)

    def boot(self, port=None):
        """There is no way to contact a real device if it's shutdown."""
        Device.boot(self)

    def shutdown(self):
        Device.shutdown(self)

        adb.shell_command(self, "reboot -p")

    def reboot(self):
        Device.reboot(self)

        result_code = adb.adb_command(self, "reboot", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_progress("\nUnable to reboot device: " + self.name)
            logger.log_progress("\nPlease, turn it off and on manually.")
            raise Exception("Unable to reboot device: " + self.name)
