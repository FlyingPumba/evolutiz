# coding=utf-8
import random
from typing import Tuple

from deap import creator

import settings
from generation.individual import Individual
from test_runner.evolutiz.evolutiz_connector import EvolutizConnector


class EvolutizCrossover(object):

    def __init__(self, evolutiz_connector: EvolutizConnector) -> None:
        self.evolutiz_connector = evolutiz_connector

    def crossover(self, ind1: Individual, ind2: Individual) -> Tuple[Individual, Individual]:
        test_cases_pool = ind1[:] + ind2[:]
        random.shuffle(test_cases_pool)

        new_ind1: Individual = getattr(creator, Individual.get_name())(test_cases_pool[:settings.SUITE_SIZE])
        new_ind2: Individual = getattr(creator, Individual.get_name())(test_cases_pool[settings.SUITE_SIZE:])

        return new_ind1, new_ind2
