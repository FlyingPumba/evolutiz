class EndCaptureFramerateEvent:

    name = "EndCaptureFramerate"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(EndCaptureFramerateEvent.name):
            return EndCaptureFramerateEvent()
        else:
            return None

    @staticmethod
    def generate():
        return EndCaptureFramerateEvent()