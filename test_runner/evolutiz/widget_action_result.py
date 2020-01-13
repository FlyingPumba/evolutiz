# coding=utf-8
import json
from typing import Dict, Any, List

from test_runner.evolutiz import widget_action
from test_runner.test_event import TestEvent


class WidgetActionResult(TestEvent):

    def __init__(self, string: str) -> None:
        self.string = string

        _json = json.loads(string)
        self.previous_state: Dict[str, Any] = _json['previousState']
        self.next_state: Dict[str, Any] = _json['nextState']
        self.widget_action = widget_action.WidgetAction.from_json(_json['widgetAction'])
        self.result_type: str = _json['resultType']

    def __str__(self) -> str:
        return self.string

    def is_outbound(self) -> bool:
        """
        :return: True iff the result of the action led the exploration outside the application under test.
        E.g., pressing back on the Main screen.
        """
        return self.result_type == "SUCCESS_APP_CRASH" or self.result_type == "SUCCESS_OUTBOUND"


EvolutizTestCase = List[WidgetActionResult]
EvolutizTestSuite = List[EvolutizTestCase]

