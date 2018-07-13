class DragEvent:

    name = "Drag"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(DragEvent.name):
            return DragEvent()
        else:
            return None

    @staticmethod
    def generate():
        return DragEvent()