class RunCmdEvent:

    name = "RunCmd"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(RunCmdEvent.name):
            return RunCmdEvent()
        else:
            return None

    @staticmethod
    def generate():
        return RunCmdEvent()