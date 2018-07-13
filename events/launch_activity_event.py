class LaunchActivityEvent:

    name = "LaunchActivity"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(LaunchActivityEvent.name):
            return LaunchActivityEvent()
        else:
            return None

    @staticmethod
    def generate():
        return LaunchActivityEvent()