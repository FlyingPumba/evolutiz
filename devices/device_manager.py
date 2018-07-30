import subprocess as sub
import time

import adb
import logger
import settings


class DeviceManager:
    def __init__(self):
        self.total_emulators = settings.EMULATOR_DEVICE_NUM

        # emulators detected in last get_devices() call
        self.emulators = []

        # real devices detected in last get_devices() call
        self.real_devices = []

        # all devices that can be used
        # this depends on the settings (USE_REAL_DEVICES and USE_EMULATORS)
        self.available_devices = []

        # devices in process of being booted
        self.booting_real_devices = {}

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

        return self.available_devices

    def get_booted_devices(self, refresh=False):
        if refresh:
            self.refresh_available_devices()

        # check if boot animation is over for each device
        devices = []
        for device in self.available_devices:
            p = sub.Popen(adb.adb_cmd_prefix + ' -s ' + device + ' shell getprop init.svc.bootanim',
                          stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
            output, errors = p.communicate()
            if output.strip() == "stopped":
                devices.append(device)

        return devices

    def get_ready_to_install_devices(self, refresh=False):
        if refresh:
            self.refresh_available_devices()

        # check if package manager is ready for each device
        devices = []
        for device in self.available_devices:
            p = sub.Popen(adb.adb_cmd_prefix  + ' -s ' + device + ' shell pm list packages',
                          stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
            output, errors = p.communicate()
            if "Error: Could not access the Package Manager" not in output.strip():
                devices.append(device)

        return devices

    def boot_emulators(self):
        if not settings.USE_EMULATORS:
            return

        self.booting_emulators = 0
        logger.log_progress("\nBooting devices: " + str(0) + "/" + str(self.total_emulators))

        for i in range(0, self.total_emulators):
            device_name = settings.AVD_SERIES + "_" + str(i)
            logger.log_progress("\rBooting devices: " + str(i + 1) + "/" + str(self.total_emulators))

            emulator_cmd = "export QEMU_AUDIO_DRV=none && $ANDROID_HOME/emulator/emulator"

            flags = " -wipe-data -no-boot-anim -writable-system"

            if settings.HEADLESS:
                # -no-window flag can't be at the end
                flags = " -no-window" + flags

            logs = " >/dev/null 2>/dev/null"

            if settings.DEBUG:
                logs = " > " + device_name + ".log 2>" + device_name + ".err"
                flags = flags + " -verbose -debug all"

            sub.Popen(emulator_cmd + ' -avd ' + device_name + flags + logs,
                      stdout=sub.PIPE, stderr=sub.PIPE, shell=True)

    def shutdown_emulators(self):
        for device in self.emulators:
            adb.adb_command(device, "emu kill")
        time.sleep(2)

    def reboot_devices(self, wait_to_be_ready=True):
        if settings.USE_REAL_DEVICES:
            for device in self.real_devices:
                result_code = adb.adb_command(device, "reboot", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
                if result_code != 0:
                    logger.log_progress("\nUnable to reboot device: " + adb.get_device_name(device))
                    logger.log_progress("\nPlease, turn it off and on manually.")
                    raise Exception("Unable to reboot device: " + adb.get_device_name(device))
                else:
                    # successfully rebooted, save booting time
                    self.booting_real_devices[device] = time.time()

        if settings.USE_EMULATORS:
            self.shutdown_emulators()
            self.boot_emulators()

        if wait_to_be_ready:
            total = len(self.available_devices)
            logger.log_progress("\nWaiting for devices to be ready: " + str(0) + "/" + str(total))

            devices = self.get_ready_to_install_devices(refresh=True)
            while len(devices) < total:
                logger.log_progress("\rWaiting for devices to be ready: " + str(len(devices)) + "/" + str(total))
                time.sleep(10)
                devices = self.get_ready_to_install_devices(refresh=True)

            logger.log_progress("\rWaiting for devices to be ready: " + str(len(devices)) + "/" + str(total))

    def clean_sdcard(self):
        if not settings.USE_EMULATORS:
            pass

        for device in self.emulators:
            adb.sudo_shell_command(device, "mount -o rw,remount rootfs", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            adb.sudo_shell_command(device, "chmod 777 /mnt/sdcard", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
            adb.sudo_shell_command(device, "rm -rf /mnt/sdcard/*", timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
