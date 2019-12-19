# coding=utf-8
from typing import List

from test_runner.test_event import TestEvent


class MotifcoreAction(TestEvent):

    def __init__(self, string) -> None:
        self.string = string

    def __str__(self):
        return self.string


MotifcoreTestCase = List[MotifcoreAction]

