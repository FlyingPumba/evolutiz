import time

import numpy
from deap import base, creator, tools

from dependency_injection.required_feature import RequiredFeature
from evaluation.test_suite_evaluator import TestSuiteEvaluator
from generation.FitnessCov import FitnessCov
from generation.IndividualSingleObjective import IndividualSingleObjective
from util import logger


class SingleObjectiveTestSuiteEvaluator(TestSuiteEvaluator):

    def __init__(self):
        super(SingleObjectiveTestSuiteEvaluator, self).__init__()

        # deap framework setup for single objective
        creator.create(FitnessCov.get_name(), base.Fitness, weights=(1.0,))
        creator.create(IndividualSingleObjective.get_name(), list, fitness=getattr(creator, FitnessCov.get_name()))

    def register_selection_operator(self, toolbox):
        toolbox.register("select", tools.selRoulette)

    def new_hall_of_fame(self):
        return tools.HallOfFame(maxsize=50)

    def set_empty_fitness(self, individual):
        individual.fitness.values = (0, )

        individual.evaluation_finish_timestamp = time.time()
        individual.evaluation_elapsed_time = 0

        hall_of_fame = RequiredFeature('hall_of_fame').request()
        hall_of_fame.update([individual])

    def evaluate(self, device, individual):
        assert not individual.fitness.valid

        coverage_fetcher = RequiredFeature('coverage_fetcher').request()
        self.package_name = RequiredFeature('package_name').request()

        start_time = time.time()
        device.mark_work_start()
        script_path, suite_lengths = self.dump_individual_to_files(individual)
        coverage, unique_crashes, scripts_crash_status = coverage_fetcher.get_suite_coverage(
            script_path,
            device,
            individual.generation,
            individual.index_in_generation
        )

        # TODO: look into fusing coverage and number of crashes found into the fitness value
        individual.fitness.values = (coverage, )

        finish_time = time.time()
        individual.evaluation_finish_timestamp = finish_time
        individual.evaluation_elapsed_time = finish_time - start_time

        hall_of_fame = RequiredFeature('hall_of_fame').request()
        hall_of_fame.update([individual])

        budget_manager = RequiredFeature('budget_manager').request()
        budget_manager.increase_evaluations_used()

        device.mark_work_stop()

        return individual

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
                'generation': individual.generation,
                'index_in_generation': individual.index_in_generation,
                'coverage': individual.fitness.values[0],
            })
            evaluation.append({
                'generation': individual.generation,
                'index_in_generation': individual.index_in_generation,
                'evaluation_finish_timestamp': individual.evaluation_finish_timestamp,
                'evaluation_elapsed_time': individual.evaluation_elapsed_time,
            })
            creation.append({
                'generation': individual.generation,
                'index_in_generation': individual.index_in_generation,
                'creation_finish_timestamp': individual.creation_finish_timestamp,
                'creation_elapsed_time': individual.creation_elapsed_time,
            })

        record['fitness'] = numpy.array(fitness)
        record['evaluation'] = numpy.array(evaluation)
        record['creation'] = numpy.array(creation)
        self.logbook.record(gen=gen, **record)

        self.show_best_historic_fitness()

    def show_best_historic_fitness(self):
        self.logbook = RequiredFeature('logbook').request()
        max_fitness_values_per_generation = numpy.array(self.logbook.select("max"))

        max_fitness_values_all_generations = max_fitness_values_per_generation.max(axis=0)

        max_coverage = max_fitness_values_all_generations[0]

        # CAUTION: these min and max are from different individuals
        logger.log_progress(f"\n- Best historic coverage: {str(max_coverage)}")

    def dump_logbook_to_file(self):
        super(SingleObjectiveTestSuiteEvaluator, self).dump_logbook_to_file()
