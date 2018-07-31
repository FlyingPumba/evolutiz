import os

import subprocess as sub
import time

import adb
import logger
import settings
from devices.do_parallel_fail_one_fail_all import DoParallelFailOneFailAll


class DeviceManager:
    def __init__(self):
        self.total_emulators = settings.EMULATOR_DEVICE_NUM

        # emulators detected in last get_devices() call
        self.emulators = []
        self.next_available_emulator_port = 5554

        # real devices detected in last get_devices() call
        self.real_devices = []

        # all devices that can be used
        # this depends on the settings (USE_REAL_DEVICES and USE_EMULATORS)
        self.available_devices = []

        # devices in process of being booted
        self.booting_devices = {}

        # init available devices
        self.refresh_available_devices()

    def refresh_available_devices(self):
        # clean previous detected devices
        self.emulators = []
        self.real_devices = []

        # run adb devices command
        p = sub.Popen(adb.adb_cmd_prefix + ' devices', stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
        output, errors = p.communicate()
        lines = output.split("\n")
        for line in lines:
            if "List of devices attached" in line:
                continue

            if "offline" not in line:
                device = line.split("\t")[0].strip()
                if "emulator" in line:
                    self.emulators.append(device)
                if "device" in line:
                    self.real_devices.append(device)

        # store devices going to be used
        self.available_devices = []
        if settings.USE_EMULATORS:
            self.available_devices.extend(self.emulators)

        if settings.USE_REAL_DEVICES:
            self.available_devices.extend(self.real_devices)

        return self.available_devices

    def get_devices(self, refresh=False):
        if refresh:
            self.refresh_available_devices()

        if len(self.booting_devices.keys()) > 0:
            # check if any rebooting device is back
            # (only check if the device was rebooted more than 2 minutes ago)
            current_time = time.time()
            devices_to_check = []
            for device, boot_time in self.booting_devices.iteritems():
                if boot_time - current_time >= settings.AVD_BOOT_DELAY:
                    devices_to_check.append(device)

            if len(devices_to_check) > 0:
                # refresh availale devices
                self.refresh_available_devices()

                for device in devices_to_check:
                    if device in self.available_devices:
                        # device is back
                        self.booting_devices.pop(device)
                    else:
                        # device is still booting
                        # update booting time with current one to wait another AVD_BOOT_DELAY seconds
                        self.booting_devices[device] = current_time

        return self.available_devices

    def get_booted_devices(self, refresh=False):
        # check if boot animation is over for each device
        devices = []
        for device in self.get_devices(refresh=refresh):
            p = sub.Popen(adb.adb_cmd_prefix + ' -s ' + device + ' shell getprop init.svc.bootanim',
                          stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
            output, errors = p.communicate()
            if output.strip() == "stopped":
                devices.append(device)

        return devices

    def get_ready_to_install_devices(self, refresh=False):
        # check if package manager is ready for each device
        devices = []
        for device in self.get_devices(refresh=refresh):
            p = sub.Popen(adb.adb_cmd_prefix  + ' -s ' + device + ' shell pm list packages',
                          stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
            output, errors = p.communicate()
            if "Error: Could not access the Package Manager" not in output.strip():
                devices.append(device)

        return devices

    def flag_device_as_malfunctioning(self, device):
        # remove device from available devices and reboot
        self.available_devices.pop(device)
        self.reboot_device(device)

    def boot_emulator(self, port=None):
        if port is None:
            port = self.next_available_emulator_port
            self.next_available_emulator_port += 2

        avd_name = self.get_avd_name_for_emulator_port(port)

        emulator_cmd = "export QEMU_AUDIO_DRV=none && $ANDROID_HOME/emulator/emulator"

        flags = " -wipe-data -no-boot-anim -writable-system --port " + str(port)

        if settings.HEADLESS:
            # -no-window flag can't be at the end
            flags = " -no-window" + flags

        logs = " >/dev/null 2>/dev/null"

        if settings.DEBUG:
            logs = " > " + avd_name + ".log 2>" + avd_name + ".err"
            flags = flags + " -verbose -debug all"

        sub.Popen(emulator_cmd + ' -avd ' + avd_name + flags + logs,
                  stdout=sub.PIPE, stderr=sub.PIPE, shell=True)

        emulator_name = "emulator-" + str(port)
        self.booting_devices[emulator_name] = time.time()

    def boot_emulators(self):
        logger.log_progress("\nBooting devices: " + str(0) + "/" + str(self.total_emulators))

        for i in range(0, self.total_emulators):
            logger.log_progress("\rBooting devices: " + str(i + 1) + "/" + str(self.total_emulators))
            self.boot_emulator()

    def shutdown_emulator(self, device):
        adb.adb_command(device, "emu kill")
        self.emulators.remove(device)

    def shutdown_emulators(self):
        for device in self.emulators:
            self.shutdown_emulator(device)
        time.sleep(2)

    def reboot_real_device(self, device):
        result_code = adb.adb_command(device, "reboot", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            logger.log_progress("\nUnable to reboot device: " + adb.get_device_name(device))
            logger.log_progress("\nPlease, turn it off and on manually.")
            raise Exception("Unable to reboot device: " + adb.get_device_name(device))
        else:
            # successfully rebooted, save booting time
            self.booting_devices[device] = time.time()

    def reboot_device(self, device):
        if device in self.real_devices:
            self.reboot_real_device(device)

        if device in self.emulators:
            self.shutdown_emulator(device)
            time.sleep(2)
            port = int(device.split('-')[1])
            self.boot_emulator(port=port)

    def reboot_devices(self, wait_to_be_ready=True):
        for device in self.real_devices:
            self.reboot_real_device(device)

        self.shutdown_emulators()
        self.boot_emulators()

        if wait_to_be_ready:
            logger.log_progress("\nWaiting for devices to be ready: " + str(0) + "/" + str(len(self.available_devices)))

            ready_devices = self.get_ready_to_install_devices(refresh=True)
            while len(self.booting_devices.keys()) > 0 or len(ready_devices) != len(self.available_devices):
                logger.log_progress("\rWaiting for devices to be ready: " + str(len(ready_devices)) + "/" + str(len(self.available_devices)))
                time.sleep(10)
                ready_devices = self.get_ready_to_install_devices(refresh=True)

            logger.log_progress("\rWaiting for devices to be ready: " + str(len(ready_devices)) + "/" + str(len(self.available_devices)))

    def clean_sdcard(self):
        if not settings.USE_EMULATORS:
            pass

        for device in self.emulators:
            adb.sudo_shell_command(device, "mount -o rw,remount rootfs", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            adb.sudo_shell_command(device, "rm -rf /mnt/sdcard/*", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

    def do_parallel_fail_one_fail_all(self, motive, function_to_apply, arguments):
        do_parallel = DoParallelFailOneFailAll(self, motive, function_to_apply, arguments)
        do_parallel.run()

    def wait_for_battery_threshold(self, battery_threshold = 20):
        # check that all devices have enough battery
        while True:
            all_devices_with_battery = True
            for device in self.get_devices():
                level = adb.get_battery_level(device)
                all_devices_with_battery = all_devices_with_battery and level >= battery_threshold

            if all_devices_with_battery:
                break
            else:
                logger.log_progress("\nWaiting for some devices to reach " + str(battery_threshold) + "% battery level")
                time.sleep(60)  # sleep 1 minute

    def log_devices_battery(self, gen, result_dir):
        log_file = result_dir + "/battery.log"
        os.system("echo 'Battery levels at gen: " + str(gen) + "' >> " + log_file)

        for device in self.get_devices():
            level = adb.get_battery_level(device)
            imei = adb.get_imei(device)
            os.system("echo '" + imei + " -> " + str(level) + "' >> " + log_file)

    def get_avd_name_for_emulator_port(self, port):
        avd_index = (port - 5554) / 2
        return settings.AVD_SERIES + "_" + str(avd_index)
