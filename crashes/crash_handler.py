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

    bugreport_suffix = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(10))
    local_bugreport_path = result_dir + "/bugreport." + bugreport_suffix + ".crash"

    device_bugreport_path = "/mnt/sdcard/bugreport.crash"

    if not adb.exists_file(device, device_bugreport_path, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT):
        # no crash
        return False
    else:
        # save the crash report
        result_code = adb.pull(device, device_bugreport_path, local_bugreport_path,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            adb.reboot(device)
            raise Exception("Failed to retrieve bugreport.crash file from device: " + device)

        # filter duplicate crashes
        with open(local_bugreport_path) as bug_report_file:
            content = ""
            for line_no, line in enumerate(bug_report_file):
                if line_no == 0:
                    # should not caused by android itself
                    if line.startswith("// CRASH: com.android."):
                        adb.shell_command(device, "rm " + device_bugreport_path,
                                          timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
                        os.system("rm " + local_bugreport_path)

                        # caught a crash, but it was Android related
                        return True

                    continue
                content += line
            if content in unique_crashes:
                adb.shell_command(device, "rm " + device_bugreport_path, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
                os.system("rm " + local_bugreport_path)

                # caught a crash, but it wasn't a new one
                return True
            else:
                unique_crashes.add(content)

        individual_suffix = str(gen) + "." + str(pop) + "." + str(index)
        os.system("mv " + local_bugreport_path + " "
                  + result_dir + "/crashes/bugreport." + individual_suffix)

        # save the script, indicate its ith gen
        os.system("cp " + script_path + " "
                  + result_dir + "/crashes/" + "script." + individual_suffix)

        print "### Caught a crash."
        adb.shell_command(device, "rm " + device_bugreport_path, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        return True
