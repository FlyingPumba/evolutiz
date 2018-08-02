import os
import sys

import numpy
from deap import creator, base

import settings
from dependency_injection.required_feature import RequiredFeature


class MultiObjectiveTestSuiteEvaluator(object):

    def __init__(self):
        self.test_runner = RequiredFeature('test_runner').request()
        self.coverage_fetcher = RequiredFeature('coverage_fetcher').request()
        self.result_dir = RequiredFeature('result_dir').request()

        # deap framework setup for multi objective
        creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
        creator.create("Individual", list, fitness=creator.FitnessCovLen)

    def evaluate(self, individual, device, gen="", pop=""):
        self.package_name = RequiredFeature('package_name').request()

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
                script.write(settings.MOTIFCORE_SCRIPT_HEADER)

                length = 0
                for line in seq:
                    script.write(line + "\n")
                    length += 1

            script = os.path.abspath(filename)
            suite_lengths[script] = length
            script_path.append(script)

        coverage, num_crashes, scripts_crash_status = self.coverage_fetcher.get_suite_coverage(script_path, device,
                                                                                               gen, pop)

        # remove from suite lengths the scripts that did NOT cause a crash
        for script, had_crash in scripts_crash_status.iteritems():
            if not had_crash:
                suite_lengths.pop(script, None)

        # 1st obj: coverage, 2nd: average seq length of the suite, 3nd: #crashes
        if suite_lengths:
            return pop, (coverage, numpy.mean(suite_lengths.values()), num_crashes), device
        else:
            return pop, (coverage, sys.maxint, num_crashes), device
