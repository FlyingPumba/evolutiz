class DispatchFlipEvent:

    name = "DispatchFlip"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(DispatchFlipEvent.name):
            return DispatchFlipEvent()
        else:
            return None

    @staticmethod
    def generate():
        return DispatchFlipEvent()