from devices.device import Device


class JacocoAppInstrumentator(object):

    def instrument_device(self, device: Device):
        """
        Prepares the instrumentation on device.

        :param device:
        :return:
        """
        raise NotImplementedError

    def instrument(self) -> None:
        """
        Instruments the application and provides:
        - package_name
        - instrumented_app_path
        :return:
        """
        raise NotImplementedError
