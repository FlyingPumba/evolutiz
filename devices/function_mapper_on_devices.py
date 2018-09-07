from Queue import Queue

from dependency_injection.required_feature import RequiredFeature
from util.queue_consumer_thread import QueueConsumerThread


class FunctionMapperOnDevices(object):
    def __init__(self, func, args=(), kwargs=None, minimum_api=None):

        self.func = func
        self.args = args
        if kwargs is None:
            self.kwargs = {}
        else:
            self.kwargs = kwargs
        self.minimum_api = minimum_api

    def run(self):
        device_manager = RequiredFeature('device_manager').request()
        devices = [device for device in device_manager.get_devices()
                   if self.minimum_api is None or device.api_level() >= self.minimum_api]

        total_devices = len(devices)
        if total_devices == 0:
            raise Exception("No devices found with api level greater or equal than " + str(self.minimum_api) +
                            " to apply function mapper.")

        devices_to_be_processed = Queue(maxsize=total_devices)
        for device in devices:
            devices_to_be_processed.put_nowait(device)

        threads = [QueueConsumerThread(devices_to_be_processed, self.func, args=self.args, kwargs=self.kwargs)
                   for i in range(0, total_devices)]

        map(lambda t: t.start(), threads)
        devices_to_be_processed.join()