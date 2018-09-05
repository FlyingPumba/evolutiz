import os
import subprocess

import settings


def is_command_available(command):
    cmd_check = "command -v " + command + " >/dev/null 2>&1"
    result_code = os.system(cmd_check)
    return result_code == 0

def run_cmd(command):
    p = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True,
                       timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT, encoding="utf-8")
    return p.stdout, p.stderr