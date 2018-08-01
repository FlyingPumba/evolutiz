import os
import sys

import numpy

import settings


class MultiObjectiveTestSuiteEvaluator(object):

    def __init__(self, test_runner, coverage_fetcher, result_dir, apk_dir, package_name):
        self.test_runner = test_runner
        self.coverage_fetcher = coverage_fetcher
        self.result_dir = result_dir
        self.apk_dir = apk_dir
        self.package_name = package_name

    def evaluate(self, individual, device, gen="", pop=""):
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

        coverage, num_crashes, scripts_crash_status = self.coverage_fetcher.get_suite_coverage(
            self.test_runner, script_path, device, self.result_dir, self.apk_dir, self.package_name, gen, pop)

        # remove from suite lengths the scripts that did NOT cause a crash
        for script, had_crash in scripts_crash_status.iteritems():
            if not had_crash:
                suite_lengths.pop(script, None)

        # 1st obj: coverage, 2nd: average seq length of the suite, 3nd: #crashes
        if suite_lengths:
            return pop, (coverage, numpy.mean(suite_lengths.values()), num_crashes), device
        else:
            return pop, (coverage, sys.maxint, num_crashes), device
