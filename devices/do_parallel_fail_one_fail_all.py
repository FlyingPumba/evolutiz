import multiprocessing.dummy as mp

from util import logger
from util.pickable import pickable_function

successful_devices = 0
total_devices = 0


class DoParallelFailOneFailAll(object):

    def __init__(self, device_manager, motive, function_to_apply, arguments, callback=None):
        self.device_manager = device_manager
        self.motive = motive
        self.function_to_apply = function_to_apply
        self.arguments = arguments
        if callback is None:
            self.callback = self.process_results
        else:
            self.callback = callback

    def function_wrapper(self, device, function_to_apply, arguments):
        try:
            function_to_apply(device, *arguments)
            return True
        except Exception as e:
            print e
            return False

    def process_results(self, success):
        if not success:
            raise Exception("Unable to perform " + self.motive + " on all devices")

        global successful_devices
        successful_devices += 1
        global total_devices
        logger.log_progress("\rPerforming " + self.motive + " on devices: " +
                            str(successful_devices) + "/" + str(total_devices))

    def run(self):
        global successful_devices
        successful_devices = 0

        global total_devices
        total_devices = len(self.device_manager.get_devices())

        logger.log_progress("\nPerforming " + self.motive + " on devices: " +
                            str(successful_devices) + "/" + str(total_devices))
        pool = mp.Pool(processes=total_devices)
        for device in self.device_manager.get_devices():
            pool.apply_async(pickable_function,
                             args=(self, 'function_wrapper', (device, self.function_to_apply, self.arguments,)),
                             callback=self.callback)

        # should wait for all processes finish
        pool.close()
        pool.join()

        logger.log_progress("\nFinished " + self.motive)
        return (successful_devices == total_devices)
