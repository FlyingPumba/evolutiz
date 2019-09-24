import os
import subprocess

import settings
from dependency_injection.required_feature import RequiredFeature
from util import logger

from typing import Dict, Optional, Tuple

RunCmdResult = Tuple[str, str, int]

def is_command_available(command: str) -> bool:
    cmd_check = "command -v " + command + " >/dev/null 2>&1"
    result_code = os.system(cmd_check)
    return result_code == 0

def run_cmd(command: str,
            timeout: Optional[int] = None,
            discard_output: bool = False,
            cwd: Optional[str] = None,
            env: Optional[Dict[str, str]] = None) -> RunCmdResult:

    if timeout is None:
        timeout = settings.ADB_REGULAR_COMMAND_TIMEOUT

    env_str = ""
    if env is not None and len(env) > 0:
        env_str = "env "
        for key, value in env.items():
            env_str += key + "=" + value + " "

    verbose_level = RequiredFeature('verbose_level').request(none_if_missing=True)
    if verbose_level is not None and verbose_level > 1:
        aux = env_str + command
        logger.log_progress("\nRunning command: %s" % aux)

    # use exec in order for process kill to also eliminate childs
    # from SO: https://stackoverflow.com/a/13143013/2271834

    # this doesn't work if a semi-colon is used in the command
    assert ";" not in command

    if discard_output:
        output_file = subprocess.DEVNULL
    else:
        output_file = subprocess.PIPE

    process = subprocess.run("exec " + env_str + command, stdout=output_file, stderr=output_file, shell=True,
                             timeout=timeout, encoding="utf-8", cwd=cwd)

    return process.stdout, process.stderr, process.returncode
