import time

from enum import Enum
from subprocess import TimeoutExpired
from threading import Lock

import settings
from devices import adb
from util.command import run_cmd


class State(Enum):
    """Describes the possible states of a device.

       unknown        Initial state, nothing is known about the device.
       booting        The device is booting and presumably not reachable.
       reachable      The device is reachable.
                      However, it might not be ready to accept some commands (e.g. install apk).
       booted         The device finished booting.
       ready_idle     The device is reachable and fully ready, not currently working.
       ready_working  The device is reachable and fully ready, currently working.

    """
    unknown = 0
    booting = 1
    reachable = 2
    booted = 3
    ready_idle = 4
    ready_working = 5

    def __lt__(self, other):
        return self.value < other.value

    def __le__(self, other):
        return self.value <= other.value

    def __gt__(self, other):
        return self.value > other.value

    def __ge__(self, other):
        return self.value >= other.value


class Device(object):
    """Represents the device entity (whether it is an emulator or a real device)

    Attributes:
        device_manager  The device manager that created this device.
        device_name     The name that appears for this device when running "adb devices" command.
        state           Last known state of the device.
        boot_time    Last known time the device started to boot.
    """

    def __init__(self, device_manager, device_name="", state=State.unknown):
        self.device_manager = device_manager
        self.name = device_name
        self.state = state
        self.boot_time = None
        self.adb_port = None

        self.lock_failures = Lock()
        self.failures = 0

    def __str__(self):
        return self.name

    def register_failure(self):
        self.lock_failures.acquire()
        self.failures += 1
        # TODO: do something if the number of failures is greater than some value
        self.lock_failures.release()

    def flag_as_malfunctioning(self):
        #self.reboot()
        self.state = State.ready_idle

    def boot(self):
        self.state = State.booting
        self.boot_time = time.time()

    def shutdown(self):
        self.state = State.unknown

    def reboot(self):
        self.state = State.booting
        self.boot_time = time.time()

    def mark_work_start(self):
        assert self.state == State.ready_idle
        self.state = State.ready_working

    def mark_work_stop(self):
        self.state = State.ready_idle

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

    def check_ready(self):
        if self.state >= State.ready_idle:
            # don't change the state of devices when it is higher or equal than ready_idle
            return

        try:
            output, errors, result_code = adb.shell_command(self, "pm list packages")
            if "Error: Could not access the Package Manager" not in output.strip() and errors.strip() == "":
                self.state = State.ready_idle
        except TimeoutExpired as e:
            return

    def check_booted(self):
        if self.state >= State.booted:
            # don't change the state of devices when it is higher or equal than booted
            return

        try:
            output, errors, result_code = adb.shell_command(self, "getprop init.svc.bootanim")
            if output.strip() == "stopped" and "error" not in errors.strip():
                self.state = State.booted
        except TimeoutExpired as e:
            return

    def get_adb_server_port_prefix(self):
        return "ANDROID_ADB_SERVER_PORT=" + str(self.adb_port)