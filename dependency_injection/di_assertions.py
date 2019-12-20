from typing import Any, Callable, Type


def NoAssertion(obj: Any) -> bool: return True

def IsInstanceOf(*classes: Type[Any]) -> Callable[[Any], bool]:
    def test(obj: Any) -> bool: return isinstance(obj, classes)

    return test


def HasAttributes(*attributes: str) -> Callable[[Any], bool]:
    def test(obj: Any) -> bool:
        for each in attributes:
            if not hasattr(obj, each): return False
        return True

    return test


def HasMethods(*methods: str) -> Callable[[Any], bool]:
    def test(obj: Any) -> bool:
        for each in methods:
            try:
                attr = getattr(obj, each)
            except AttributeError:
                return False
            if not callable(attr): return False
        return True

    return test
