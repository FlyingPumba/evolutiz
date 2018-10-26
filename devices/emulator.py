import subprocess as sub
import time

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device, State
from util.command import run_cmd


class Emulator(Device):

    def __init__(self, device_manager, device_name="", state=State.unknown):
        Device.__init__(self, device_manager, device_name, state)

        self.avd_manager = RequiredFeature('avd_manager').request()

        if device_name != "":
            # we assume device_name has form "emulator-xxxx"
            self.port = int(device_name.split('-')[1])
        else:
            self.port = None

        self.avd_name = None

    def boot(self, port=None):
        verbose_level = RequiredFeature('verbose_level').request()

        Device.boot(self)

        # ensure the emulator configuration is correct
        self.port = port if port is not None else self.device_manager.get_next_available_emulator_port()
        self.avd_name = self.avd_manager.get_avd_name_for_emulator_port(self.port)
        if not self.avd_manager.avd_name_exists(self.avd_name):
            raise Exception("AVD name " + self.avd_name + " doesn't exist. Check that the provided AVD series (" + self.avd_manager.avd_series + ") is correct.")

        # start custom abd server for this emulator
        self.adb_port = self.device_manager.get_next_available_adb_server_port()
        output, errors, result_code = run_cmd(adb.adb_cmd_prefix + " start-server", env={"ANDROID_ADB_SERVER_PORT": str(self.adb_port)})

        # start emulator
        self.name = "emulator-" + str(self.port)

        emulator_cmd = self.get_adb_server_port_prefix() + " QEMU_AUDIO_DRV=none $ANDROID_HOME/emulator/emulator"

        flags = " -wipe-data -no-boot-anim -writable-system -port " + str(self.port)

        if verbose_level < 3:
            # -no-window flag can't be at the end
            flags = " -no-window" + flags

        logs = " >/dev/null 2>/dev/null"

        if verbose_level > 0:
            logs = " > " + self.avd_name + ".log 2>" + self.avd_name + ".err"
            flags = flags + " -verbose -debug all"

        cmd = emulator_cmd + ' -avd ' + self.avd_name + flags + logs
        sub.Popen(cmd, shell=True)

    def shutdown(self):
        Device.shutdown(self)

        adb.adb_command(self, "emu kill")
        time.sleep(3)

    def reboot(self):
        Device.reboot(self)

        self.shutdown()
        self.boot(port=self.port)

    def clean_sdcard(self):
        adb.get_root_permissions(self)

        adb.adb_command(self, "remount", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(self, "mount -o rw,remount /", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        adb.shell_command(self, "rm -rf /mnt/sdcard/*", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)