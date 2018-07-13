class PinchZoomEvent:

    name = "PinchZoom"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(PinchZoomEvent.name):
            return PinchZoomEvent()
        else:
            return None

    @staticmethod
    def generate():
        return PinchZoomEvent()