# coding=utf-8
import json
from typing import TYPE_CHECKING

from dependency_injection.required_feature import RequiredFeature
from devices import adb
from test_runner.evolutiz.widget_action_result import WidgetActionResult

if TYPE_CHECKING:
    from devices.device import Device

class WidgetAction(object):

    def __init__(self, properties):
        self.properties = properties

    def execute(self, device: 'Device', evolutiz_connector) -> None:
        package_name = RequiredFeature('package_name').request()
        evolutiz_connector.send_command(device, package_name,
                                        f"performview XXXX")

    @classmethod
    def random(cls, device: 'Device', evolutiz_connector) -> WidgetActionResult:
        package_name = RequiredFeature('package_name').request()

        current_activity = adb.get_current_activity(device)
        if 'com.google.android' in current_activity:
            # we are still in the HOME, and launch-app command failed
            raise Exception(f"An error ocurred launching app {package_name}")

        # indicate Evolutiz test runner to perform any executable action randomly
        # the runner should return the action performed
        action_performed = evolutiz_connector.send_command(device, package_name,
                                                                f"performview random-action {current_activity}")

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
    def from_json(cls, json) -> 'WidgetAction':
        return cls(json)

    @classmethod
    def from_string(cls, string) -> 'WidgetAction':
        _json = json.loads(string)
        return cls(_json)
