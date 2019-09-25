import datetime

from dependency_injection.required_feature import RequiredFeature


class IndividualGenerator:

    def get_path_for_test_case(self, generation, individual_index, test_case_index):
        result_dir = RequiredFeature('result_dir').request()
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S.%f")[:-3]

        return f"{result_dir}/intermediate/script.{generation}.{individual_index}.{test_case_index}.{ts}"
