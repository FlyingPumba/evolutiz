import datetime
from typing import List

from dependency_injection.required_feature import RequiredFeature
from devices.device import Device
from generation.individual import Individual

class IndividualGenerator:

    def get_path_for_test_case(self, generation: int, individual_index: int, test_case_index: int) -> str:
        result_dir = RequiredFeature('result_dir').request()
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

        return f"{result_dir}/intermediate/script.{generation}.{individual_index}.{test_case_index}.{ts}"

    def gen_individual(self, device: Device, individual_index: int, generation: int) -> Individual:
        raise NotImplementedError
