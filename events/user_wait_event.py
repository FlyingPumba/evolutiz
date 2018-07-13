class UserWaitEvent:

    name = "UserWait"

    def __init__(self):
        pass

    def mutate(self):
        pass

    @staticmethod
    def parse(command):
        if command.startswith(UserWaitEvent.name):
            return UserWaitEvent()
        else:
            return None

    @staticmethod
    def generate():
        return UserWaitEvent()