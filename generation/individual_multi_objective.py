from generation.fitness_cov_len_crash import FitnessCovLenCrash
from generation.individual import Individual


class IndividualMultiObjective(Individual):
    def __init__(self):
        super().__init__()

        self.fitness: FitnessCovLenCrash
