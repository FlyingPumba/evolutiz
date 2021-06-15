import time
from typing import Set, Optional

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util.command import run_cmd
from hashlib import sha256


class Ella(object):

    def __init__(self) -> None:
        super().__init__()
        self.ella_original_folder_path = f"{settings.WORKING_DIR}lib/ella-customized/"
        self.ella_original_output_folder_path = f"{self.ella_original_folder_path}ella-out/"

        self.tcp_relay_android = f"{settings.WORKING_DIR}lib/tcp-relay-android/"

        self.devices_with_ella_folder: Set[str] = set()

        self.tcp_relay_android_package_name = "edu.illinois.cs.ase.reportrelay"

        self.ella_original_port = 23745

    def start_ella(self, device: Device) -> None:
        self.prepare_ella_folder_for_device(device)

        run_cmd(f"find {self.get_device_ella_output_folder_path(device)} -type f -name \"coverage.dat.*\" | "
                f"xargs -I {{}} rm {{}}")

        # start ELLA server
        output, errors, result_code = run_cmd(f"./ella.sh s", cwd=self.get_device_ella_folder_path(device))
        if result_code != 0:
            raise Exception("Unable to start ELLA server")

        # start the TCP relay service on the device
        output, errors, result_code = adb.shell_command(
            device,
            f"am startservice -n {self.tcp_relay_android_package_name}/.RelayService -a start "
            f"--ei pA {self.ella_original_port} --ei pB {self.get_device_tcp_relay_port(device)}")
        if result_code != 0:
            raise Exception("Unable to start the TCP relay service")

        # forward the TCP relay port from the emulator to the local PC
        device.forward_port(self.get_device_tcp_relay_port(device), self.get_device_tcp_relay_port(device))

        # forward, locally, the TCP relay port to the ELLA server port
        run_cmd(f"socat TCP:localhost:{self.get_device_ella_port(device)} TCP:localhost:{self.get_device_tcp_relay_port(device)} &", discard_output=True)

    def stop_ella(self, device: Device) -> None:
        # stop ELLA server
        output, errors, result_code = run_cmd(f"./ella.sh k", cwd=self.get_device_ella_folder_path(device))
        if result_code != 0:
            raise Exception("Unable to stop ELLA server")

        # wait a bit for ELLA server to stop (it is not quite immediate)
        time.sleep(3)

        # Do NOT stop the TCP relay service, since it might cause the crash of the ELLA instrumented app
        # output, errors, result_code = adb.shell_command(
        #     device,
        #     f"am force-stop {self.tcp_relay_android_package_name}")
        # if result_code != 0:
        #     raise Exception("Unable to stop the TCP relay service")

    def get_current_apk_output_folder(self, device: Optional[Device] = None) -> str:
        self.app_path: str = RequiredFeature('app_path').request()

        # find folder in ELLa's output folder that has the APK's path (or its hash if it is too long) in its name
        aux = self.app_path.replace("/", "_")
        if len(aux) > 100:
            aux = sha256(aux.encode()).hexdigest()

        output, errors, result_code = run_cmd(
            f"find -L {self.ella_original_output_folder_path if device is None else self.get_device_ella_output_folder_path(device)} -type d -name \"*{aux}*\"")
        folder = output.rstrip('\n')

        if folder == '':
            raise Exception(f"Unable to find ELLA output folder for app {self.app_path}")

        return folder

    def get_coverage_dat_path(self, device: Device) -> str:
        output, errors, result_code = run_cmd(f"find {self.get_current_apk_output_folder(device)} -type f -name \"coverage.dat.*\"")
        if output == "":
            raise Exception(
                f"Unable to find the coverage dat file inside folder: {self.get_current_apk_output_folder(device)}")
        return output.strip().rstrip('\n').rstrip('/')

    def prepare_ella_folder_for_device(self, device: Device):
        if device.name in self.devices_with_ella_folder:
            return

        device_ella_folder_path = self.get_device_ella_folder_path(device)
        device_ella_settings_path = f"{device_ella_folder_path}ella.settings"
        device_ella_port = self.get_device_ella_port(device)

        # copy folder and change port in ELLA settings
        run_cmd(f"rm -rf {device_ella_folder_path}")
        run_cmd(f"cp -r {self.ella_original_folder_path} {device_ella_folder_path}")
        run_cmd(f"sed -i \'s/ella.server.port=23745/ella.server.port={device_ella_port}/g\' {device_ella_settings_path}")

        self.devices_with_ella_folder.add(device.name)

    def get_device_ella_folder_path(self, device: Device):
        return f"{settings.WORKING_DIR}lib/ella-customized-{device.name}/"

    def get_device_ella_output_folder_path(self, device: Device):
        device_ella_folder_path = self.get_device_ella_folder_path(device)
        return f"{device_ella_folder_path}ella-out/"

    def get_device_ella_port(self, device: Device):
        # the base ELLA port begins at 23745 and the emulator port begins at 5554,
        # we will use the first as a base and the second one as an additive to provide
        # a different ELLA port for each device

        port_base = self.ella_original_port + 1 - 5554
        device_ella_port = port_base + device.port
        return device_ella_port

    def get_device_tcp_relay_port(self, device: Device):
        # the base ELLA port begins at 23745 and the emulator port begins at 5554,
        # we will use the first as a base and the second one as an additive to provide
        # a different TCP relay port for each device

        port_base = self.ella_original_port + 2 - 5554
        device_tcp_relay_port = port_base + device.port
        return device_tcp_relay_port
