# coding=utf-8

from algorithms.genetic_algorithm import GeneticAlgorithm


class Cellular(GeneticAlgorithm):
    """Implements the Cellular Genetic Algorithm as described in [CamposGAFEA18]_.

    This algorithm differs from the Standard GA by considering a structured population which influences selection. For
    example, individuals can be set in a toroidal d-dimensional grid where each individual takes a place per a grid
    (i.e., cell) and belongs to an overlapped neighbourhood. The grid of individuals can have different number of
    dimensions; common values are one-dimensional (i.e., ring) or two-dimensional grids.

    Each individual is only allowed to interact with its neighbours and therefore the search operators are only applied
    on the individuals of one neighbourhood. First, two parents p_1, p_2 are selected among the neighbours of one
    individual _p_ according to a selection criterion. Then, crossover is performed to create two nwe individuals o_1,
    o_2, which are then evaluated. The best individual (_o_) among the two new generated individuals is mutated and
    evaluated. Finally, if fitness value of _p_ is better than the fitness value of _o_, the former is included in the
    next population, otherwise the later is included in the next population.

    Due to the neighbourhood overlapping, the Cellular GA motivates slow diffusion of solutions through the population
    and thus the exploration of the search space and the exploitation inside each neighbourhood are promoted during the
    search.

    .. [CamposGAFEA18] J. Campos, Y. Ge, N. Albunian, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Information and Software Technology journal, 2018, pp. 207–235.
    """

    def __init__(self):
        super(Cellular, self).__init__()
