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
        unique_crashes: Set[str],
        logcat_previous_to_run: str,
) -> bool:
    crash_in_logcat = handle_logcat_crash(
        device,
        script_path,
        generation,
        individual_index,
        test_case_index,
        unique_crashes,
        logcat_previous_to_run,
    )

    crash_in_bugreport = handle_bugreport_crash(
        device,
        script_path,
        generation,
        individual_index,
        test_case_index,
        unique_crashes,
    )

    return crash_in_bugreport or crash_in_logcat

def handle_logcat_crash(
    device: Device,
    script_path: str,
    generation: int,
    individual_index: int,
    test_case_index: int,
    unique_crashes: Set[str],
    logcat_previous_to_run: str,
) -> bool:
    result_dir = RequiredFeature('result_dir').request()

    individual_suffix = f"{str(generation)}.{str(individual_index)}.{str(test_case_index)}"
    logcat_crash_path = f"{result_dir}/crashes/logcat_crash.{individual_suffix}"

    logcat_posterior_to_run, errors, result_code = adb.adb_command(device, f"logcat -d | grep \"CovidApplication:\"")

    # Is there a hidden crash inside the Covid19 Android app?
    prev_logcat_lines = logcat_previous_to_run.split("\n")
    post_logcat_lines = logcat_posterior_to_run.split("\n")
    logcat_diff = [line for line in post_logcat_lines if line not in prev_logcat_lines]

    if len(logcat_diff) == 0:
        return False

    # We found a Covid19 crash in the logcat, filter out date and tag from the log
    logcat_diff = list(map(lambda line: line.split("CovidApplication:")[1].strip(), logcat_diff))

    new_file = open(logcat_crash_path, "w")
    new_file.write("\n".join(logcat_diff))
    new_file.close()

    # filter duplicate crashes
    content_str = "".join(logcat_diff)
    if content_str in unique_crashes:
        # caught a crash, but it wasn't a new one
        return True

    unique_crashes.add(content_str)

    # save the script, indicate its ith gen
    os.system(f"cp {script_path} {result_dir}/crashes/script.{individual_suffix}")
    return True


def handle_bugreport_crash(
    device: Device,
    script_path: str,
    generation: int,
    individual_index: int,
    test_case_index: int,
    unique_crashes: Set[str],
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