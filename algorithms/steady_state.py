# coding=utf-8
class SteadyState(object):
    """Implements the Steady State Evolutionary Algorithm as described in [CamposGFEA17]_.

    The Steady State version of the Standard GA uses the same replacement strategy as the Monotonic GA, but instead of
    creating a new population of offspring, the offspring replace the parents from the current population immediately
    after the mutation phase.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self):
        pass

    def setup(self, toolbox, test_runner, stats=None, verbose=False):
        pass

    def run(self):
        pass

    def initPopulation(self):
        pass

    def evolve(self):
        pass
