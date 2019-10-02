import os
from typing import Set

from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device


def handle(
        device: Device,
        script_path: str,
        generation: int,
        individual_index: int,
        test_case_index: int,
        unique_crashes: Set[str]
) -> bool:

    result_dir = RequiredFeature('result_dir').request()

    device_bugreport_path = "/mnt/sdcard/bugreport.crash"

    individual_suffix = f"{str(generation)}.{str(individual_index)}.{str(test_case_index)}"
    local_bugreport_path = f"{result_dir}/crashes/bugreport.{individual_suffix}"

    # save the crash report
    output, errors, result_code = adb.pull(device, device_bugreport_path, local_bugreport_path)
    if result_code != 0:
        return False

    # get content
    with open(local_bugreport_path) as bug_report_file:
        content = bug_report_file.read().split('\n')

    # delete remote file
    adb.shell_command(device, f"rm {device_bugreport_path}")

    # should not caused by android itself
    if content[0].startswith("// CRASH: com.android."):
        os.system(f"rm {local_bugreport_path}")

        # caught a crash, but it was Android related
        return True

    # drop first line
    content = content[1:]

    # filter duplicate crashes
    content_str = "".join(content)
    if content_str in unique_crashes:
        os.system(f"rm {local_bugreport_path}")

        # caught a crash, but it wasn't a new one
        return True

    unique_crashes.add(content_str)

    # save the script, indicate its ith gen
    os.system(f"cp {script_path} {result_dir}/crashes/script.{individual_suffix}")
    return True
