# coding=utf-8
from algorithms.strategy import Strategy


class Mosa(Strategy):
    """Implements the MOSA Evolutionary Algorithm as described in [CamposGFEA17]_.

    Unlike the single-objective optimisation on the test suite level described above, the Many-Objective Sorting
    Algorithm (MOSA) regards each coverage goal as an independent optimisation objective. MOSA is a variant of NSGA-II,
    and uses a preference sorting criterion to reward the best tests for each non- covered target, regardless of their
    dominance relation with other tests in the population. MOSA also uses an archive to store the tests that cover new
    targets, which aiming to keep record on current best cases after each iteration.

    It starts with a random population of test cases. Then, and similar to typical EAs, the offspring are created by
    applying crossover and mutation. Selection is based on the combined set of parents and offspring. This set is sorted
    based on a non-dominance relation and preference criterion. MOSA selects non-dominated individuals based on the
    resulting rank, starting from the lowest rank (F_0), until the population size is reached. If fewer than p_s
    individuals are selected, the individuals of the current rank (F_r) are sorted by crowding distance, and the
    individuals with the largest distance are added. Finally, the archive that stores previously uncovered branches is
    updated in order to yield the final test suite.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
            “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
            in Search Based Software Engineering, 2017, pp. 33–48.
    """
