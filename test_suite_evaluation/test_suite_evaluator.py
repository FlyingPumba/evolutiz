import os
import pickle

from deap import tools

import settings
from dependency_injection.required_feature import RequiredFeature


class TestSuiteEvaluator(object):

    def __init__(self):
        self.test_runner = RequiredFeature('test_runner').request()
        self.coverage_fetcher = RequiredFeature('coverage_fetcher').request()
        self.result_dir = RequiredFeature('result_dir').request()

        self.stats = RequiredFeature('stats').request()
        self.logbook = self.logbook = tools.Logbook()
        self.logbook.header = ['gen'] + (self.stats.fields if self.stats else [])

    def dump_individual_to_files(self, individual, gen, pop):
        script_path = []
        suite_lengths = {}

        for index, seq in enumerate(individual):
            # generate script file list
            filename = self.result_dir + "/intermediate/script." + str(gen) + "." + str(pop) + "." + str(index)
            # check that directory exists before creating file
            dirname = os.path.dirname(filename)
            if not os.path.exists(dirname):
                os.makedirs(dirname)
            with open(filename, "w+") as script:
                script.write(settings.SCRIPT_HEADER)

                length = 0
                for line in seq:
                    script.write(line + "\n")
                    length += 1

            script = os.path.abspath(filename)
            suite_lengths[script] = length
            script_path.append(script)

        return script_path, suite_lengths

    def dump_hall_of_fame_to_file(self):
        if hasattr(self, 'hall_of_fame'):
            hof_file = open(self.result_dir + "/hall_of_fame.pickle", 'wb')
            pickle.dump(self.hall_of_fame, hof_file)
            hof_file.close()

    def update_logbook(self, gen, population):
        record = self.stats.compile(population) if self.stats is not None else {}
        self.logbook.record(gen=gen, **record)

    def dump_logbook_to_file(self):
        logbook_file = open(self.result_dir + "/logbook.pickle", 'wb')
        pickle.dump(self.logbook, logbook_file)
        logbook_file.close()