class PressAndHoldEvent:

    name = "PressAndHold"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(PressAndHoldEvent.name):
            return PressAndHoldEvent()
        else:
            return None

    @staticmethod
    def generate():
        return PressAndHoldEvent()