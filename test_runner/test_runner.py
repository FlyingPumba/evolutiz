import settings
from devices import adb

class TestRunner():

    def prepare_device_for_run(self, device):
        adb.set_bluetooth_state(device, True, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.set_wifi_state(device, True, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.set_location_state(device, True, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)