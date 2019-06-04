import time

from subprocess import TimeoutExpired
from threading import Lock

import settings
from devices import adb
from devices.device_setup import DeviceSetupThread
from devices.device_state import State


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
        self.needs_setup = False

        self.lock_failures = Lock()
        self.failures = 0
        self.fail_limit = 5

    def __str__(self):
        return self.name

    def register_failure(self):
        self.lock_failures.acquire()
        self.failures += 1
        if self.failures >= self.fail_limit:
            self.failures = 0
            self.flag_as_malfunctioning()
        self.lock_failures.release()

    def flag_as_malfunctioning(self):
        self.reboot()
        self.needs_setup = True

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
        assert not self.needs_setup
        self.state = State.ready_working

    def mark_work_stop(self):
        self.state = State.ready_idle

    def battery_level(self):
        return adb.get_battery_level(self)

    def imei(self):
        return adb.get_imei(self)

    def api_level(self):
        for i in range(5):
            try:
                level = adb.get_api_level(self)
                if level is not None:
                    return level
            except Exception as e:
                time.sleep(20)

        raise Exception("Unable to get API level for device " + self.name)

    def android_version(self):
        return adb.get_android_version(self)

    def set_bluetooth_state(self, enabled):
        adb.set_bluetooth_state(self, enabled, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    def set_wifi_state(self, enabled):
        adb.set_wifi_state(self, enabled, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    def set_location_state(self, enabled):
        adb.set_location_state(self, enabled, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    def clean_sdcard(self):
        adb.get_root_permissions(self)

        adb.adb_command(self, "remount", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(self, "mount -o rw,remount /", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(self, "rm -rf /mnt/sdcard/*", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    def check_ready(self):
        if self.state >= State.ready_idle:
            # don't change the state of devices when it is higher or equal than ready_idle
            return

        try:
            output, errors, result_code = adb.shell_command(self, "pm list packages")
            if "Error: Could not access the Package Manager" not in output.strip() and errors.strip() == "":

                # this device is ready to receive commands, but first we have to check if it needs to be set up
                if not self.needs_setup:
                    self.state = State.ready_idle
                else:
                    device_setup_thread = DeviceSetupThread(self)
                    device_setup_thread.run()

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
