# coding=utf-8

from algorithms.standard import Standard
from dependency_injection.required_feature import RequiredFeature
from util import logger


class SteadyState(Standard):
    """Implements the Steady State Evolutionary Algorithm as described in [CamposGFEA17]_.

    The Steady State version of the Standard GA uses the same replacement strategy as the Monotonic GA, but instead of
    creating a new population of offspring, the offspring replace the parents from the current population immediately
    after the mutation phase.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self):
        super(SteadyState, self).__init__()

    def evolve(self):
        verbose_level = RequiredFeature('verbose_level').request()

        for gen in range(1, self.max_generations):

            if not self.budget_manager.time_budget_available():
                print("Time budget run out, exiting evolve")
                break

            logger.log_progress("\n---> Starting generation " + str(gen) + " at " +
                                str(self.budget_manager.get_time_budget_used()))

            # select parents
            parents = self.toolbox.select(self.population, 2)

            # generate offspring
            offspring = self.crossover(parents, gen, 2)
            self.mutation(offspring)

            success = self.parallel_evaluator.evaluate(offspring)
            if not success:
                print("Time budget run out during parallel evaluation, exiting evolve")
                break

            best_ind, = self.toolbox.selBest(offspring + parents, 1)

            if best_ind in offspring:
                # only update population if the best individual is one of the offspring
                self.population = [ind for ind in self.population if ind not in parents] + offspring

            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress("\nFinished generation " + str(gen) + " at " +
                                    str(self.budget_manager.get_time_budget_used()))

        return self.population
