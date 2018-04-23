import os
import subprocess

import logger
import settings

adb_cmd_prefix = "$ANDROID_HOME/platform-tools/adb"

def adb_command(device, command, timeout = False):
    adb_cmd = adb_cmd_prefix + " -s " + device + " " + command
    print "executing adb command: ", command
    if timeout:
        os.system(settings.TIMEOUT_CMD + " " + str(settings.EVAL_TIMEOUT) + " " + adb_cmd + logger.redirect_string())
    else:
        os.system(adb_cmd + logger.redirect_string())

def shell_command(device, command, timeout = False):
    adb_command(device, "shell " + command, timeout)
    #os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " shell " + command)

def sudo_shell_command(device, command, timeout = False):
    shell_command(device, "\" su -s sh -c '" + command + "'\"", timeout)
    # os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " shell \" su -c '" + command + "'\"")

def push(device, src, dest):
    adb_command(device, "push " + src + " " + dest)

def sudo_push(device, src, dest):
    filename = os.path.basename(src)
    aux_file = "/sdcard/" + filename
    push(device, src, aux_file)
    sudo_shell_command(device, "cat " + aux_file + " > " + dest)
    sudo_shell_command(device, "rm " + aux_file)
    return filename

def pull(device, src, dest):
    adb_command(device, "pull " + src + " " + dest)


def uninstall(device, package_name):
    adb_command(device, "uninstall " + package_name)

def install(device, package_name, apk_path):
    adb_command(device, "install " + apk_path)

    cmd = adb_cmd_prefix + " -s " + device + " shell pm list packages | grep " + package_name
    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    if package_name not in res:
        raise Exception("Unable to install apk: " + apk_path)

def pkill(device, string):
    adb_cmd = adb_cmd_prefix + " -s " + device + " shell "
    os.system(adb_cmd + "ps | grep " + string + " | awk '{print $2}' | xargs -I pid " + adb_cmd + "kill pid " + logger.redirect_string())

def get_battery_level(device):
    adb_cmd = adb_cmd_prefix + " -s " + device + " shell "
    cmd = adb_cmd + "dumpsys battery | grep level | cut -d ' ' -f 4 "
    res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip()
    return int(res)