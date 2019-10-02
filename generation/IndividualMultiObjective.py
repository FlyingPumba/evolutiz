from generation.FitnessCovLenCrash import FitnessCovLenCrash
from generation.Individual import Individual


class IndividualMultiObjective(Individual):
    def __init__(self):
        super().__init__()

        self.fitness: FitnessCovLenCrash
