import os
import time
from subprocess import TimeoutExpired

from devices.device_state import State
from devices.real_device import RealDevice
from util.command import run_cmd
from . import adb
import settings
from dependency_injection.required_feature import RequiredFeature
from devices.device import Device
from devices.emulator import Emulator
from util import logger


from typing import List, Tuple
class WaitDevicesTimeout(Exception):
    pass

class DeviceManager(object):
    """Manages the availability of the devices as they are used.

    This involves:
    - Know which devices were last seen using the command 'adb devices'. These devices are marked as reachable.
    - Know which devices already finished booting, and which ones are ready to receive apk installations.
    - Detect when a rebooting device is back.
    - Boot and shutdown emulators.
     """

    def __init__(self) -> None:
        self.emulators_number: int = RequiredFeature('emulators_number').request()
        self.real_devices_number: int = RequiredFeature('real_devices_number').request()

        self.next_available_emulator_port: int = 5554
        self.next_available_adb_server_port: int = 5038

        # all devices that can be used
        self.devices: List[Device] = []

        # init available devices
        self.refresh_reachable_devices()

    def refresh_reachable_devices(self) -> List[Device]:
        emulators_found, real_devices_found = self.parse_adb_devices(5037, 0, 0)

        for adb_port in range(5038, 5038 + self.get_total_number_of_devices_expected()*2, 2):
            emulators_found, real_devices_found = self.parse_adb_devices(adb_port, emulators_found, real_devices_found)

        return self.devices

    def parse_adb_devices(self, adb_port: int, emulators_found: int, real_devices_found: int) -> Tuple[int, int]:
        devices_cmd = adb.adb_cmd_prefix + ' devices'

        try:
            output, errors, result_code = run_cmd(devices_cmd, env={"ANDROID_ADB_SERVER_PORT": str(adb_port)})
        except TimeoutExpired as e:
            return emulators_found, real_devices_found

        error_lines = errors.split("\n")
        for line in error_lines:
            if "daemon not running" in line:
                continue

            if "daemon started successfully" in line:
                continue

            if line.strip() != "":
                raise Exception("There was an error running 'adb devices' command: " + errors)

        lines = output.split("\n")
        for line in lines:
            if "List of devices attached" in line:
                continue

            if line.strip() == "":
                continue

            if "offline" not in line:
                device_name = line.split("\t")[0].strip()

                matching_devices = [device for device in self.devices if device.name == device_name]

                if len(matching_devices) > 0:
                    device = matching_devices.pop(0)
                    if device.state < State.reachable:
                        device.state = State.reachable

                    if type(device) is Emulator:
                        emulators_found += 1
                    else:
                        real_devices_found += 1

                elif "emulator" in line and emulators_found < self.emulators_number:
                    self.devices.append(Emulator(self, device_name, state=State.reachable))
                    emulators_found += 1

                elif "device" in line and real_devices_found < self.real_devices_number:
                    self.devices.append(Device(self, device_name, state=State.reachable))
                    real_devices_found += 1

        return emulators_found, real_devices_found

    def get_devices(self, refresh: bool = False) -> List[Device]:
        current_time: float = time.time()
        booting_devices_to_check = [device for device in self.devices if
                                    device.state is State.booting and
                                    current_time - device.boot_time >= settings.AVD_BOOT_DELAY]

        if refresh or len(booting_devices_to_check) > 0:
            # fetch devices and update the state of devices that finished booting
            self.refresh_reachable_devices()

            devices_still_booting = [device for device in self.devices if
                                     device.state is State.booting and
                                     current_time - device.boot_time >= settings.AVD_BOOT_DELAY]

            for device in devices_still_booting:
                # update boot time with current one to wait another AVD_BOOT_DELAY seconds
                device.boot_time = time.time()

        return self.devices

    def get_booted_devices(self, refresh=False) -> List[Device]:
        if refresh is True:
            # check if boot animation is over for each device
            devices = self.get_devices(refresh=True)
            for device in devices:
                device.check_booted()

        return [device for device in self.devices if device.state is State.booted]

    def get_ready_to_install_devices(self, refresh: bool = False) -> List[Device]:
        if refresh is True:
            # check if package manager is ready for each device
            devices = self.get_devices(refresh=True)
            for device in devices:
                device.check_ready()

        return [device for device in self.devices if device.state is State.ready_idle]

    def get_idle_devices(self) -> List[Device]:
        return self.get_ready_to_install_devices(refresh=True)

    def boot_emulators(self, wait_to_be_ready: bool = False) -> None:
        while True:
            self.next_available_emulator_port = 5554
            self.next_available_adb_server_port = 5038
            self.devices = []
            logger.log_progress("\nBooting devices: " + str(0) + "/" + str(self.emulators_number))

            for i in range(0, self.emulators_number):
                logger.log_progress("\nBooting devices: " + str(i + 1) + "/" + str(self.emulators_number))
                emulator = Emulator(self)
                emulator.boot()
                self.devices.append(emulator)

            if wait_to_be_ready:
                try:
                    self.wait_devices_to_be_ready()
                except WaitDevicesTimeout as e:
                    logger.log_progress("\n" + str(e))
                    logger.log_progress("\nForce kill on all current emulator processes")
                    run_cmd("pgrep emu | xargs kill -9", discard_output=True)
                    time.sleep(5)
                    continue

            break

    def shutdown_emulators(self, remove: bool = False) -> None:
        emulators = [device for device in self.get_devices() if type(device) is Emulator]
        for device in emulators:
            device.shutdown()

        if remove:
            self.next_available_emulator_port = 5554
            self.devices = [device for device in self.get_devices() if type(device) is RealDevice]

    def reboot_devices(self, wait_to_be_ready=False) -> None:
        logger.log_progress("\nRebooting devices.")
        
        for device in self.get_devices():
            device.reboot()

        if wait_to_be_ready:
            self.wait_devices_to_be_ready()

    def wait_devices_to_be_ready(self) -> None:
        devices_to_wait = self.get_total_number_of_devices_expected()
        logger.log_progress("\nWaiting for devices to be ready: " +
                            str(0) + "/" + str(devices_to_wait))

        ready_devices = self.get_ready_to_install_devices(refresh=True)
        time_limit = devices_to_wait * 90  # 1.5 minutes per device
        start_time = time.time()
        while len(ready_devices) < devices_to_wait:
            logger.log_progress("\nWaiting for devices to be ready: " +
                                str(len(ready_devices)) + "/" + str(devices_to_wait))
            time.sleep(3)

            elapsed_time = time.time() - start_time
            if elapsed_time > time_limit:
                raise WaitDevicesTimeout("Devices still not ready after " + str(time_limit) + " seconds.")

            ready_devices = self.get_ready_to_install_devices(refresh=True)

        logger.log_progress("\nWaiting for devices to be ready: " +
                            str(len(ready_devices)) + "/" + str(devices_to_wait))

    def wait_for_battery_threshold(self, battery_threshold=20) -> None:
        while True:
            all_devices_above_threshold = all([level is None or level >= battery_threshold
                                               for level in (device.battery_level() for device in self.get_devices())])
            if all_devices_above_threshold:
                break
            else:
                logger.log_progress("\nWaiting for some devices to reach " + str(battery_threshold) + "% battery level")
                time.sleep(60)  # sleep 1 minute

    def log_devices_battery(self, gen, result_dir) -> None:
        log_file = result_dir + "/battery.log"
        os.system("echo 'Battery levels at gen: " + str(gen) + "' >> " + log_file)

        for device in self.get_devices():
            level = device.battery_level()
            name = device.name
            
            if type(device) is RealDevice:
                name = device.imei()

            os.system("echo '" + name + " -> " + str(level) + "' >> " + log_file)

    def get_next_available_emulator_port(self) -> int:
        port = self.next_available_emulator_port
        self.next_available_emulator_port += 2
        return port

    def get_next_available_adb_server_port(self) -> int:
        port = self.next_available_adb_server_port
        self.next_available_adb_server_port += 2
        return port

    def get_total_number_of_devices_expected(self) -> int:
        return self.real_devices_number + self.emulators_number
