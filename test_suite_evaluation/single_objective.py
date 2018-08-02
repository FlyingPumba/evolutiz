import os

from deap import base, creator, tools

import settings
from dependency_injection.required_feature import RequiredFeature


class SingleObjectiveTestSuiteEvaluator(object):

    def __init__(self):
        self.test_runner = RequiredFeature('test_runner').request()
        self.coverage_fetcher = RequiredFeature('coverage_fetcher').request()
        self.result_dir = RequiredFeature('result_dir').request()

        # deap framework setup for single objective
        creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
        creator.create("Individual", list, fitness=creator.FitnessMin)

    def register_selection_operator(self, toolbox):
        # self.toolbox.register("select", tools.selTournament, tournsize=5)
        # TODO: check if this is the proper selection operator for single_objective context
        self.toolbox.register("select", tools.selNSGA2)

    def evaluate(self, individual, device, gen="", pop=""):
        self.package_name = RequiredFeature('package_name').request()

        script_path = []

        for index, seq in enumerate(individual):
            # generate script file list
            filename = self.result_dir + "/intermediate/script." + str(gen) + "." + str(pop) + "." + str(index)
            # check that directory exists before creating file
            dirname = os.path.dirname(filename)
            if not os.path.exists(dirname):
                os.makedirs(dirname)
            with open(filename, "w+") as script:
                script.write(settings.MOTIFCORE_SCRIPT_HEADER)

                for line in seq:
                    script.write(line + "\n")

            script_path.append(os.path.abspath(filename))

        coverage, num_crashes, scripts_crash_status = self.coverage_fetcher.get_suite_coverage(script_path, device,
                                                                                               gen, pop)

        # TODO: look into fusing coverage and number of crashes found into the fitness value
        fitness = coverage
        return pop, fitness, device
