class LongPressEvent:

    name = "LongPress"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(LongPressEvent.name):
            return LongPressEvent()
        else:
            return None

    @staticmethod
    def generate():
        return LongPressEvent()