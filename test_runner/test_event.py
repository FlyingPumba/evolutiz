# coding=utf-8
from typing import Iterable, List


class TestEvent(object):

    @classmethod
    def from_string(cls, string: str) -> 'TestEvent':
        raise NotImplementedError


TestCase = List[TestEvent]
TestSuite = List[TestCase]
