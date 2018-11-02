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

    def update_logbook(self, gen, population):
        self.result_dir = RequiredFeature('result_dir').request()
        self.logbook = RequiredFeature('logbook').request()
        self.stats = RequiredFeature('stats').request()

        record = self.stats.compile(population) if self.stats is not None else {}
        fitness = []
        evaluation = []
        creation = []
        for individual in population:
            fitness.append({
                'index_in_generation': individual.index_in_generation,
                'coverage': individual.fitness.values[0],
                'length': individual.fitness.values[1],
                'crashes': individual.fitness.values[2],
            })
            evaluation.append({
                'index_in_generation': individual.index_in_generation,
                'evaluation_finish_timestamp': individual.evaluation_finish_timestamp,
                'evaluation_elapsed_time': individual.evaluation_elapsed_time,
            })
            creation.append({
                'index_in_generation': individual.index_in_generation,
                'creation_finish_timestamp': individual.creation_finish_timestamp,
                'creation_elapsed_time': individual.creation_elapsed_time,
            })

        record['fitness'] = numpy.array(fitness)
        record['evaluation'] = numpy.array(evaluation)
        record['creation'] = numpy.array(creation)
        self.logbook.record(gen=gen, **record)

    def dump_logbook_to_file(self):
        self.logbook = RequiredFeature('logbook').request()
        self.result_dir = RequiredFeature('result_dir').request()

        logbook_file = open(self.result_dir + "/logbook.pickle", 'wb')
        pickle.dump(self.logbook, logbook_file)
        logbook_file.close()
