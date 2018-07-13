class StartCaptureFramerateEvent:

    name = "StartCaptureFramerate"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(StartCaptureFramerateEvent.name):
            return StartCaptureFramerateEvent()
        else:
            return None

    @staticmethod
    def generate():
        return StartCaptureFramerateEvent()