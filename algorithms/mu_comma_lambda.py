# coding=utf-8

from algorithms.mu_plus_lambda import MuPlusLambda
from dependency_injection.required_feature import RequiredFeature
from util import logger


class MuCommaLambda(MuPlusLambda):
    """Implements the (μ, λ) Evolutionary Algorithm as described in [CamposGAFEA18]_.

    This algorithms is a variant of the (μ + λ) EA, where the μ new individuals are only selected from the offspring,
    and the parents are discarded.

    .. [CamposGAFEA18] J. Campos, Y. Ge, N. Albunian, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Information and Software Technology journal, 2018, pp. 207–235.
    """

    def __init__(self):
        super(MuCommaLambda, self).__init__()

    def evolve(self):
        verbose_level = RequiredFeature('verbose_level').request()

        for gen in range(1, self.max_generations):

            if not self.budget_manager.time_budget_available():
                print("Time budget run out, exiting evolve")
                break

            logger.log_progress("\n---> Starting generation " + str(gen) + " at " + str(self.budget_manager.get_time_budget_used()))

            offspring = self.generate_offspring(self.population, gen)
            success = self.parallel_evaluator.evaluate(offspring)

            if not success:
                print("Time budget run out during parallel evaluation, exiting evolve")
                break

            self.population[:] = self.toolbox.selBest(offspring, self.population_size)

            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress("\nFinished generation " + str(gen) + " at " + str(self.budget_manager.get_time_budget_used()))

        return self.population
