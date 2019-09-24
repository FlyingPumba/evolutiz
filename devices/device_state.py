from enum import Enum

class State(Enum):
    """Describes the possible states of a device.

       unknown        Initial state, nothing is known about the device.
       booting        The device is booting and presumably not reachable.
       reachable      The device is reachable.
                      However, it might not be ready to accept some commands (e.g. install apk).
       booted         The device finished booting.
       ready_idle     The device is reachable and ready to receive commands, not currently working.
       setting_up     The device is being set up.
       ready_working  The device is reachable and ready to receive commands, currently working.

    """
    unknown = 0
    booting = 1
    reachable = 2
    booted = 3
    ready_idle = 4
    setting_up = 5
    ready_working = 6

    def __lt__(self, other: 'State') -> bool:
        return int(self.value) < int(other.value)

    def __le__(self, other: 'State') -> bool:
        return int(self.value) <= int(other.value)

    def __gt__(self, other: 'State') -> bool:
        return int(self.value) > int(other.value)

    def __ge__(self, other: 'State') -> bool:
        return int(self.value) >= int(other.value)
