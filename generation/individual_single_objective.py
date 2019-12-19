from generation.fitness_cov import FitnessCov
from generation.individual import Individual


class IndividualSingleObjective(Individual):
    def __init__(self) -> None:
        super().__init__()

        self.fitness: FitnessCov
