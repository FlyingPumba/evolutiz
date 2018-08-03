class TestRunner(object):
    def __init__(self):
        pass

    def prepare_device_for_run(self, device):
        device.set_bluetooth_state(True)
        device.set_wifi_state(True)
        device.set_location_state(True)
