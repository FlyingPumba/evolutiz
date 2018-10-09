import os
import pickle

import numpy
from deap import tools

import settings
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature


class TestSuiteEvaluator(object):

    def __init__(self):
        self.test_runner = RequiredFeature('test_runner').request()
        self.coverage_fetcher = RequiredFeature('coverage_fetcher').request()

        self.stats = RequiredFeature('stats').request()
        self.logbook = RequiredFeature('logbook').request()

        # override future responses of calling RequiredFeature('test_suite_evaluator').request(),
        # this way we have a TestSuiteEvaluator singleton
        # This is useful for example to have a single hall-of-fame across a single run.
        features.provide('test_suite_evaluator', self)

        self.result_dir = None

    def dump_individual_to_files(self, individual):
        self.result_dir = RequiredFeature('result_dir').request()

        script_path = []
        suite_lengths = {}

        for test_case_index, test_case in enumerate(individual):
            # generate script file list
            filename = self.result_dir + "/intermediate/script." + str(individual.generation) + "." + \
                       str(individual.index_in_generation) + "." + str(test_case_index)
            # check that directory exists before creating file
            dirname = os.path.dirname(filename)
            if not os.path.exists(dirname):
                os.makedirs(dirname)
            with open(filename, "w+") as script:
                script.write(settings.SCRIPT_HEADER)

                length = 0
                for line in test_case:
                    script.write(line + "\n")
                    length += 1

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

    def update_logbook(self, gen, population):
        self.result_dir = RequiredFeature('result_dir').request()
        record = self.stats.compile(population) if self.stats is not None else {}
        fitness = []
        for individual in population:
            aux = {
                'coverage': individual.fitness.values[0],
                'length': individual.fitness.values[2],
                'crashes': individual.fitness.values[2],
                'timestamp': individual.fitness.timestamp,
            }
            fitness.append(aux)

        record['fitness'] = numpy.array(fitness)
        self.logbook.record(gen=gen, **record)

    def dump_logbook_to_file(self):
        self.result_dir = RequiredFeature('result_dir').request()
        logbook_file = open(self.result_dir + "/logbook.pickle", 'wb')
        pickle.dump(self.logbook, logbook_file)
        logbook_file.close()