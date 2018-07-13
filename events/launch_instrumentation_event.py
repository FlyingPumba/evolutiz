class LaunchInstrumentationEvent:

    name = "LaunchInstrumentation"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(LaunchInstrumentationEvent.name):
            return LaunchInstrumentationEvent()
        else:
            return None

    @staticmethod
    def generate():
        return LaunchInstrumentationEvent()