import subprocess as sub
import time

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.avd_manager import AvdManager
from devices.device import Device, State


class Emulator(Device):

    def __init__(self, device_manager, device_name="", state=State.unknown):
        Device.__init__(self, device_manager, device_name, state)

        self.avd_manager = AvdManager()

        if device_name != "":
            # we assume device_name has form "emulator-xxxx"
            self.port = int(device_name.split('-')[1])
        else:
            self.port = None

        self.avd_name = None

    def boot(self, port=None):
        Device.boot(self)

        self.port = port if port is not None else self.device_manager.get_next_available_emulator_port()
        self.avd_name = self.avd_manager.get_avd_name_for_emulator_port(self.port)
        if not self.avd_manager.avd_name_exists(self.avd_name):
            raise Exception("AVD name " + self.avd_name + " doesn't exist. Check that the provided AVD series (" + self.avd_manager.avd_series + ") is correct.")

        self.name = "emulator-" + str(self.port)

        emulator_cmd = "export QEMU_AUDIO_DRV=none && $ANDROID_HOME/emulator/emulator"

        flags = " -wipe-data -no-boot-anim -writable-system -port " + str(self.port)

        if settings.HEADLESS:
            # -no-window flag can't be at the end
            flags = " -no-window" + flags

        logs = " >/dev/null 2>/dev/null"

        if settings.DEBUG:
            logs = " > " + self.avd_name + ".log 2>" + self.avd_name + ".err"
            flags = flags + " -verbose -debug all"

        cmd = emulator_cmd + ' -avd ' + self.avd_name + flags + logs
        sub.Popen(cmd, stdout=sub.PIPE, stderr=sub.PIPE, shell=True)

    def shutdown(self):
        Device.shutdown(self)

        adb.adb_command(self, "emu kill")
        time.sleep(5)

    def reboot(self):
        Device.reboot(self)

        # FIX: this shutdown and boot doesn't seem to work.
        self.shutdown()
        self.boot(port=self.port)

    def clean_sdcard(self):
        adb.sudo_shell_command(self, "mount -o rw,remount rootfs", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.sudo_shell_command(self, "chmod 777 /mnt/sdcard", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.sudo_shell_command(self, "rm -rf /mnt/sdcard/*", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)