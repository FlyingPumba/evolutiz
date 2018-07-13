class RotateScreenEvent:

    name = "RotateScreen"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(RotateScreenEvent.name):
            return RotateScreenEvent()
        else:
            return None

    @staticmethod
    def generate():
        return RotateScreenEvent()