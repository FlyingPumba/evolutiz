from events.event import Event
from events.java_numbers_generator import *

class DispatchPointerEvent(Event):

    name = "DispatchPointer"

    def __init__(self, downTime, eventTime, action, x, y, pressure, size, metaState, xPrecision, yPrecision, device, edgeFlags, pointerId=None):
        self.arguments = [
            ('downTime', downTime),
            ('eventTime', eventTime),
            ('action', action),
            ('x', x),
            ('y', y),
            ('pressure', pressure),
            ('size', size),
            ('metaState', metaState),
            ('xPrecision', xPrecision),
            ('yPrecision', yPrecision),
            ('device', device),
            ('edgeFlags', edgeFlags),
            ('pointerId', pointerId),
        ]

    @staticmethod
    def parse(command_name, args):
        if command_name == DispatchPointerEvent.name and len(args) == 12:
            try:
                return DispatchPointerEvent(
                    long(args[0]),
                    long(args[1]),
                    int(args[2]),
                    float(args[3]),
                    float(args[4]),
                    float(args[5]),
                    float(args[6]),
                    int(args[7]),
                    float(args[8]),
                    float(args[9]),
                    int(args[10]),
                    int(args[11]))
            except ValueError:
                # one or more of the arguments were invalid
                return None

        elif command_name == DispatchPointerEvent.name and len(args) == 13:
            try:
                return DispatchPointerEvent(
                    long(args[0]),
                    long(args[1]),
                    int(args[2]),
                    float(args[3]),
                    float(args[4]),
                    float(args[5]),
                    float(args[6]),
                    int(args[7]),
                    float(args[8]),
                    float(args[9]),
                    int(args[10]),
                    int(args[11]),
                    int(args[12]))
            except ValueError:
                # one or more of the arguments were invalid
                return None
        else:
            return None

    @staticmethod
    def generate():
        return DispatchPointerEvent(
            get_random_long(),
            get_random_long(),
            get_random_integer(),
            get_random_float(),
            get_random_float(),
            get_random_float(),
            get_random_float(),
            get_random_integer(),
            get_random_float(),
            get_random_float(),
            get_random_integer(),
            get_random_integer())