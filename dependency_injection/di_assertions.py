from typing import Any, Callable

def NoAssertion(obj: Any) -> bool: return True

def IsInstanceOf(*classes) -> Callable[[Any], bool]:
    def test(obj): return isinstance(obj, classes)

    return test


def HasAttributes(*attributes) -> Callable[[Any], bool]:
    def test(obj):
        for each in attributes:
            if not hasattr(obj, each): return False
        return True

    return test


def HasMethods(*methods) -> Callable[[Any], bool]:
    def test(obj):
        for each in methods:
            try:
                attr = getattr(obj, each)
            except AttributeError:
                return False
            if not callable(attr): return False
        return True

    return test
