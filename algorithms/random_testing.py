from algorithms.strategy import Strategy


class RandomTesting(Strategy):
    """Implements Random Testing strategy as described in [CamposGFEA17]_.

    Random Testing is a variant of Random Search in test generation which builds test suites incrementally. Test cases
    (rather than test suites) are sampled individually, and if a test improves coverage, it is retained in the test
    suite, otherwise it is discarded.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
            “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
            in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self):
        super(RandomTesting, self).__init__()

    def run(self):
        pass
