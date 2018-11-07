import time

from deap import base, creator, tools

from dependency_injection.required_feature import RequiredFeature
from test_suite_evaluation.test_suite_evaluator import TestSuiteEvaluator


class SingleObjectiveTestSuiteEvaluator(TestSuiteEvaluator):

    def __init__(self):
        super(SingleObjectiveTestSuiteEvaluator, self).__init__()

        # deap framework setup for single objective
        creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
        creator.create("Individual", list, fitness=creator.FitnessMin)

        self.hall_of_fame = tools.HallOfFame(maxsize=10)

    def register_selection_operator(self, toolbox):
        # self.toolbox.register("select", tools.selTournament, tournsize=5)
        # TODO: check if this is the proper selection operator for single_objective context
        toolbox.register("select", tools.selNSGA2)

    def set_empty_fitness(self, individual):
        individual.fitness.values = 0

        individual.evaluation_finish_timestamp = time.time()
        individual.evaluation_elapsed_time = 0

        self.hall_of_fame.update([individual])

    def evaluate(self, device, individual):
        assert not individual.fitness.valid

        coverage_fetcher = RequiredFeature('coverage_fetcher').request()
        self.package_name = RequiredFeature('package_name').request()

        start_time = time.time()
        device.mark_work_start()
        script_path, suite_lengths = self.dump_individual_to_files(individual)
        coverage, unique_crashes, scripts_crash_status = coverage_fetcher.get_suite_coverage(script_path, device,
                                                                                               individual.generation,
                                                                                               individual.index_in_generation)
        # TODO: look into fusing coverage and number of crashes found into the fitness value
        individual.fitness.values = coverage

        finish_time = time.time()
        individual.evaluation_finish_timestamp = finish_time
        individual.evaluation_elapsed_time = finish_time - start_time

        self.hall_of_fame.update([individual])

        device.mark_work_stop()

        return individual
