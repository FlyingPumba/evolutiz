import os
import pickle
from typing import Dict, List, Tuple

import settings
from dependency_injection.required_feature import RequiredFeature
from generation.individual import Individual


class TestSuiteEvaluator(object):

    def __init__(self) -> None:
        self.test_runner = RequiredFeature('test_runner').request()
        self.result_dir: str = ""

    def dump_individual_to_files(self, individual: Individual) -> Tuple[List[str], Dict[str, int]]:
        self.result_dir = RequiredFeature('result_dir').request()
        main_activity = RequiredFeature('main_activity').request()
        package_name = RequiredFeature('compiled_package_name').request()

        script_path: List[str] = []
        suite_lengths: Dict[str, int] = {}

        for test_case_index, test_case in enumerate(individual):
            length = len(test_case)
            # generate script file list
            filename = f"{self.result_dir}/intermediate/script." \
                       f"{str(individual.generation)}.{str(individual.index_in_generation)}.{str(test_case_index)}"

            with open(filename, "w") as script_file:
                script_file.write(settings.SCRIPT_HEADER)
                script_file.write(f"LaunchActivity({package_name},{main_activity})\n")
                script_file.write("\n".join(map(lambda t: str(t), test_case)))
                script_file.write("\n")

            script = os.path.abspath(filename)
            suite_lengths[script] = length
            script_path.append(script)

        return script_path, suite_lengths

    def dump_hall_of_fame_to_file(self) -> None:
        self.result_dir = RequiredFeature('result_dir').request()
        hall_of_fame = RequiredFeature('hall_of_fame').request()

        hof_file = open(f"{self.result_dir}/hall_of_fame.pickle", 'wb')
        pickle.dump(hall_of_fame, hof_file)
        hof_file.close()

    def dump_logbook_to_file(self) -> None:
        self.logbook = RequiredFeature('logbook').request()
        self.result_dir = RequiredFeature('result_dir').request()

        logbook_file = open(f"{self.result_dir}/logbook.pickle", 'wb')
        pickle.dump(self.logbook, logbook_file)
        logbook_file.close()
