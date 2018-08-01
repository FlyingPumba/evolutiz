import multiprocessing as mp

from devices import adb


def push_callback(result_code):
    if result_code != 0: raise Exception("Unable to complete push_to_all operation")


def push_to_all(device_manager, src, target, timeout=None):
    pool = mp.Pool(processes=len(device_manager.get_devices()))
    for device in device_manager.get_devices():
        pool.apply_async(adb.push,
                         args=(device, src, target, timeout),
                         callback=push_callback)

    # wait for all processes to finish
    pool.close()
    pool.join()
