from generation.FitnessCov import FitnessCov
from generation.Individual import Individual


class IndividualSingleObjective(Individual):
    def __init__(self):
        super().__init__()

        self.fitness: FitnessCov
