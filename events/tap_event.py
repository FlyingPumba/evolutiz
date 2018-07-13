class TapEvent:

    name = "Tap"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(TapEvent.name):
            return TapEvent()
        else:
            return None

    @staticmethod
    def generate():
        return TapEvent()