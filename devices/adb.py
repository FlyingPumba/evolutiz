import os
import subprocess

import logger
import settings

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
    '4df7ec8e6a72219b': '640'
}

def adb_command(device, command, timeout = False, log_output=True):
    adb_cmd = adb_cmd_prefix + " -s " + device + " " + command

    if timeout:
        timeout_adb_cmd = settings.TIMEOUT_CMD + " " + str(settings.EVAL_TIMEOUT) + " " + adb_cmd
        log_adb_command(device, timeout_adb_cmd)
        return os.system(timeout_adb_cmd + logger.redirect_string(log_output))
    else:
        log_adb_command(device, adb_cmd)
        return os.system(adb_cmd + logger.redirect_string(log_output))

def shell_command(device, command, timeout = False, log_output=True):
    return adb_command(device, "shell " + command, timeout, log_output)

def sudo_shell_command(device, command, timeout = False, log_output=True):
    return shell_command(device, "\" su -s sh -c '" + command + "'\"", timeout, log_output)

def push(device, src, dest, timeout = False):
    return adb_command(device, "push " + src + " " + dest, timeout=timeout)

def sudo_push(device, src, dest, timeout = False):
    filename = os.path.basename(src)
    aux_file = "/sdcard/" + filename

    result_code = push(device, src, aux_file, timeout=timeout)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    result_code = sudo_shell_command(device, "cat " + aux_file + " > " + dest, timeout=timeout)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    result_code = sudo_shell_command(device, "rm " + aux_file, timeout=timeout)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    return filename

def pull(device, src, dest, timeout = False):
    return adb_command(device, "pull " + src + " " + dest, timeout=timeout)

def uninstall(device, package_name):
    return adb_command(device, "uninstall " + package_name)

def install(device, package_name, apk_path):
    result_code = adb_command(device, "install " + apk_path, timeout=True)
    if result_code != 0:
        # we were unable to install the apk in device.
        # Reboot and raise exception
        reboot(device)
        raise Exception("Unable to install apk: " + apk_path + " on device: " + device)

    cmd = settings.TIMEOUT_CMD + " " + str(settings.EVAL_TIMEOUT) + " " + adb_cmd_prefix + " -s " + device + " shell pm list packages | grep " + package_name
    log_adb_command(device, cmd)

    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    if package_name not in res:
        # we were unable to install
        # Reboot and raise exception
        reboot(device)
        raise Exception("Unable to install apk: " + apk_path + " on device: " + device)

def pkill(device, string):
    adb_cmd = adb_cmd_prefix + " -s " + device + " shell "
    pkill_cmd = adb_cmd + "ps | grep " + string + " | awk '{print $2}' | xargs -I pid " + adb_cmd + "kill pid "
    log_adb_command(device, pkill_cmd)

    return os.system(pkill_cmd + logger.redirect_string())

def reboot(device):
    adb_command(device, "reboot")

def set_bluetooth_state(device, enabled, timeout = False):
    if enabled:
        # sometimes might not work
        return sudo_shell_command(device, "service call bluetooth_manager 6", timeout=timeout)
    else:
        # the following command is not working. Also tried with number 9.
        return sudo_shell_command(device, "service call bluetooth_manager 8", timeout=timeout)

def set_wifi_state(device, enabled, timeout = False):
    if enabled:
        return sudo_shell_command(device, "svc wifi enable", timeout=timeout)
    else:
        # the following command is not working.
        return sudo_shell_command(device, "svc wifi disable", timeout=timeout)

def set_stay_awake_state(device, enabled, timeout = False):
    if enabled:
        return sudo_shell_command(device, "svc power stayon true", timeout=timeout)
    else:
        return sudo_shell_command(device, "svc power stayon false", timeout=timeout)

def set_location_state(device, enabled, timeout = False):
    if enabled:
        return shell_command(device, "settings put secure location_providers_allowed gps,wifi,network", timeout=timeout)
    else:
        return shell_command(device, "settings put secure location_providers_allowed ' '", timeout=timeout)

def set_brightness(device, value, timeout = False):
    # value should be between 0 and 250
    return shell_command(device, "settings put system screen_brightness " + value, timeout=timeout)


def get_battery_level(device):
    adb_cmd = adb_cmd_prefix + " -s " + device + " shell "
    cmd = adb_cmd + "dumpsys battery | grep level | cut -d ' ' -f 4 "
    log_adb_command(device, cmd)

    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    return int(res)

def get_imei(device):
    if device not in devices_imei:
        adb_cmd = adb_cmd_prefix + " -s " + device + " shell "
        cmd = adb_cmd + "dumpsys iphonesubinfo | grep 'Device ID' | cut -d ' ' -f 6 "
	    # leave commented to avoid infinite recursion
        #log_adb_command(device, cmd)

        res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
        devices_imei[device] = res

    return devices_imei[device]

def get_device_name(device):
    if "emulator" in device:
        return device
    else:
        return get_imei(device)

def log_adb_command(device, cmd):
    device_adb_log_file = adb_logs_dir + "/" + get_device_name(device) + "-adb.log"
    os.system("echo \"" + cmd + "\" >> " + device_adb_log_file)

def exists_file(device, file_path, timeout = False):
    adb_cmd = adb_cmd_prefix + " -s " + device + " shell ls " + file_path
    if timeout:
        adb_cmd = settings.TIMEOUT_CMD + " " + str(settings.EVAL_TIMEOUT) + " " + adb_cmd

    log_adb_command(device, adb_cmd)

    p = subprocess.Popen(adb_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    output, errors = p.communicate()

    if output.find("No such file or directory") != -1:
        # no such file was found
        return False
    else:
        # file exist
        return True