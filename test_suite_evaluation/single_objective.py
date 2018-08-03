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

    def evaluate(self, individual, device, gen="", pop=""):
        self.package_name = RequiredFeature('package_name').request()
        try:
            script_path, suite_lengths = self.dump_individual_to_files(individual, gen, pop)
            coverage, num_crashes, scripts_crash_status = self.coverage_fetcher.get_suite_coverage(script_path, device,
                                                                                                   gen, pop)
            # TODO: look into fusing coverage and number of crashes found into the fitness value
            individual.fitness.values = coverage

            self.hall_of_fame.update([individual])

            # TODO: log single-objective fitness result
            #logger.log_fitness_result(individual.fitness.values)

            return individual, pop, device, True

        except Exception as e:
            return None, pop, device, False
