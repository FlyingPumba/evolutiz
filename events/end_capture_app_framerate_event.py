class EndCaptureAppFramerateEvent:

    name = "EndCaptureAppFramerate"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(EndCaptureAppFramerateEvent.name):
            return EndCaptureAppFramerateEvent()
        else:
            return None

    @staticmethod
    def generate():
        return EndCaptureAppFramerateEvent()