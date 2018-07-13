from events.java_numbers_generator import *

class Event:

    def __init__(self):
        pass

    def mutate(self):
        index = random.randint(0, len(self.arguments) - 1)
        arg_name, current_value = self.arguments[index]
        if isinstance(current_value, long):
            self.arguments[index] = (arg_name, get_random_long())
        else:
            self.arguments[index] = (arg_name, get_random_integer())

    def __str__(self):
        args_str = map(lambda x: str(x[1]), self.arguments).join(',')
        return self.name + '(' + args_str + ')'

    @staticmethod
    def parse(command_name, args):
        return None

    @staticmethod
    def generate():
        return None