# coding=utf-8
import json
from typing import Dict, Any

from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device import Device
from test_runner.evolutiz.evolutiz_connector import EvolutizConnector
from test_runner.evolutiz.widget_action_result import WidgetActionResult
from util import logger


class WidgetAction(object):

    def __init__(self, properties: Dict[str, Any]) -> None:
        self.properties = properties

    def execute(self, device: Device, evolutiz_connector: EvolutizConnector) -> bool:
        package_name = RequiredFeature('compiled_package_name').request()
        result = evolutiz_connector.send_command(device, package_name, f"performview id {self.id()} {self.action_type()}")
        return result.startswith('OK')

    def id(self):
        return self.properties['widget']['id']

    def action_type(self):
        return self.properties['actionType']

    @classmethod
    def random(cls, device: Device, evolutiz_connector: EvolutizConnector) -> WidgetActionResult:
        package_name = RequiredFeature('compiled_package_name').request()

        current_package_name = adb.get_current_package_name(device)
        current_activity = adb.get_current_activity(device)
        if 'com.google.android' in current_activity or 'com.google.android' in current_package_name:
            # we are still in the HOME, and launch-app command failed
            raise Exception(f"An error ocurred launching app {package_name}")

        action_performed = None
        for i in range(0, 3):
            # indicate Evolutiz test runner to perform any executable action randomly.
            # the runner should return the action performed
            action_performed = evolutiz_connector.send_command(device, package_name,
                                                                    f"performview random-action {current_activity}")

            if action_performed.startswith("FAILURE"):
                logger.log_progress(f"An error occurred when performing random action onto activity {current_activity}."
                                    f"Retrying.")
            else:
                break


        if action_performed.startswith("FAILURE"):
            raise Exception(f"An error occurred when performing random action onto activity {current_activity}")

        elif action_performed.startswith("SUCCESS_APP_CRASH"):
            result = action_performed.split("SUCCESS_APP_CRASH:")[1]
            return WidgetActionResult(result)

        elif action_performed.startswith("SUCCESS_OUTBOUND"):
            result = action_performed.split("SUCCESS_OUTBOUND:")[1]
            return WidgetActionResult(result)

        elif action_performed.startswith("SUCCESS_NEW_STATE"):
            result = action_performed.split("SUCCESS_NEW_STATE:")[1]
            return WidgetActionResult(result)

        elif action_performed.startswith("SUCCESS"):
            result = action_performed.split("SUCCESS:")[1]
            return WidgetActionResult(result)

        else:
            raise Exception(f"An error occurred when performing random action onto activity {current_activity}")

    @classmethod
    def from_json(cls, json: Dict[str, Any]) -> 'WidgetAction':
        return cls(json)

    @classmethod
    def from_string(cls, string: str) -> 'WidgetAction':
        _json = json.loads(string)
        return cls(_json)
