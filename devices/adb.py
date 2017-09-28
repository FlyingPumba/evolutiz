import os
from random import randint

def adb_command(device, command):
    os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " " + command)

def shell_command(device, command):
    adb_command(device, "shell " + command)
    #os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " shell " + command)

def sudo_shell_command(device, command):
    shell_command(device, "\" su -c '" + command + "'\"")
    # os.system("$ANDROID_HOME/platform-tools/adb -s " + device + " shell \" su -c '" + command + "'\"")

def push(device, src, dest):
    adb_command(device, "push " + src + " " + dest)

def sudo_push(device, src, dest):
    aux_file = "/sdcard/" + str(randint(0, 1000))
    push(device, src, aux_file)
    sudo_shell_command(device, "cp " + aux_file + " " + dest)
    sudo_shell_command(device, "rm " + aux_file)