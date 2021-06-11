from algorithms.strategy import Strategy
from crashes import crash_handler
from dependency_injection.required_feature import RequiredFeature
from util import logger

class CheckInstrumentation(Strategy):

    def __init__(self) -> None:
        super(CheckInstrumentation, self).__init__()
        self.device_manager = RequiredFeature('device_manager').request()
        self.sampling_size = self.device_manager.get_total_number_of_devices_expected()

    def run(self) -> None:
        app_path = RequiredFeature('app_path').request()

        self.population_generator.generate(self.sampling_size, gen=0)

        # did we crash?
        devices = self.device_manager.get_devices()
        assert len(devices) == 1
        device = devices[0]
        crashed = crash_handler.handle(device, '', 0, 0, 0, set())
        if crashed:
            logger.log_progress(f"\nApp {app_path} CRASHED")
        else:
            logger.log_progress(f"\nApp {app_path} PASSED")
