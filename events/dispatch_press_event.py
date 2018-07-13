class DispatchPressEvent:

    name = "DispatchPress"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(DispatchPressEvent.name):
            return DispatchPressEvent()
        else:
            return None

    @staticmethod
    def generate():
        return DispatchPressEvent()