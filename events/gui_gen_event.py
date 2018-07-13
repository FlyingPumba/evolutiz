class GUIGenEvent:

    name = "GUIGen"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(GUIGenEvent.name):
            return GUIGenEvent()
        else:
            return None

    @staticmethod
    def generate():
        return GUIGenEvent()