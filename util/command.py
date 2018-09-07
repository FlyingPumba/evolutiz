import os
import subprocess

import settings
from dependency_injection.required_feature import RequiredFeature
from util import logger


def is_command_available(command):
    cmd_check = "command -v " + command + " >/dev/null 2>&1"
    result_code = os.system(cmd_check)
    return result_code == 0

def run_cmd(command):
    verbose_level = RequiredFeature('verbose_level').request()
    if verbose_level > 1:
        logger.log_progress("Running command: %s" % command)

    p = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True,
                       timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT, encoding="utf-8")
    return p.stdout, p.stderr