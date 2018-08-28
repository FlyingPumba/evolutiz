# coding=utf-8
class OnePlusLambdaCommaLambda(object):
    """Implements the 1+(λ, λ) Evolutionary Algorithm as described in [CamposGFEA17]_.

    The 1 + (λ, λ) GA, starts by generating a random population of size 1. Then, mutation is used to create λ different
    mutated versions of the current individual. Mutation is applied with a high mutation probability, defined as
    m_p = k/n , where k is typically greater than one, which allows, on average, more than one gene to be mutated per
    chromosome. Then, uniform crossover is applied to the parent and best generated mutant to create λ offspring.
    While a high mutation probability is intended to support faster exploration of the search space, a uniform crossover
    between the best individual among the λ mutants and the parent was suggested to repair the defects caused by the
    aggressive mutation. Then all offspring are evaluated and the best one is selected. If the best offspring is better
    than the parent, the population of size one is replaced by the best offspring. 1 + (λ, λ) GA could be very expensive
    for large values of λ, as fitness has to be evaluated after mutation and after crossover.

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
