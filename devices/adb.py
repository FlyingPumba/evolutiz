import os
from subprocess import TimeoutExpired

from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.command import run_cmd, RunCmdResult

from typing import Optional, List, Dict, TYPE_CHECKING

if TYPE_CHECKING:
    from devices.device import Device

adb_logs_dir = ""
adb_cmd_prefix: str = "$ANDROID_HOME/platform-tools/adb"
devices_imei: Dict[str, str] = {
    '4df7b14770c52129': '047',
    '4df7b1b874be21a3': '528',
    '4df7009864632199': '494',
    '4df7eff63a752139': '239',
    '4df7c75211d5118f': '697',
    '4df7ff8e527521e9': '532',
    '4df7c21734af2119': '863',
    '4df7ec8e6a72219b': '640',
    '4df7c3bb449221d3': '391'
}

devices_with_root_permissions: List[str] = []

def get_adb_cmd_prefix_for_device(device: 'Device') -> str:
    return "env " + device.get_adb_server_port_prefix() + " " + adb_cmd_prefix + " -s " + device.name

def adb_command(
        device: 'Device',
        command: str,
        timeout: Optional[int] = None,
        retry: int = 1,
        discard_output: bool = False
) -> RunCmdResult:

    cmd = adb_cmd_prefix + " -s " + device.name + " " + command

    tries = 0
    while True:
        tries += 1
        log_adb_command(device, cmd)

        try:
            output, errors, result_code = run_cmd(cmd, timeout=timeout, discard_output=discard_output,
                                                  env={"ANDROID_ADB_SERVER_PORT": str(device.adb_port)})

            if tries >= retry or result_code == 0:
                return output, errors, result_code

        except TimeoutExpired as e:

            if tries >= retry:
                return e.stdout, e.stderr, 124

def get_root_permissions(device: 'Device') -> None:
    if device.name in devices_with_root_permissions:
        return

    output, errors, result_code = adb_command(device, "root", retry=3)
    print(output)
    print(errors)
    if result_code != 0:
        raise Exception("Unable to gain root permissions on device: " + device.name)

    devices_with_root_permissions.append(device.name)

def shell_command(device: 'Device', command: str, timeout: Optional[int] = None, retry: int = 1, discard_output: bool = False) -> RunCmdResult:
    return adb_command(device, "shell " + command, timeout=timeout, retry=retry, discard_output=discard_output)

def sudo_shell_command(device, command, timeout: Optional[int] = None, retry=1, discard_output=False) -> RunCmdResult:
    get_root_permissions(device)
    return shell_command(device, command, timeout=timeout, retry=retry, discard_output=discard_output)

def push(device: 'Device', src: str, dest: str, timeout: Optional[int] = None) -> RunCmdResult:
    return adb_command(device, "push " + src + " " + dest, timeout=timeout)

def push_all(device, src_list, dest, timeout: Optional[int] = None) -> RunCmdResult:
    src_str = " ".join(src_list)
    return adb_command(device, "push " + src_str + " " + dest, timeout=timeout)

def sudo_push(device, src, dest, timeout: Optional[int] = None) -> RunCmdResult:
    get_root_permissions(device)
    return push(device, src, dest, timeout=timeout)

def pull(device, src, dest, timeout: Optional[int] = None) -> RunCmdResult:
    return adb_command(device, "pull " + src + " " + dest, timeout=timeout)

def uninstall(device: 'Device', package_name: str) -> RunCmdResult:
    return adb_command(device, "uninstall " + package_name)

def install(device: 'Device', package_name: str, apk_path: str) -> None:
    verbose_level = RequiredFeature('verbose_level').request()

    output, errors, result_code = adb_command(device, "install " + apk_path)
    if result_code != 0:
        error_msg = "Unable to install apk: " + apk_path + " on device: " + device.name
        if verbose_level > 0:
            print(error_msg)
            print(output)
            print(errors)

        raise Exception(error_msg)

    cmd = get_adb_cmd_prefix_for_device(device) + " shell pm list packages | grep " + package_name
    log_adb_command(device, cmd)

    res = run_cmd(cmd)[0].strip()
    if package_name not in res:
        raise Exception("Unable to install apk: " + apk_path + " on device: " + device.name)

def pkill(device, string) -> int:
    adb_cmd = get_adb_cmd_prefix_for_device(device) + " shell "
    pkill_cmd = adb_cmd + "ps | grep " + string + " | awk '{print $2}' | xargs -I pid " + adb_cmd + "kill pid "

    log_adb_command(device, pkill_cmd)
    try:
        output, errors, result_code = run_cmd(pkill_cmd)
        return result_code
    except TimeoutExpired as e:
        return 124

def set_bluetooth_state(device, enabled, timeout: Optional[int] = None) -> RunCmdResult:
    if enabled:
        # sometimes might not work
        return sudo_shell_command(device, "service call bluetooth_manager 6", timeout=timeout)
    else:
        # the following command is not working. Also tried with number 9.
        return sudo_shell_command(device, "service call bluetooth_manager 8", timeout=timeout)

def set_wifi_state(device, enabled, timeout: Optional[int] = None) -> RunCmdResult:
    if enabled:
        return sudo_shell_command(device, "svc wifi enable", timeout=timeout)
    else:
        # the following command is not working.
        return sudo_shell_command(device, "svc wifi disable", timeout=timeout)

def set_stay_awake_state(device, enabled, timeout: Optional[int] = None) -> RunCmdResult:
    if enabled:
        return sudo_shell_command(device, "svc power stayon true", timeout=timeout)
    else:
        return sudo_shell_command(device, "svc power stayon false", timeout=timeout)

def set_location_state(device, enabled, timeout: Optional[int] = None) -> RunCmdResult:
    if enabled:
        return shell_command(device, "settings put secure location_providers_allowed gps,wifi,network", timeout=timeout)
    else:
        return shell_command(device, "settings put secure location_providers_allowed ' '", timeout=timeout)

def set_brightness(device, value, timeout: Optional[int] = None) -> RunCmdResult:
    # value should be between 0 and 250
    return shell_command(device, "settings put system screen_brightness " + value, timeout=timeout)

def get_battery_level(device) -> Optional[int]:
    output, errors, result_code = shell_command(device, "dumpsys battery", retry=3)

    if result_code != 0:
        return None
    else:
        for line in output.split('\n'):
            if 'level' in line:
                level_str = line.split(':')
                return int(level_str[1].strip())
    return None

def get_imei(device) -> Optional[str]:
    if device.name not in devices_imei:
        adb_cmd = get_adb_cmd_prefix_for_device(device) + " shell "
        imei_cmd = adb_cmd + "dumpsys iphonesubinfo | grep 'Device ID' | cut -d ' ' -f 6 "

        # leave commented to avoid infinite recursion
        # log_adb_command(device, cmd)

        try:
            output, errors, result_code = run_cmd(imei_cmd)
            devices_imei[device.name] = output.strip()

        except TimeoutExpired as e:
            return None

    return devices_imei[device.name]

def start_server() -> None:
    try:
        run_cmd(adb_cmd_prefix + " start-server" + logger.redirect_string())
    except TimeoutExpired as e:
        pass

def kill_server() -> None:
    try:
        run_cmd(adb_cmd_prefix + " kill-server" + logger.redirect_string())
    except TimeoutExpired as e:
        pass

def restart_server() -> None:
    kill_server()
    start_server()

def log_adb_command(device: 'Device', cmd: str) -> None:
    verbose_level = RequiredFeature('verbose_level').request()
    if verbose_level > 0:
        device_adb_log_file = adb_logs_dir + "/" + device.name + "-adb.log"
        os.system("echo \"" + cmd + "\" >> " + device_adb_log_file)

def exists_file(device, file_path) -> bool:
    try:
        output, errors, result_code = shell_command(device, "ls " + file_path)
    except TimeoutExpired:
        return False

    no_file_str = "No such file or directory"
    if output.find(no_file_str) != -1 or errors.find(no_file_str) != -1:
        # no such file was found
        return False
    else:
        # file exist
        return True

def log_evaluation_result(device, result_dir, script, success) -> None:
    verbose_level = RequiredFeature('verbose_level').request()
    if verbose_level > 0:
        device_adb_log_file = result_dir + "/" + device.name + "-evaluations.log"
        os.system("echo \"" + str(success) + " -> " + script + "\" >> " + device_adb_log_file)

def get_api_level(device: 'Device') -> Optional[int]:
    try:
        output, errors, result_code = shell_command(device, "getprop ro.build.version.sdk")
        res = output.strip()
        return int(res)
    except TimeoutExpired:
        return None

def get_android_version(device) -> Optional[int]:
    try:
        output, errors, result_code = shell_command(device, "getprop ro.build.version.release")
        res = output.strip()
        return int(res)
    except TimeoutExpired:
        return None
