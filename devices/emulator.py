import time

import subprocess as sub
from typing import Optional, TYPE_CHECKING

from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from devices.device_state import State
from util.command import run_cmd
from util import logger

if TYPE_CHECKING:
    from devices.device_manager import DeviceManager

class Emulator(Device):

    def __init__(self, device_manager: 'DeviceManager', device_name: str = "", state: State = State.unknown) -> None:
        Device.__init__(self, device_manager, device_name, state)

        self.avd_manager = RequiredFeature('avd_manager').request()

        self.port: Optional[int]
        self.adb_port: Optional[int]

        if device_name != "":
            # we assume device_name has form "emulator-xxxx"
            self.port = int(device_name.split('-')[1])
            self.adb_port = self.get_adb_server_port_for_emulator_port(self.port)
        else:
            self.port = None
            self.adb_port = None

        self.avd_name: str = ""

    def boot(self, port: Optional[int] = None, adb_port: Optional[int] = None) -> None:
        verbose_level = RequiredFeature('verbose_level').request()

        Device.boot(self)

        # ensure the emulator configuration is correct
        self.port = port if port is not None else self.device_manager.get_next_available_emulator_port()
        self.avd_name = self.avd_manager.get_avd_name_for_emulator_port(self.port)
        if not self.avd_manager.avd_name_exists(self.avd_name):
            raise Exception("AVD name " + self.avd_name + " doesn't exist. Check that the provided AVD series (" + self.avd_manager.avd_series + ") is correct.")

        # start custom abd server for this emulator
        self.adb_port = adb_port if adb_port is not None else self.device_manager.get_next_available_adb_server_port()
        output, errors, result_code = run_cmd(adb.adb_cmd_prefix + " start-server", env={"ANDROID_ADB_SERVER_PORT": str(self.adb_port)})

        # start emulator
        self.name = "emulator-" + str(self.port)

        emulator_cmd = self.get_adb_server_port_prefix() + " QEMU_AUDIO_DRV=none $ANDROID_HOME/emulator/emulator"

        flags = " -no-snapshot -wipe-data -no-boot-anim -writable-system -port " + str(self.port)

        if verbose_level < 3:
            # -no-window flag can't be at the end
            flags = " -no-window" + flags

        logs = " >/dev/null 2>/dev/null"

        if verbose_level > 0:
            logs = " >logs/" + self.avd_name + ".log 2>logs/" + self.avd_name + ".err"
            flags = flags + " -verbose -debug all"

        cmd = emulator_cmd + ' -avd ' + self.avd_name + flags + logs

        if verbose_level > 1:
            logger.log_progress("\nFiring up emulator with command: " + cmd)

        sub.Popen(cmd, shell=True)

    def shutdown(self) -> None:
        Device.shutdown(self)

        adb.adb_command(self, "emu kill")
        time.sleep(3)

    def reboot(self):
        Device.reboot(self)

        self.shutdown()
        self.boot(port=self.port, adb_port=self.adb_port)

    def get_adb_server_port_for_emulator_port(self, port: int) -> int:
        return port - 516

