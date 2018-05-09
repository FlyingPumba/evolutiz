# Copyright (c) 2016-present, Ke Mao. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#
#     * The names of the contributors may not be used to endorse or
#       promote products derived from this software without specific
#       prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
import random
import string
import subprocess
import os

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

    if not adb.exists_file(device, device_bugreport_path):
        # no crash
        return False
    else:
        # save the crash report
        result_code = adb.pull(device, device_bugreport_path, local_bugreport_path)
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
                        adb.shell_command(device, "rm " + device_bugreport_path)
                        os.system("rm " + local_bugreport_path)

                        # caught a crash, but it was Android related
                        return True

                    continue
                content += line
            if content in unique_crashes:
                adb.shell_command(device, "rm " + device_bugreport_path)
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
        adb.shell_command(device, "rm " + device_bugreport_path)
        return True
