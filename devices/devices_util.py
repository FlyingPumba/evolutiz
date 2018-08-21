from devices import adb
from devices.mapper_on_devices import MapperOnDevices


def push_to_all(src, target, timeout=None):
    mapper = MapperOnDevices(adb.push, extra_args=(src, target, timeout))
    mapper.run()