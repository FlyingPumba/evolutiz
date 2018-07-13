class PowerLogEvent:

    name = "PowerLog"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(PowerLogEvent.name):
            return PowerLogEvent()
        else:
            return None

    @staticmethod
    def generate():
        return PowerLogEvent()