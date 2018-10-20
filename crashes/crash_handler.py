import os
import random
import string

import settings
from devices import adb


def handle(device, result_dir, script_path, gen, pop, index, unique_crashes):
    """
    :param device:
    :param apk_dir:
    :param script_path:
    :param gen_str: string, "init" is caused when init,
        "0" is caused when evaluate the init population
    :return: True if it is a real crash
    """

    device_bugreport_path = "/mnt/sdcard/bugreport.crash"

    individual_suffix = str(gen) + "." + str(pop) + "." + str(index)
    local_bugreport_path = result_dir + "/crashes/bugreport." + individual_suffix

    # save the crash report
    output, errors, result_code = adb.pull(device, device_bugreport_path, local_bugreport_path)
    if result_code != 0:
        return False

    # get content
    with open(local_bugreport_path) as bug_report_file:
        content = bug_report_file.read()

    # delete remote file
    adb.shell_command(device, "rm " + device_bugreport_path)

    # should not caused by android itself
    if content[0].startswith("// CRASH: com.android."):
        os.system("rm " + local_bugreport_path)

        # caught a crash, but it was Android related
        return True

    # drop first line
    content = content[1:]

    # filter duplicate crashes
    if content in unique_crashes:
        os.system("rm " + local_bugreport_path)

        # caught a crash, but it wasn't a new one
        return True

    unique_crashes.add(content)

    # save the script, indicate its ith gen
    os.system("cp " + script_path + " " + result_dir + "/crashes/" + "script." + individual_suffix)
    return True
