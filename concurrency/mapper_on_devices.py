import time
from typing import Callable, List, Optional, Tuple, Any, Dict

from concurrency.multiple_queue_consumer_thread import MultipleQueueConsumerThread
from concurrency.queue import Queue
from concurrency.watchdog_thread import WatchDogThread
from dependency_injection.required_feature import RequiredFeature
from devices.device_manager import DeviceManager
from util.integer import Integer


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
        fail_times_limit    Maximum number of times an item can fail before being discarded.
        default_output      Default output in case an item fails more than the allowed number of times.
        extra_args          The extra arguments to be applied to the function: arg_1, ..., arg_n
        extra_kwargs        The extra keyword arguments to be applied to the function: karg_1, ..., karg_m
        minimum_api         Filter devices to be used by minimum API level.
        idle_devices_only   Use only idle devices.
    """

    def __init__(self,
                 func: Callable,
                 items_to_map: Optional[List[Integer]] = None,
                 fail_times_limit: int = 3,
                 default_output: Any = None,
                 extra_args: Tuple = (),
                 extra_kwargs: Optional[Dict[str, Any]] = None,
                 minimum_api: Optional[int] = None,
                 idle_devices_only: bool = False
                 ) -> None:

        self.func = func
        self.items_to_map = items_to_map
        self.extra_args = extra_args

        if extra_kwargs is None:
            self.extra_kwargs: Dict[str, Any] = {}
        else:
            self.extra_kwargs = extra_kwargs

        self.minimum_api = minimum_api
        self.idle_devices_only = idle_devices_only

        self.default_output = default_output
        self.fail_times_limit = fail_times_limit

    def run(self) -> List[Any]:
        device_manager = RequiredFeature('device_manager').request()

        if self.idle_devices_only:
            devices = device_manager.get_idle_devices()
        else:
            devices = device_manager.get_devices()

        if self.minimum_api is not None:
            devices = [device for device in devices if device.api_level() >= self.minimum_api]

        total_devices = len(devices)
        if total_devices == 0:
            raise Exception(f"No devices found with api level greater or equal than "
                            f"{str(self.minimum_api)} to apply function mapper.")

        # prepare devices queue
        devices_to_use = Queue(elements=devices)

        # prepare output queue
        output_queue = Queue()

        # prepare items to map queue
        if self.items_to_map is None:
            for i in range(0, total_devices):
                thread = MultipleQueueConsumerThread(self.func,
                                                     devices_queue=devices_to_use,
                                                     devices_are_consumable=True,
                                                     extra_args=self.extra_args,
                                                     extra_kwargs=self.extra_kwargs,
                                                     output_queue=output_queue,
                                                     fail_times_limit=self.fail_times_limit,
                                                     default_output=self.default_output,
                                                     name=f"MQCThread-{str(i)}")
                thread.start()

            watchdog_thread = WatchDogThread(output_queue, total_devices)
            self.wait_for_watchdog_to_finish(device_manager, watchdog_thread)

        else:
            items_queue = Queue(elements=self.items_to_map)

            for i in range(0, total_devices):
                thread = MultipleQueueConsumerThread(self.func,
                                                     items_queue=items_queue,
                                                     items_are_consumable=True,
                                                     devices_queue=devices_to_use,
                                                     devices_are_consumable=False,
                                                     extra_args=self.extra_args,
                                                     extra_kwargs=self.extra_kwargs,
                                                     output_queue=output_queue,
                                                     fail_times_limit=self.fail_times_limit,
                                                     default_output=self.default_output,
                                                     name=f"MQCThread-{str(i)}")
                thread.start()

            watchdog_thread = WatchDogThread(output_queue, len(self.items_to_map))
            self.wait_for_watchdog_to_finish(device_manager, watchdog_thread)

        # collect output
        results = output_queue.pop_all()
        return results

    def wait_for_watchdog_to_finish(self, device_manager: DeviceManager, watchdog_thread: WatchDogThread) -> None:
        watchdog_thread.start()
        time.sleep(1)

        while not watchdog_thread.finished():
            # ask for idle devices to set state in case one or more devices are booting
            device_manager.get_idle_devices()
            time.sleep(15)

        if not watchdog_thread.finished_successfully():
            raise TimeoutError("Timeout occurred while running MapperOnDevices")
