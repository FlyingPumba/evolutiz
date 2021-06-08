import sys
import time
from typing import List

import numpy
from deap import base, creator, tools
from deap.base import Toolbox
from deap.tools import HallOfFame

from dependency_injection.required_feature import RequiredFeature
from devices.device import Device
from evaluation.test_suite_evaluator import TestSuiteEvaluator
from generation.fitness_cov import FitnessCov
from generation.individual_single_objective import IndividualSingleObjective
from util import logger


class SingleObjectiveTestSuiteEvaluator(TestSuiteEvaluator):

    def __init__(self) -> None:
        super(SingleObjectiveTestSuiteEvaluator, self).__init__()

        # deap framework setup for single objective
        creator.create(FitnessCov.get_name(), base.Fitness, weights=(1.0,))
        creator.create(IndividualSingleObjective.get_name(), list, fitness=getattr(creator, FitnessCov.get_name()))

    def register_selection_operator(self, toolbox: Toolbox) -> None:
        toolbox.register("select", tools.selRoulette)

    def new_hall_of_fame(self) -> HallOfFame:
        return HallOfFame(maxsize=50)

    def set_empty_fitness(self, individual: IndividualSingleObjective) -> None:
        individual.fitness.values = (0, sys.maxsize, 0)

        individual.evaluation_finish_timestamp = time.time()
        individual.evaluation_elapsed_time = 0

        hall_of_fame = RequiredFeature('hall_of_fame').request()
        hall_of_fame.update([individual])

    def evaluate(self, device: Device, individual: IndividualSingleObjective) -> IndividualSingleObjective:
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

        individual.fitness.values = (coverage,)

        finish_time = time.time()
        individual.evaluation_finish_timestamp = finish_time
        individual.evaluation_elapsed_time = finish_time - start_time

        # Save crashes and length info outside fitness, to avoid messing up with the "wvalues" and "dominates" function
        # inside Deap's Fitness base class.
        if suite_lengths:
            individual.length = numpy.mean(list(suite_lengths.values()))
        else:
            individual.length = sys.maxsize

        individual.crashes = len(unique_crashes)

        hall_of_fame = RequiredFeature('hall_of_fame').request()
        hall_of_fame.update([individual])

        budget_manager = RequiredFeature('budget_manager').request()
        budget_manager.increase_evaluations_used()

        device.mark_work_stop()

        return individual

    def update_logbook(self, gen: int, population: List[IndividualSingleObjective]) -> None:
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
                'length': individual.length,
                'crashes': individual.crashes,
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

        # find out max crashes and min length by hand, since they are not included in the "max" and "min" elements of
        # the logbook. Since this is a single-objective algorithm, the max crashes and min length obtained are the one
        # of the individual that has the max coverage achieved.
        fitness_by_gen = self.logbook.select("fitness")

        max_coverage = 0
        min_length = sys.maxsize
        max_crashes = 0

        for gen, population in enumerate(fitness_by_gen):
            for fitness in population:

                at_least_as_good = fitness['coverage'] >= max_coverage
                partially_better = fitness['coverage'] > max_coverage

                if at_least_as_good and partially_better:
                    max_coverage = fitness['coverage']
                    min_length = fitness['length']
                    max_crashes = fitness['crashes']

        # CAUTION: these min and max are from different individuals
        logger.log_progress(f"\n- Best historic coverage: {str(max_coverage)}")

        logger.log_progress(f"\n- Best historic crashes: {str(max_crashes)}")
        if max_crashes > 0:
            logger.log_progress(f"\n- Best historic length: {str(min_length)}")
        else:
            logger.log_progress("\n- Best historic length: --")

    def dump_logbook_to_file(self) -> None:
        super(SingleObjectiveTestSuiteEvaluator, self).dump_logbook_to_file()
