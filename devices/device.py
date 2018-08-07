import time

from enum import Enum

import settings
from devices import adb


class State(Enum):
    """Describes the possible states of a device.

       unknown     Initial state, nothing is known about the device.
       booting     The device is booting and presumably not reachable.
       reachable   The device is reachable.
                   However, it might not be ready to accept some commands (e.g. install apk).
       booted      The device finished booting.
       ready       The device is reachable and fully ready.
    """
    unknown = 0
    booting = 1
    reachable = 2
    booted = 3
    ready = 4


class Device(object):
    """Represents the device entity (whether it is an emulator or a real device)

    Attributes:
        device_manager  The device manager that created this device.
        device_name     The name that appears for this device when running "adb devices" command.
        state           Last known state of the device.
        booting_time    Last known time the device started to boot.
    """

    def __init__(self, device_manager, device_name="", state=State.unknown):
        self.device_manager = device_manager
        self.name = device_name
        self.state = state
        self.boot_time = None

    def flag_as_malfunctioning(self):
        self.device_manager.flag_device_as_malfunctioning(self)

    def boot(self):
        self.state = State.booting
        self.boot_time = time.time()

    def shutdown(self):
        self.state = State.unknown

    def reboot(self):
        self.state = State.booting
        self.boot_time = time.time()

    def battery_level(self):
        return adb.get_battery_level(self)

    def imei(self):
        return adb.get_imei(self)

    def api_level(self):
        return adb.get_api_level(self)

    def android_version(self):
        return adb.get_android_version(self)

    def set_bluetooth_state(self, enabled):
        adb.set_bluetooth_state(self, enabled, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    def set_wifi_state(self, enabled):
        adb.set_wifi_state(self, enabled, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    def set_location_state(self, enabled):
        adb.set_location_state(self, enabled, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

