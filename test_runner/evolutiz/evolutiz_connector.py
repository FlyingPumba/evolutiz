# coding=utf-8
import errno
import io
import select
import socket
import time
from typing import Optional

from devices import adb
from devices.device import Device


class EvolutizConnector(object):

    def __init__(self) -> None:
        self.host = '127.0.0.1'
        self.port = 31337

    def send_command(self, device: 'Device', package_name: str, command: str) -> str:
        """
        :param command: to send through the socket and be run by the runner.
        :return: the response of the runner as a string.
        """

        # ensure there is only one return character at the end of the command
        command = command.rstrip("\n") + "\n"

        # set up evolutiz runner in emulator
        adb.adb_command(device, f"forward tcp:{self.port} tcp:{self.port}")
        adb.shell_command(device, f"evolutiz -p {package_name} -c android.intent.category.LAUNCHER --port {self.port} &", discard_output=True)
        time.sleep(2)

        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((self.host, self.port))

            s.sendall(command.encode('utf-8'))

            data = self.receive_data(s)

            s.close()

        # need to manually kill evolutiz after working
        adb.pkill(device, "evolutiz")

        if data is None:
            raise Exception(f"Unable to parse result of command {command}.")
        else:
            return data.rstrip("\n")

    def receive_data(self, socket) -> Optional[str]:
        buffer = io.BytesIO()
        decoded = None

        # Make the socket non-blocking (see http://docs.python.org/library/socket.html#socket.socket.setblocking)
        socket.setblocking(0)

        run_main_loop = True
        while run_main_loop:
            # Wait for events...
            read_ready, _, _ = select.select([socket], [], [], 1)

            if socket in read_ready:
                # The socket have data ready to be received
                continue_recv = True

                while continue_recv:
                    try:
                        # Try to receive som data
                        recv = socket.recv(1024)
                        buffer.write(recv)
                    except (OSError, IOError) as e:
                        if type(e) is BlockingIOError and e.errno == errno.EWOULDBLOCK:
                            # If e.errno is errno.EWOULDBLOCK, then there might not be more data
                            # look at last character
                            continue_recv = False

                            decoded = self.decode(buffer)
                            if decoded is not None and decoded.endswith("\n"):
                                # there is definitelly no more data
                                run_main_loop = False
                        else:
                            # Unknown error! Print it and tell main loop to stop
                            run_main_loop = False
                            continue_recv = False
                    except Exception as e:
                        # Unknown error! Print it and tell main loop to stop
                        run_main_loop = False
                        continue_recv = False

        # We now have all data we can in "buffer"
        if decoded is None:
            return self.decode(buffer)
        else:
            return decoded

    def decode(self, buffer: io.BytesIO) -> Optional[str]:
        try:
            return buffer.getvalue().decode('utf-8')
        except Exception as e:
            return None
