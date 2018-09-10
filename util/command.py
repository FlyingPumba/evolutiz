import os
import signal
import subprocess

import settings
from dependency_injection.required_feature import RequiredFeature
from util import logger


def is_command_available(command):
    cmd_check = "command -v " + command + " >/dev/null 2>&1"
    result_code = os.system(cmd_check)
    return result_code == 0

def run_cmd(command):
    verbose_level = RequiredFeature('verbose_level').request(none_if_missing=True)
    if verbose_level is not None and verbose_level > 1:
        logger.log_progress("\nRunning command: %s\n" % command)

    try:
        # use exec in order for process kill to also eliminate childs
        # from SO: https://stackoverflow.com/a/13143013/2271834

        # this doesn't work if a semi-colon is used in the command
        assert ";" not in command

        p = subprocess.run("exec " + command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True,
                       timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT, encoding="utf-8")
    except subprocess.TimeoutExpired as timeout:
        if verbose_level is not None and verbose_level > 1:
            logger.log_progress("\nTimeout occurred.\nOn timeout, stdout is : %s, stderr is: %s\n" % (timeout.stdout, timeout.stderr))

        return timeout.stdout, timeout.stderr, 124

    return p.stdout, p.stderr, p.returncode