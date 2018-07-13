class StartCaptureAppFramerateEvent:

    name = "StartCaptureAppFramerate"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(StartCaptureAppFramerateEvent.name):
            return StartCaptureAppFramerateEvent()
        else:
            return None

    @staticmethod
    def generate():
        return StartCaptureAppFramerateEvent()