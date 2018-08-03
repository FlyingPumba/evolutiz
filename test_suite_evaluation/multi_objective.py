import sys

import numpy
from deap import creator, base, tools

from dependency_injection.required_feature import RequiredFeature
from test_suite_evaluation.test_suite_evaluator import TestSuiteEvaluator
from util import logger


class MultiObjectiveTestSuiteEvaluator(TestSuiteEvaluator):

    def __init__(self):
        super(MultiObjectiveTestSuiteEvaluator, self).__init__()

        # deap framework setup for multi objective
        creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
        creator.create("Individual", list, fitness=creator.FitnessCovLen)

        self.hall_of_fame = tools.ParetoFront()

    def register_selection_operator(self, toolbox):
        # self.toolbox.register("select", tools.selTournament, tournsize=5)
        toolbox.register("select", tools.selNSGA2)

    def evaluate(self, individual, device, gen="", pop=""):
        self.package_name = RequiredFeature('package_name').request()
        try:
            script_path, suite_lengths = self.dump_individual_to_files(individual, gen, pop)

            coverage, num_crashes, scripts_crash_status = self.coverage_fetcher.get_suite_coverage(script_path, device,
                                                                                                   gen, pop)
            # remove from suite lengths the scripts that did NOT cause a crash
            for script, had_crash in scripts_crash_status.iteritems():
                if not had_crash:
                    suite_lengths.pop(script, None)

            # 1st obj: coverage, 2nd: average seq length of the suite, 3nd: #crashes
            if suite_lengths:
                length = numpy.mean(suite_lengths.values())
            else:
                length = sys.maxint

            individual.fitness.values = (coverage, length, num_crashes)

            self.hall_of_fame.update([individual])
            logger.log_fitness_result(individual.fitness.values)

            return individual, pop, device, True

        except Exception as e:
            return None, pop, device, False
