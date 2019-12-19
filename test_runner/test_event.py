# coding=utf-8
from typing import Sequence


class TestEvent(object):

    @classmethod
    def from_string(cls, string) -> 'TestEvent':
        raise NotImplementedError

TestCase = Sequence[TestEvent]