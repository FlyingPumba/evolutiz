from events.event import Event
from events.java_numbers_generator import *

class DispatchKeyEvent(Event):

    name = "DispatchKey"

    def __init__(self, downTime, eventTime, action, code, repeat, metaState, device, scancode):
        self.arguments = [
            ('downTime', downTime),
            ('eventTime', eventTime),
            ('action', action),
            ('code', code),
            ('repeat', repeat),
            ('metaState', metaState),
            ('device', device),
            ('scancode', scancode)
        ]

    @staticmethod
    def parse(command_name, args):
        if command_name == DispatchKeyEvent.name and len(args) == 8:
            try:
                return DispatchKeyEvent(
                    long(args[0]),
                    long(args[1]),
                    int(args[2]),
                    int(args[3]),
                    int(args[4]),
                    int(args[5]),
                    int(args[6]),
                    int(args[7]))
            except ValueError:
                # one or more of the arguments were invalid
                return None
        else:
            return None

    @staticmethod
    def generate():
        return DispatchKeyEvent(
            get_random_long(),
            get_random_long(),
            get_random_integer(),
            get_random_integer(),
            get_random_integer(),
            get_random_integer(),
            get_random_integer(),
            get_random_integer())