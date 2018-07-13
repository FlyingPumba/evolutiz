class WriteLogEvent:

    name = "WriteLog"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(WriteLogEvent.name):
            return WriteLogEvent()
        else:
            return None

    @staticmethod
    def generate():
        return WriteLogEvent()