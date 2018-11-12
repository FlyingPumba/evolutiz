from threading import Lock


class Queue(object):
    """Queue with simple mutex implementation that allows multi-threading operations.
    """

    def __init__(self, elements=None):
        self._lock_elements = Lock()
        if elements is not None:
            self._elements = elements.copy()
        else:
            self._elements = []

    def size(self):
        self._lock_elements.acquire()
        n = len(self._elements)
        self._lock_elements.release()
        return n

    def put(self, element):
        self._lock_elements.acquire()
        self._elements.append(element)
        self._lock_elements.release()

    def pop(self):
        self._lock_elements.acquire()
        if len(self._elements) > 0:
            element = self._elements.pop(0)
        else:
            element = None
        self._lock_elements.release()

        return element

    def pop_all(self):
        self._lock_elements.acquire()
        all = self._elements.copy()
        self._elements.clear()
        self._lock_elements.release()

        return all

    def pop_with_blacklist(self, blacklist):
        self._lock_elements.acquire()
        possible_elements = [element for element in self._elements if str(element) not in blacklist]
        if len(possible_elements) > 0:
            element = possible_elements.pop(0)
            self._elements.remove(element)
        else:
            element = None
        self._lock_elements.release()
        return element
