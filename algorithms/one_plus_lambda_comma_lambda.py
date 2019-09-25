# coding=utf-8
import time
from typing import Any, List

from algorithms.genetic_algorithm import GeneticAlgorithm
from dependency_injection.required_feature import RequiredFeature
from util import logger


class OnePlusLambdaCommaLambda(GeneticAlgorithm):
    """Implements the 1 + (λ, λ) Evolutionary Algorithm as described in [CamposGFEA17]_.

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

    def __init__(self) -> None:
        super(OnePlusLambdaCommaLambda, self).__init__()

        self.history = RequiredFeature('history').request()
        self.population_size = 1
        self.parent = None

    def evolve(self) -> List[Any]:
        verbose_level: bool = RequiredFeature('verbose_level').request()
        self.parent = self.population[0]

        for gen in range(1, self.max_generations):

            if not self.budget_manager.is_budget_available():
                print("Budget ran out, exiting evolve")
                break

            logger.log_progress(f"\n---> Starting generation {str(gen)}"
                                f"at {str(self.budget_manager.get_time_budget_used())}")

            # Create and evaluate mutants
            mutants = self.generate_mutants(gen)
            success = self.parallel_evaluator.evaluate(mutants)
            if not success:
                print("Budget ran out during parallel evaluation, exiting evolve")
                break

            # select best mutant and generate offspring by applying crossover with parent
            best_mutant, = self.toolbox.selectBest(mutants, 1)
            offspring = self.generate_offspring(gen, best_mutant)
            success = self.parallel_evaluator.evaluate(offspring)
            if not success:
                print("Budget ran out during parallel evaluation, exiting evolve")
                break

            # select best offspring and set as new parent
            # the parent is included in the selection to avoid degrading the current solution
            offspring.append(self.parent)
            best_offspring, = self.toolbox.selectBest(offspring, 1)
            self.parent = best_offspring
            self.population = [self.parent]

            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

            if verbose_level > 0:
                logger.log_progress(f"\nFinished generation {str(gen)} "
                                    f"at {str(self.budget_manager.get_time_budget_used())}")

        return self.population

    def generate_mutants(self, gen) -> List[Any]:
        mutants = []
        for index_in_generation in range(self.offspring_size):
            ind = self.toolbox.clone(self.parent)
            self.toolbox.mutate(ind)

            del ind.fitness.values

            ind.index_in_generation = -1  # not an offspring of this generation, just a mutant
            ind.generation = gen
            ind.creation_finish_timestamp = time.time()
            ind.creation_elapsed_time = 0

            mutants.append(ind)

        return mutants

    def generate_offspring(self, gen, mutant) -> List[Any]:
        offspring = []
        for index_in_generation in range(0, self.offspring_size, 2):
            ind1, ind2 = map(self.toolbox.clone, [self.parent, mutant])
            ind1, ind2 = self.toolbox.mate(ind1, ind2)

            del ind1.fitness.values

            ind1.index_in_generation = index_in_generation
            ind1.generation = gen
            ind1.creation_finish_timestamp = time.time()
            ind1.creation_elapsed_time = 0

            offspring.append(ind1)

            del ind2.fitness.values

            ind2.index_in_generation = index_in_generation + 1
            ind2.generation = gen
            ind2.creation_finish_timestamp = time.time()
            ind2.creation_elapsed_time = 0

            offspring.append(ind2)

        return offspring

