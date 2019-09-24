# coding=utf-8
from algorithms.strategy import Strategy


class DynaMosa(Strategy):
    """Implements the DynaMOSA Evolutionary Algorithm as described in [CamposGFEA17]_.

    In order to cope with the large numbers of goals resulting from the combination of multiple coverage criteria in the
    MOSA EA, the DynaMOSA extension dynamically selects targets based on the dependencies between the uncovered targets
    and the newly covered targets.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
            “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
            in Search Based Software Engineering, 2017, pp. 33–48.
    """
