from deap.base import Fitness


class FitnessCov(Fitness):

    @staticmethod
    def get_name() -> str:
        return str(FitnessCov.__name__)
