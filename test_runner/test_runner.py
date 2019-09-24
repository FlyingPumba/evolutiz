from typing import List

import settings


class TestRunner(object):
    def __init__(self):
        pass

    def prepare_device_for_run(self, device) -> None:
        # commented for performance improvements
        # device.set_bluetooth_state(True)
        # device.set_wifi_state(True)
        # device.set_location_state(True)
        pass

    def write_test_case_to_file(self, content, filename) -> None:
        with open(filename, "w") as script:
            script.write(settings.SCRIPT_HEADER)
            script.write("\n".join(content))
            script.write("\n")

    def get_test_case_content_from_file(self, filename) -> List[str]:
        test_content = []

        with open(filename) as script:
            lines = script.read().split('\n')
            
        is_content = False
        is_skipped_first = False
        for line in lines:
            line = line.strip()
            if line.find("start data >>") != -1:
                is_content = True
                continue
            if is_content and line != "":
                if not is_skipped_first:
                    is_skipped_first = True
                    continue
                if is_skipped_first:
                    test_content.append(line)

        script.close()
        return test_content
