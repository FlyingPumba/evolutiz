import os
import pickle

import numpy
from deap import tools

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature


class TestSuiteEvaluator(object):

    def __init__(self) -> None:
        self.test_runner = RequiredFeature('test_runner').request()
        self.result_dir = None

    def dump_individual_to_files(self, individual):
        self.result_dir = RequiredFeature('result_dir').request()

        script_path = []
        suite_lengths = {}

        for test_case_index, test_case in enumerate(individual):
            length = len(test_case)
            # generate script file list
            filename = self.result_dir + "/intermediate/script." + str(individual.generation) + "." + \
                       str(individual.index_in_generation) + "." + str(test_case_index)

            with open(filename, "w") as script:
                script.write(settings.SCRIPT_HEADER)
                script.write("\n".join(test_case))
                script.write("\n")

            script = os.path.abspath(filename)
            suite_lengths[script] = length
            script_path.append(script)

        return script_path, suite_lengths

    def dump_hall_of_fame_to_file(self):
        self.result_dir = RequiredFeature('result_dir').request()
        if hasattr(self, 'hall_of_fame'):
            hof_file = open(self.result_dir + "/hall_of_fame.pickle", 'wb')
            pickle.dump(self.hall_of_fame, hof_file)
            hof_file.close()

    def dump_logbook_to_file(self):
        self.logbook = RequiredFeature('logbook').request()
        self.result_dir = RequiredFeature('result_dir').request()

        logbook_file = open(self.result_dir + "/logbook.pickle", 'wb')
        pickle.dump(self.logbook, logbook_file)
        logbook_file.close()
