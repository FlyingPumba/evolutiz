# coding=utf-8
from typing import List

from deap import creator

from algorithms.standard import Standard
from dependency_injection.required_feature import RequiredFeature
from generation.individual import Individual
from util import logger


class Monotonic(Standard):
    """Implements the Monotonic Evolutionary Algorithm as described in [CamposGFEA17]_.

    The Monotonic version of the Standard GA only includes either the best offspring or the best parent in the next
    population (whereas the Standard GA includes both offspring in the next population regardless of their fitness
    value).

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self) -> None:
        super(Monotonic, self).__init__()

    def evolve(self) -> List[Individual]:
        verbose_level: bool = RequiredFeature('verbose_level').request()

        for gen in range(1, self.max_generations):

            if not self.budget_manager.is_budget_available():
                print("Budget ran out, exiting evolve")
                break

            logger.log_progress(f"\n---> Starting generation {str(gen)}"
                                f" at {str(self.budget_manager.get_time_budget_used())}")

            # create new population, starting with elitism
            new_population: List[Individual] = self.toolbox.selectBest(self.population, self.elitism_size)
            while len(new_population) < self.population_size:
                # select parents
                parents: List[Individual] = self.toolbox.select(self.population, 2)

                # generate offspring
                needed_offspring = min(self.population_size - len(new_population), 2)
                offspring: List[Individual] = self.crossover(parents, gen, needed_offspring,
                                                      base_index_in_generation=len(new_population))
                self.mutation(offspring)

                success = self.parallel_evaluator.evaluate(offspring)
                if not success:
                    print("Budget ran out during parallel evaluation, exiting evolve")
                    return self.population

                # extend new population with offspring or parents, depending which ones have the best individual
                best_ind, = self.toolbox.selectBest(offspring + parents, 1)
                if best_ind in offspring:
                    new_population.extend(offspring)
                else:
                    new_population.extend(parents)

            self.population = new_population.copy()

            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress(f"\nFinished generation {str(gen)} "
                                    f"at {str(self.budget_manager.get_time_budget_used())}")

        return self.population
