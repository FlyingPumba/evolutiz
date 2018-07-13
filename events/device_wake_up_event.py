class DeviceWakeUpEvent:

    name = "DeviceWakeUp"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(DeviceWakeUpEvent.name):
            return DeviceWakeUpEvent()
        else:
            return None

    @staticmethod
    def generate():
        return DeviceWakeUpEvent()