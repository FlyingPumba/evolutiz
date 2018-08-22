import pickle
import sys

import numpy
from deap import creator, base, tools

from dependency_injection.required_feature import RequiredFeature
from plot import two_d_line
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

    def evaluate(self, device, individual):
        assert not individual.fitness

        self.package_name = RequiredFeature('package_name').request()

        device.mark_work_start()
        script_path, suite_lengths = self.dump_individual_to_files(individual)

        coverage, num_crashes, scripts_crash_status = self.coverage_fetcher.get_suite_coverage(script_path, device,
                                                                                               individual.generation,
                                                                                               individual.index_in_generation)
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

        device.mark_work_stop()

        return individual

    def update_logbook(self, gen, population):
        super(MultiObjectiveTestSuiteEvaluator, self).update_logbook(gen, population)
        self.show_best_historic_fitness()

    def show_best_historic_fitness(self):
        min_fitness_values_per_generation = numpy.array(self.logbook.select("min"))
        max_fitness_values_per_generation = numpy.array(self.logbook.select("max"))

        max_fitness_values_all_generations = max_fitness_values_per_generation.max(axis=0)
        min_fitness_values_all_generations = min_fitness_values_per_generation.min(axis=0)

        max_coverage = max_fitness_values_all_generations[0]
        min_length = min_fitness_values_all_generations[1]
        max_crashes = max_fitness_values_all_generations[2]

        # CAUTION: these min and max are from different individuals
        logger.log_progress("\n- Best historic coverage: " + str(max_coverage))
        logger.log_progress("\n- Best historic crashes: " + str(max_crashes))
        if max_crashes > 0:
            logger.log_progress("\n- Best historic length: " + str(min_length))

    def dump_logbook_to_file(self):
        super(MultiObjectiveTestSuiteEvaluator, self).dump_logbook_to_file()

        # draw graph
        two_d_line.plot(self.logbook, 0, self.result_dir)
        two_d_line.plot(self.logbook, 1, self.result_dir)
        two_d_line.plot(self.logbook, 2, self.result_dir)
