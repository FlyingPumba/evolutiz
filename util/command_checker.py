import os


def is_command_available(command):
    cmd_check = "command -v " + command + " >/dev/null 2>&1"
    result_code = os.system(cmd_check)
    return result_code == 0
