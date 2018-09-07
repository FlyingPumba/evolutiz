import os
import time

from util.command import run_cmd
from . import adb
import settings
from dependency_injection.required_feature import RequiredFeature
from devices.device import Device, State
from devices.emulator import Emulator
from util import logger


class DeviceManager(object):
    """Manages the availability of the devices as they are used.

    This involves:
    - Know which devices were last seen using the command 'adb devices'. These devices are marked as reachable.
    - Know which devices already finished booting, and which ones are ready to receive apk installations.
    - Detect when a rebooting device is back.
    - Boot and shutdown emulators.
     """

    def __init__(self):
        self.emulators_number = RequiredFeature('emulators_number').request()
        self.real_devices_number = RequiredFeature('real_devices_number').request()

        self.next_available_emulator_port = 5554

        # all devices that can be used
        self.devices = []

        # init available devices
        self.refresh_reachable_devices()

    def refresh_reachable_devices(self):
        emulators_found = 0
        real_devices_found = 0

        devices_cmd = adb.adb_cmd_prefix + ' devices'
        output, errors = run_cmd(devices_cmd)

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

        return self.devices

    def get_devices(self, refresh=False):
        current_time = time.time()
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

    def get_booted_devices(self, refresh=False):
        if refresh is True:
            # check if boot animation is over for each device
            for device in self.get_devices(refresh=True):
                if device.state >= State.booted:
                    # don't change the state of devices when it is higher or equal than booted
                    continue

                output, errors = run_cmd(adb.adb_cmd_prefix + ' -s ' + device.name + ' shell getprop init.svc.bootanim')
                if output.strip() == "stopped" and "error" not in errors.strip():
                    device.state = State.booted

        return [device for device in self.devices if device.state is State.booted]

    def get_ready_to_install_devices(self, refresh=False):
        if refresh is True:
            # check if package manager is ready for each device
            for device in self.get_devices(refresh=True):
                if device.state >= State.ready_idle:
                    # don't change the state of devices when it is higher or equal than ready_idle
                    continue

                output, errors = run_cmd(adb.adb_cmd_prefix + ' -s ' + device.name + ' shell pm list packages')
                if "Error: Could not access the Package Manager" not in output.strip() and errors.strip() == "":
                    device.state = State.ready_idle

        return [device for device in self.devices if device.state is State.ready_idle]

    def get_idle_devices(self):
        return self.get_ready_to_install_devices(refresh=True)

    def flag_device_as_malfunctioning(self, device):
        # remove device from available devices and reboot
        device.reboot()

    def boot_emulators(self, wait_to_be_ready=False):
        logger.log_progress("\nBooting devices: " + str(0) + "/" + str(self.emulators_number))

        for i in range(0, self.emulators_number):
            logger.log_progress("\rBooting devices: " + str(i + 1) + "/" + str(self.emulators_number))
            emulator = Emulator(self)
            emulator.boot()
            self.devices.append(emulator)

        if wait_to_be_ready:
            self.wait_devices_to_be_ready()

    def shutdown_emulators(self):
        emulators = [device for device in self.get_devices() if type(device) is Emulator]
        for device in emulators:
            device.shutdown()

    def reboot_devices(self, wait_to_be_ready=False):
        logger.log_progress("\nRebooting devices.")
        
        for device in self.get_devices():
            device.reboot()

        if wait_to_be_ready:
            self.wait_devices_to_be_ready()

    def wait_devices_to_be_ready(self):
        devices_to_wait = self.get_total_number_of_devices_expected()
        logger.log_progress("\nWaiting for devices to be ready: " +
                            str(0) + "/" + str(devices_to_wait))

        ready_devices = self.get_ready_to_install_devices(refresh=True)
        while len(ready_devices) < devices_to_wait:
            logger.log_progress("\rWaiting for devices to be ready: " +
                                str(len(ready_devices)) + "/" + str(devices_to_wait))
            time.sleep(10)
            ready_devices = self.get_ready_to_install_devices(refresh=True)

        logger.log_progress("\rWaiting for devices to be ready: " +
                            str(len(ready_devices)) + "/" + str(devices_to_wait))

    def wait_for_battery_threshold(self, battery_threshold=20):
        while True:
            all_devices_above_threshold = all(map(lambda device: device.battery_level() >= battery_threshold,
                                                  self.get_devices()))
            if all_devices_above_threshold:
                break
            else:
                logger.log_progress("\nWaiting for some devices to reach " + str(battery_threshold) + "% battery level")
                time.sleep(60)  # sleep 1 minute

    def log_devices_battery(self, gen, result_dir):
        log_file = result_dir + "/battery.log"
        os.system("echo 'Battery levels at gen: " + str(gen) + "' >> " + log_file)

        for device in self.get_devices():
            level = device.battery_level()
            imei = device.imei()
            os.system("echo '" + imei + " -> " + str(level) + "' >> " + log_file)

    def get_next_available_emulator_port(self):
        port = self.next_available_emulator_port
        self.next_available_emulator_port += 2
        return port

    def get_total_number_of_devices_expected(self):
        return self.real_devices_number + self.emulators_number
