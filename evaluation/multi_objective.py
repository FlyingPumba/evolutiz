import sys
import time
from typing import List

import numpy
from deap import creator, base, tools
from deap.base import Toolbox
from deap.tools.support import ParetoFront

from dependency_injection.required_feature import RequiredFeature
from devices.device import Device
from evaluation.test_suite_evaluator import TestSuiteEvaluator
from generation.fitness_cov_len_crash import FitnessCovLenCrash
from generation.individual import Individual
from generation.individual_multi_objective import IndividualMultiObjective
from util import logger


class MultiObjectiveTestSuiteEvaluator(TestSuiteEvaluator):

    def __init__(self) -> None:
        super(MultiObjectiveTestSuiteEvaluator, self).__init__()

        # deap framework setup for multi objective
        creator.create(FitnessCovLenCrash.get_name(), base.Fitness, weights=(10.0, -0.5, 1000.0))
        creator.create(IndividualMultiObjective.get_name(), list,
                       fitness=getattr(creator, FitnessCovLenCrash.get_name()))

    def register_selection_operator(self, toolbox: Toolbox) -> None:
        # self.toolbox.register("select", tools.selTournament, tournsize=5)
        toolbox.register("select", tools.selNSGA2)

    def new_hall_of_fame(self) -> ParetoFront:
        return tools.ParetoFront()

    def set_empty_fitness(self, individual: IndividualMultiObjective) -> None:
        individual.fitness.values = (0, sys.maxsize, 0)

        individual.evaluation_finish_timestamp = time.time()
        individual.evaluation_elapsed_time = 0

        hall_of_fame = RequiredFeature('hall_of_fame').request()
        hall_of_fame.update([individual])

    def evaluate(self, device: Device, individual: IndividualMultiObjective) -> IndividualMultiObjective:
        assert not individual.fitness.valid

        coverage_fetcher = RequiredFeature('coverage_fetcher').request()

        start_time = time.time()
        device.mark_work_start()
        script_path, suite_lengths = self.dump_individual_to_files(individual)

        coverage, unique_crashes, scripts_crash_status = coverage_fetcher.get_suite_coverage(
            script_path,
            device,
            individual.generation,
            individual.index_in_generation
        )

        # remove from suite lengths the scripts that did NOT cause a crash
        for script, had_crash in scripts_crash_status.items():
            if not had_crash:
                suite_lengths.pop(script, None)

        # 1st obj: coverage, 2nd: average seq length of the suite, 3rd: #crashes
        if suite_lengths:
            length = numpy.mean(list(suite_lengths.values()))
        else:
            length = sys.maxsize

        crashes = len(unique_crashes)

        individual.fitness.values = (coverage, length, crashes)

        finish_time = time.time()
        individual.evaluation_finish_timestamp = finish_time
        individual.evaluation_elapsed_time = finish_time - start_time

        individual.length = length
        individual.crashes = crashes

        hall_of_fame = RequiredFeature('hall_of_fame').request()
        hall_of_fame.update([individual])

        budget_manager = RequiredFeature('budget_manager').request()
        budget_manager.increase_evaluations_used()

        device.mark_work_stop()

        return individual

    def update_logbook(self, gen: int, population: List[IndividualMultiObjective]) -> None:
        self.result_dir = RequiredFeature('result_dir').request()
        self.logbook = RequiredFeature('logbook').request()

        record = {}
        fitness = []
        evaluation = []
        creation = []
        for individual in population:
            fitness.append({
                'evaluation': 'multi-objective',
                'generation': individual.generation,
                'index_in_generation': individual.index_in_generation,
                'coverage': individual.fitness.values[0],
                'length': individual.fitness.values[1],
                'crashes': individual.fitness.values[2],
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

    def show_best_historic_fitness(self) -> None:
        self.logbook = RequiredFeature('logbook').request()
        fitness_by_gen = self.logbook.select("fitness")

        # best independent (i.e., from different individuals) historic values for each objective
        max_coverage = 0
        min_length = sys.maxsize
        max_crashes = 0

        # the fitness of the best multi-objective individual
        best_individual_fitness = (max_coverage, min_length, max_crashes)

        for gen, population in enumerate(fitness_by_gen):
            for fitness in population:

                individual_coverage = fitness['coverage']
                individual_length = fitness['length']
                individual_crashes = fitness['crashes']

                # is this a better individual than the one found so far?
                at_least_as_good = individual_coverage >= max_coverage \
                                   and individual_length <= min_length \
                                   and individual_crashes >= max_crashes

                partially_better = individual_coverage > max_coverage \
                                   or individual_length < min_length \
                                   or individual_crashes > max_crashes

                if at_least_as_good and partially_better:
                    best_individual_fitness = (individual_coverage, individual_length, individual_crashes)

                if individual_coverage > max_coverage:
                    max_coverage = individual_coverage

                if individual_length < min_length:
                    min_length = individual_length

                if individual_crashes > max_crashes:
                    max_crashes = individual_crashes

        logger.log_progress(f"\n- Best multi-objective individual: {best_individual_fitness}")

        # CAUTION: the following min and max are from different individuals
        logger.log_progress(f"\n- Best historic coverage: {str(max_coverage)}")
        logger.log_progress(f"\n- Best historic crashes: {str(max_crashes)}")
        if max_crashes > 0:
            logger.log_progress(f"\n- Best historic length: {str(min_length)}")
        else:
            logger.log_progress("\n- Best historic length: --")

    def dump_logbook_to_file(self) -> None:
        super(MultiObjectiveTestSuiteEvaluator, self).dump_logbook_to_file()
