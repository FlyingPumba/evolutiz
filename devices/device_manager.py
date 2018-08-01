import os
import subprocess as sub
import time

import adb
import settings
from devices.device import Device, State
from devices.do_parallel_fail_one_fail_all import DoParallelFailOneFailAll
from devices.emulator import Emulator
from util import logger


class DeviceManager:
    def __init__(self):
        self.total_emulators = settings.EMULATOR_DEVICE_NUM
        self.next_available_emulator_port = 5554

        # all devices that can be used
        # this depends on the settings (USE_REAL_DEVICES and USE_EMULATORS)
        self.reachable_devices = []

        # init available devices
        self.refresh_reachable_devices()

    def refresh_reachable_devices(self):
        p = sub.Popen(adb.adb_cmd_prefix + ' devices', stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
        output, errors = p.communicate()
        lines = output.split("\n")
        for line in lines:
            if "List of devices attached" in line:
                continue

            if "offline" not in line:
                device_name = line.split("\t")[0].strip()
                if "emulator" in line and settings.USE_EMULATORS:
                    self.reachable_devices.append(Emulator(self, device_name, state=State.reachable))
                if "device" in line and settings.USE_REAL_DEVICES:
                    self.reachable_devices.append(Device(self, device_name, state=State.reachable))

        return self.reachable_devices

    def get_devices(self, refresh=False):
        current_time = time.time()
        booting_devices_to_check = [device for device in self.reachable_devices if
                                    device.state is State.booting and
                                    current_time - device.boot_time >= settings.AVD_BOOT_DELAY]

        if refresh or len(booting_devices_to_check) > 0:
            self.refresh_reachable_devices()

        for device in booting_devices_to_check:
            if device in self.reachable_devices:
                # device is back
                device.state = State.reachable
            else:
                # device is still booting
                # update boot time with current one to wait another AVD_BOOT_DELAY seconds
                device.boot_time = time.time()

        return self.reachable_devices

    def get_booted_devices(self, refresh=False):
        if refresh is True:
            # check if boot animation is over for each device
            for device in self.get_devices(refresh=True):
                p = sub.Popen(adb.adb_cmd_prefix + ' -s ' + device.name + ' shell getprop init.svc.bootanim',
                              stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
                output, errors = p.communicate()
                if output.strip() == "stopped":
                    device.set_state(State.booted)

        return [device for device in self.reachable_devices if device.state is State.booted]

    def get_ready_to_install_devices(self, refresh=False):
        if refresh is True:
            # check if package manager is ready for each device
            for device in self.get_devices(refresh=True):
                p = sub.Popen(adb.adb_cmd_prefix + ' -s ' + device.name + ' shell pm list packages',
                              stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
                output, errors = p.communicate()
                if "Error: Could not access the Package Manager" not in output.strip():
                    device.set_state(State.ready)

        return [device for device in self.reachable_devices if device.state is State.ready]

    def flag_device_as_malfunctioning(self, device):
        # remove device from available devices and reboot
        self.reachable_devices.pop(device)
        device.reboot()

    def boot_emulators(self):
        logger.log_progress("\nBooting devices: " + str(0) + "/" + str(self.total_emulators))

        for i in range(0, self.total_emulators):
            logger.log_progress("\rBooting devices: " + str(i + 1) + "/" + str(self.total_emulators))
            emulator = Emulator(self)
            emulator.boot()
            self.reachable_devices.append(emulator)

    def shutdown_emulators(self):
        emulators = [device for device in self.get_devices() if type(device) is Emulator]
        for device in emulators:
            device.shutdown()
        time.sleep(2)

    def reboot_devices(self, wait_to_be_ready=True):
        for device in self.get_devices():
            device.reboot()

        if wait_to_be_ready:
            self.wait_devices_to_be_ready()

    def wait_devices_to_be_ready(self):
        devices_to_wait = 0
        if settings.USE_REAL_DEVICES:
            devices_to_wait += settings.REAL_DEVICE_NUM
        if settings.USE_EMULATORS:
            devices_to_wait += settings.EMULATOR_DEVICE_NUM

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

    def do_parallel_fail_one_fail_all(self, motive, function_to_apply, arguments):
        do_parallel = DoParallelFailOneFailAll(self, motive, function_to_apply, arguments)
        do_parallel.run()

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
