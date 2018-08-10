import os
import subprocess

import settings
from util import logger

adb_logs_dir = ""
adb_cmd_prefix = "$ANDROID_HOME/platform-tools/adb"
devices_imei = {
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


def adb_command(device, command, timeout=None, log_output=True):
    adb_cmd = adb_cmd_prefix + " -s " + device.name + " " + command

    if timeout is not None:
        timeout_adb_cmd = settings.TIMEOUT_CMD + " " + str(timeout) + " " + adb_cmd
        log_adb_command(device, timeout_adb_cmd)
        return os.system(timeout_adb_cmd + logger.redirect_string(log_output))
    else:
        log_adb_command(device, adb_cmd)
        return os.system(adb_cmd + logger.redirect_string(log_output))


def shell_command(device, command, timeout=None, log_output=True):
    return adb_command(device, "shell " + command, timeout, log_output)


def sudo_shell_command(device, command, timeout=None, log_output=True):
    return shell_command(device, "\" su -s sh -c '" + command + "'\"", timeout, log_output)


def push(device, src, dest, timeout=None):
    return adb_command(device, "push " + src + " " + dest, timeout=timeout)


def sudo_push(device, src, dest, timeout=None):
    filename = os.path.basename(src)
    aux_file = "/sdcard/" + filename

    result_code = push(device, src, aux_file, timeout=timeout)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    result_code = sudo_shell_command(device, "cat " + aux_file + " > " + dest, timeout=timeout)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    result_code = sudo_shell_command(device, "rm " + aux_file, timeout=timeout)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    return filename


def pull(device, src, dest, timeout=None):
    return adb_command(device, "pull " + src + " " + dest, timeout=timeout)


def uninstall(device, package_name):
    return adb_command(device, "uninstall " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)


def install(device, package_name, apk_path):
    result_code = adb_command(device, "install " + apk_path, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
    if result_code != 0:
        # we were unable to install the apk in device.
        # Reboot and raise exception
        device.flag_as_malfunctioning()
        raise Exception("Unable to install apk: " + apk_path + " on device: " + device.name)

    cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + \
          adb_cmd_prefix + " -s " + device.name + " shell pm list packages | grep " + package_name
    log_adb_command(device, cmd)

    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    if package_name not in res:
        # we were unable to install
        # Reboot and raise exception
        device.flag_as_malfunctioning()
        raise Exception("Unable to install apk: " + apk_path + " on device: " + device.name)


def pkill(device, string):
    adb_cmd = adb_cmd_prefix + " -s " + device.name + " shell "
    pkill_cmd = adb_cmd + "ps | grep " + string + " | awk '{print $2}' | xargs -I pid " + adb_cmd + "kill pid "
    cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + pkill_cmd
    log_adb_command(device, cmd)

    return os.system(cmd + logger.redirect_string())


def set_bluetooth_state(device, enabled, timeout=None):
    if enabled:
        # sometimes might not work
        return sudo_shell_command(device, "service call bluetooth_manager 6", timeout=timeout)
    else:
        # the following command is not working. Also tried with number 9.
        return sudo_shell_command(device, "service call bluetooth_manager 8", timeout=timeout)


def set_wifi_state(device, enabled, timeout=None):
    if enabled:
        return sudo_shell_command(device, "svc wifi enable", timeout=timeout)
    else:
        # the following command is not working.
        return sudo_shell_command(device, "svc wifi disable", timeout=timeout)


def set_stay_awake_state(device, enabled, timeout=None):
    if enabled:
        return sudo_shell_command(device, "svc power stayon true", timeout=timeout)
    else:
        return sudo_shell_command(device, "svc power stayon false", timeout=timeout)


def set_location_state(device, enabled, timeout=None):
    if enabled:
        return shell_command(device, "settings put secure location_providers_allowed gps,wifi,network", timeout=timeout)
    else:
        return shell_command(device, "settings put secure location_providers_allowed ' '", timeout=timeout)


def set_brightness(device, value, timeout=None):
    # value should be between 0 and 250
    return shell_command(device, "settings put system screen_brightness " + value, timeout=timeout)


def get_battery_level(device):
    while True:
        try:
            adb_cmd = adb_cmd_prefix + " -s " + device.name + " shell "
            battery_cmd = adb_cmd + "dumpsys battery | grep level | cut -d ' ' -f 4 "
            cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + battery_cmd
            log_adb_command(device, cmd)

            res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
            return int(res)
        except Exception as e:
            # device.flag_as_malfunctioning()
            # raise Exception("There was an error fetching battery level for device: " + device.name)

            # TODO: we should be able to remove the while True and flag the device as malfunctioning when unable to fetch
            # battery level, but it seems is very normal for this command to fail
            pass


def get_imei(device):
    if device.name not in devices_imei:
        adb_cmd = adb_cmd_prefix + " -s " + device.name + " shell "
        imei_cmd = adb_cmd + "dumpsys iphonesubinfo | grep 'Device ID' | cut -d ' ' -f 6 "
        cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + imei_cmd
        # leave commented to avoid infinite recursion
        # log_adb_command(device, cmd)

        res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
        devices_imei[device.name] = res

    return devices_imei[device.name]


def restart_server():
    # print "### killall adb"
    # os.system("kill -9 $(lsof -i:5037 | tail -n +2 | awk '{print $2}')" + logger.redirect_string())
    # os.system("killall adb" + logger.redirect_string())
    os.system(settings.TIMEOUT_CMD + " " + str(
        settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + adb_cmd_prefix + " kill-server" + logger.redirect_string())
    os.system(settings.TIMEOUT_CMD + " " + str(
        settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + adb_cmd_prefix + " devices" + logger.redirect_string())


def log_adb_command(device, cmd):
    device_adb_log_file = adb_logs_dir + "/" + device.name + "-adb.log"
    os.system("echo \"" + cmd + "\" >> " + device_adb_log_file)


def exists_file(device, file_path, timeout=None):
    adb_cmd = adb_cmd_prefix + " -s " + device.name + " shell ls " + file_path
    if timeout:
        adb_cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + adb_cmd

    log_adb_command(device, adb_cmd)

    p = subprocess.Popen(adb_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    output, errors = p.communicate()

    no_file_str = "No such file or directory"
    if output.find(no_file_str) != -1 or errors.find(no_file_str) != -1:
        # no such file was found
        return False
    else:
        # file exist
        return True


def log_evaluation_result(device, result_dir, script, success):
    device_adb_log_file = result_dir + "/" + device.name + "-evaluations.log"
    os.system("echo \"" + str(success) + " -> " + script + "\" >> " + device_adb_log_file)


def get_api_level(device):
    adb_cmd = adb_cmd_prefix + " -s " + device.name + " shell "
    api_level_cmd = adb_cmd + " getprop ro.build.version.sdk"
    cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + api_level_cmd
    log_adb_command(device, cmd)

    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    return int(res)


def get_android_version(device):
    adb_cmd = adb_cmd_prefix + " -s " + device.name + " shell "
    android_version_cmd = adb_cmd + " getprop ro.build.version.release"
    cmd = settings.TIMEOUT_CMD + " " + str(settings.ADB_REGULAR_COMMAND_TIMEOUT) + " " + android_version_cmd
    log_adb_command(device, cmd)

    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    return res
