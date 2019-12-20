from typing import Union


class Integer(object):
    def __init__(self, val: int = 0) -> None:
        self._val: int = int(val)

    def __add__(self, val: Union[int, 'Integer']) -> 'Integer':
        if isinstance(val, Integer):
            return Integer(self._val + val._val)
        return Integer(self._val + val)

    def __iadd__(self, val: Union[int, 'Integer']) -> 'Integer':
        if isinstance(val, Integer):
            self._val += val._val
        else:
            self._val += val
        return self

    def __str__(self) -> str:
        return str(self._val)

    def __repr__(self) -> str:
        return f'Integer({self._val})'
