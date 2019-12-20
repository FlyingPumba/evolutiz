# coding=utf-8
from typing import Tuple

from generation.individual import Individual
from test_runner.evolutiz.evolutiz_connector import EvolutizConnector


class EvolutizCrossover(object):

    def __init__(self, evolutiz_connector: EvolutizConnector) -> None:
        self.evolutiz_connector = evolutiz_connector

    def crossover(self, ind1: Individual, ind2: Individual) -> Tuple[Individual, Individual]:

        return ind1, ind2
