# coding=utf-8

from algorithms.genetic_algorithm import GeneticAlgorithm


class Breeder(GeneticAlgorithm):
    """Implements the Breeder Genetic Algorithm as described in [CamposGAFEA18]_.

    This algorithm is a GA variant that does not aim to mimic Darwinian evolutionary, but instead tries to mimic
    breeding mechanism, as used for example in livestock. This is done by selecting a fixed percentage (e.g., 50%) of
    the best individuals of the total population as a gene pool, and then uniformly sampling from this pool for
    reproduction (using standard crossover and mutation) when generating a new population. In addition, the best _q_
    individuals (e.g., 1) survive in terms of elitism.

    .. [CamposGAFEA18] J. Campos, Y. Ge, N. Albunian, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Information and Software Technology journal, 2018, pp. 207–235.
    """

    def __init__(self):
        super(Breeder, self).__init__()
