from dependency_injection.required_feature import RequiredFeature
from evaluation.parallel_evaluator import ParallelEvaluator


class Strategy(object):

    def __init__(self) -> None:
        self.device_manager = RequiredFeature('device_manager').request()
        self.budget_manager = RequiredFeature('budget_manager').request()

        self.population_generator = RequiredFeature('population_generator').request()
        self.parallel_evaluator = ParallelEvaluator()

        self.toolbox = RequiredFeature('toolbox').request()
        self.result_dir = RequiredFeature('result_dir').request()
