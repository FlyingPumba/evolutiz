import time

from queue import Queue, Empty

from dependency_injection.required_feature import RequiredFeature
from util.multiple_queue_consumer_thread import MultipleQueueConsumerThread


class MapperOnDevices(object):
    """Manages parallel execution of a function in the available devices.

    Attributes:
        func                The function to be applied on each device.
                            If items_to_map is provide, it's arity should be
                                func(device, item, arg_1, ..., arg_n, karg_1, ..., karg_m)
                            Otherwise, it's arity should be
                                func(device, arg_1, ..., arg_n, karg_1, ..., karg_m)
        items_to_map        Extra items that will be "consumed" on each function call.
                            If this parameter is provided, the function will be applied until all items have been applied.
                            Otherwise, the function will be applied once per device.
        extra_args          The extra arguments to be applied to the function: arg_1, ..., arg_n
        extra_kwargs        The extra keyword arguments to be applied to the function: karg_1, ..., karg_m
        minimum_api         Filter devices to be used by minimum API level.
        idle_devices_only   Use only idle devices.
    """

    def __init__(self, func, items_to_map=None, extra_args=(), extra_kwargs=None, minimum_api=None, idle_devices_only=False):

        self.func = func
        self.items_to_map = items_to_map
        self.extra_args = extra_args

        if extra_kwargs is None:
            self.extra_kwargs = {}
        else:
            self.extra_kwargs = extra_kwargs

        self.minimum_api = minimum_api
        self.idle_devices_only = idle_devices_only

    def run(self):
        budget_manager = RequiredFeature('budget_manager').request()

        device_manager = RequiredFeature('device_manager').request()
        all_devices = device_manager.get_idle_devices() if self.idle_devices_only else device_manager.get_devices()
        devices = [device for device in all_devices
                   if self.minimum_api is None or device.api_level() >= self.minimum_api]

        total_devices = len(devices)
        if total_devices == 0:
            raise Exception("No devices found with api level greater or equal than " + str(self.minimum_api) +
                            " to apply function mapper.")

        # prepare devices queue
        devices_to_use = Queue(maxsize=total_devices)
        for device in devices:
            devices_to_use.put_nowait(device)

        # prepare items to map queue
        if self.items_to_map is None:

            # prepare output queue
            output_queue = Queue(maxsize=total_devices)

            threads = [MultipleQueueConsumerThread(self.func,
                                                   consumable_items_queues=[devices_to_use],
                                                   extra_args=self.extra_args, extra_kwargs=self.extra_kwargs,
                                                   output_queue=output_queue)
                       for i in range(0, total_devices)]

            for thread in threads:
                thread.start()

            # busy loop checking if we still have budget available
            budget_run_out = False
            while not devices_to_use.empty():
                if not budget_manager.time_budget_available():
                    map(lambda t: t.stop(), threads)
                    budget_run_out = True
                    break
                else:
                    time.sleep(5)

            if not budget_run_out:
                # only join the consumable queue if we are sure thread was able to run until the end
                # and was not preemptively stopped.
                devices_to_use.join()

        else:
            items_queue = Queue(maxsize=len(self.items_to_map))
            for item in self.items_to_map:
                items_queue.put_nowait(item)

            # prepare output queue
            output_queue = Queue(maxsize=len(self.items_to_map))

            threads = [MultipleQueueConsumerThread(self.func,
                                                   recyclable_items_queues=[devices_to_use],
                                                   consumable_items_queues=[items_queue],
                                                   extra_args=self.extra_args,
                                                   extra_kwargs=self.extra_kwargs,
                                                   output_queue=output_queue)
                       for i in range(0, total_devices)]

            for thread in threads:
                thread.start()

            # busy loop checking if we still have budget available
            budget_run_out = False
            while not items_queue.empty():
                if not budget_manager.time_budget_available():
                    map(lambda t: t.stop(), threads)
                    budget_run_out = True
                    break
                else:
                    time.sleep(5)

            if not budget_run_out:
                # only join the consumable queue if we are sure thread was able to run until the end
                # and was not preemptively stopped.
                items_queue.join()

        # collect output
        results = []
        while True:
            try:
                output = output_queue.get_nowait()
                results.append(output)
            except Empty as e:
                # no more output values
                break
        return results
