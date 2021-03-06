import sys

from deap.base import Fitness

from test_runner.test_event import TestSuite


class Individual(TestSuite):

    def __init__(self) -> None:
        super().__init__()
        self.creation_finish_timestamp: float = 0
        self.creation_elapsed_time: float = 0
        self.evaluation_finish_timestamp: float = 0
        self.evaluation_elapsed_time: float = 0

        self.generation: int = 0
        self.index_in_generation: int = 0

        self.history_index: int = 0

        self.fitness: Fitness = None

        self.length: int = sys.maxsize
        self.crashes: int = 0

    @staticmethod
    def get_name() -> str:
        return Individual.__name__
