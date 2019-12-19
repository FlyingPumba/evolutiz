from deap.base import Fitness


class Individual(list):

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

    @staticmethod
    def get_name() -> str:
        return Individual.__name__
