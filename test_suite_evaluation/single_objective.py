import os

import settings


class SingleObjectiveTestSuiteEvaluator(object):

    def __init__(self, test_runner, coverage_fetcher, result_dir, apk_dir, package_name):
        self.test_runner = test_runner
        self.coverage_fetcher = coverage_fetcher
        self.result_dir = result_dir
        self.apk_dir = apk_dir
        self.package_name = package_name

    def evaluate(self, individual, device, gen="", pop=""):
        script_path = []

        for index, seq in enumerate(individual):
            # generate script file list
            filename = self.result_dir + "/intermediate/motifcore.evo.script." + str(gen) + "." + str(pop) + "." + str(index)
            # check that directory exists before creating file
            dirname = os.path.dirname(filename)
            if not os.path.exists(dirname):
                os.makedirs(dirname)
            with open(filename, "w+") as script:
                script.write(settings.MOTIFCORE_SCRIPT_HEADER)

                for line in seq:
                    script.write(line + "\n")

            script_path.append(os.path.abspath(filename))

        coverage, num_crashes, scripts_crash_status = self.coverage_fetcher.get_suite_coverage(
            self.test_runner, script_path, device, self.result_dir, self.apk_dir, self.package_name, gen, pop)

        # TODO: look into fusing coverage and number of crashes found into the fitness value
        fitness = coverage
        return pop, fitness, device
