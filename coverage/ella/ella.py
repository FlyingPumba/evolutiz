import time

import settings
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from util.command import run_cmd
from hashlib import sha256


class Ella(object):

    def __init__(self) -> None:
        super().__init__()
        self.ella_folder_path = f"{settings.WORKING_DIR}lib/ella-customized/"
        self.tcp_relay_android = f"{settings.WORKING_DIR}lib/tcp-relay-android/"

        self.ella_output_folder_path = f"{self.ella_folder_path}ella-out/"

        self.tcp_relay_android_package_name = "edu.illinois.cs.ase.reportrelay"

        self.ella_port = 23745
        self.tcp_relay_port = 23746

    def get_current_apk_output_folder(self) -> str:
        self.app_path: str = RequiredFeature('app_path').request()

        # find folder in ELLa's output folder that has the APK's path (or its hash if it is too long) in its name
        aux = self.app_path.replace("/", "_")
        if len(aux) > 100:
            aux = sha256(aux.encode()).hexdigest()

        output, errors, result_code = run_cmd(
            f"find -L {self.ella_output_folder_path} -type d -name \"*{aux}*\"")
        folder = output.rstrip('\n')

        if folder == '':
            raise Exception(f"Unable to find ELLA output folder fora pp {self.app_path}")

        return folder

    def get_coverage_dat_path(self) -> str:
        output, errors, result_code = run_cmd(f"find {self.get_current_apk_output_folder()} -type f -name \"coverage.dat.*\"")
        if output == "":
            raise Exception(
                f"Unable to find the coverage dat file inside folder: {self.get_current_apk_output_folder()}")
        return output.strip().rstrip('\n').rstrip('/')

    def start_ella(self, device: Device) -> None:
        run_cmd(f"find {self.ella_output_folder_path} -type f -name \"coverage.dat.*\" | "
                f"xargs -I {{}} rm {{}}")

        # start ELLA server
        output, errors, result_code = run_cmd(f"./ella.sh s", cwd=self.ella_folder_path)
        if result_code != 0:
            raise Exception("Unable to start ELLA server")

        # start the TCP relay service on the device
        output, errors, result_code = adb.shell_command(
            device,
            f"am startservice -n {self.tcp_relay_android_package_name}/.RelayService -a start "
            f"--ei pA {self.ella_port} --ei pB {self.tcp_relay_port}")
        if result_code != 0:
            raise Exception("Unable to start the TCP relay service")

        # forward the TCP relay port from the emulator to the local PC
        output, errors, result_code = adb.adb_command(device,
                                                      f"forward tcp:{self.tcp_relay_port} tcp:{self.tcp_relay_port}")
        if result_code != 0:
            raise Exception("Unable to forward TCP relay port from emulator to local PC")

        # forward, locally, the TCP relay port to the ELLA server port
        run_cmd(f"socat TCP:localhost:{self.ella_port} TCP:localhost:{self.tcp_relay_port} &", discard_output=True)

    def stop_ella(self, device: Device) -> None:
        # stop ELLA server
        output, errors, result_code = run_cmd(f"./ella.sh k", cwd=self.ella_folder_path)
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
