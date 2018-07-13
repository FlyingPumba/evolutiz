class DispatchStringEvent:

    name = "DispatchString"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(DispatchStringEvent.name):
            return DispatchStringEvent()
        else:
            return None

    @staticmethod
    def generate():
        return DispatchStringEvent()