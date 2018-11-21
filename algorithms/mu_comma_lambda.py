# coding=utf-8

from algorithms.genetic_algorithm import GeneticAlgorithm


class MuCommaLambda(GeneticAlgorithm):
    """Implements the (μ, λ) Evolutionary Algorithm as described in [CamposGAFEA18]_.

    This algorithms is a variant of the (μ + λ) EA, where the μ new individuals are only selected from the offspring,
    and the parents are discarded.

    .. [CamposGAFEA18] J. Campos, Y. Ge, N. Albunian, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Information and Software Technology journal, 2018, pp. 207–235.
    """

    def __init__(self):
        super(MuCommaLambda, self).__init__()
