class Integer(object):
    def __init__(self, val=0):
        self._val = int(val)

    def __add__(self, val):
        if isinstance(val, Integer):
            return Integer(self._val + val._val)
        return Integer(self._val + val)

    def __iadd__(self, val):
        self._val += val
        return self

    def __str__(self):
        return str(self._val)

    def __repr__(self):
        return 'Integer(%s)' % self._val
