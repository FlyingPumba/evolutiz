from threading import Lock

from typing import List, Optional, Any, TypeVar, Generic

Element = TypeVar('Element')

class Queue(Generic[Element]):
    """Queue with simple mutex implementation that allows multi-threading operations.
    """

    def __init__(self, elements: Optional[List[Element]] = None) -> None:
        self._lock_elements = Lock()
        if elements is not None:
            self._elements = elements.copy()
        else:
            self._elements = []

    def size(self) -> int:
        self._lock_elements.acquire()
        n = len(self._elements)
        self._lock_elements.release()
        return n

    def put(self, element: Element) -> None:
        self._lock_elements.acquire()
        self._elements.append(element)
        self._lock_elements.release()

    def pop(self) -> Optional[Element]:
        self._lock_elements.acquire()
        element: Optional[Element] = None
        if len(self._elements) > 0:
            element = self._elements.pop(0)

        self._lock_elements.release()

        return element

    def pop_all(self) -> List[Element]:
        self._lock_elements.acquire()
        all = self._elements.copy()
        self._elements.clear()
        self._lock_elements.release()

        return all

    def pop_with_blacklist(self, blacklist: List[Element]) -> Optional[Element]:
        self._lock_elements.acquire()
        element: Optional[Element] = None
        possible_elements = [element for element in self._elements if str(element) not in blacklist]
        if len(possible_elements) > 0:
            element = possible_elements.pop(0)
            self._elements.remove(element)
        self._lock_elements.release()
        return element
