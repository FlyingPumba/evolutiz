import os
import subprocess

import logger
import settings

adb_logs_dir = ""
adb_cmd_prefix = "$ANDROID_HOME/platform-tools/adb"
devices_imei = {}

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

def push(device, src, dest):
    return adb_command(device, "push " + src + " " + dest)

def sudo_push(device, src, dest):
    filename = os.path.basename(src)
    aux_file = "/sdcard/" + filename

    result_code = push(device, src, aux_file)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    result_code = sudo_shell_command(device, "cat " + aux_file + " > " + dest)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    result_code = sudo_shell_command(device, "rm " + aux_file)
    if result_code != 0: raise Exception("Unable to sudo_push file: " + src)

    return filename

def pull(device, src, dest):
    return adb_command(device, "pull " + src + " " + dest)

def uninstall(device, package_name):
    return adb_command(device, "uninstall " + package_name)

def install(device, package_name, apk_path):
    result_code = adb_command(device, "install " + apk_path, timeout=True)
    if result_code != 0: raise Exception("Unable to install apk: " + apk_path + " on device: " + device)

    cmd = adb_cmd_prefix + " -s " + device + " shell pm list packages | grep " + package_name
    log_adb_command(device, cmd)

    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    if package_name not in res:
        raise Exception("Unable to install apk: " + apk_path + " on device: " + device)

def pkill(device, string):
    adb_cmd = adb_cmd_prefix + " -s " + device + " shell "
    pkill_cmd = adb_cmd + "ps | grep " + string + " | awk '{print $2}' | xargs -I pid " + adb_cmd + "kill pid "
    log_adb_command(device, pkill_cmd)

    return os.system(pkill_cmd + logger.redirect_string())

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
