from deap.base import Fitness


class FitnessCovLenCrash(Fitness):

    @staticmethod
    def get_name() -> str:
        return str(FitnessCovLenCrash.__name__)
