from algorithms.strategy import Strategy
from dependency_injection.required_feature import RequiredFeature
from util import logger


class RandomSearch(Strategy):
    """Implements Random Search strategy as described in [CamposGFEA17]_.

    Random Search is a baseline search strategy which does not use crossover, mutation, or selection, but a simple
    replacement strategy. Random search consists of repeatedly sampling candidates from the search space; the previous
    candidate is replaced if the fitness of the new sampled individual is better.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
            “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
            in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self):
        super(RandomSearch, self).__init__()

        # use expected number of devices as population size to leverage parallelism when using more than one emulator.
        self.device_manager = RequiredFeature('device_manager').request()
        self.population_size = self.device_manager.get_total_number_of_devices_expected()

        self.population = []

    def run(self):
        gen = 0
        while self.budget_manager.time_budget_available():
            new_population = self.population_generator.generate(self.population_size, gen=gen)
            if new_population is None:
                # Timeout occurred
                break

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in new_population if not ind.fitness.valid]
            success = self.parallel_evaluator.evaluate(invalid_ind)
            if not success:
                # Timeout occurred
                break

            # save stats
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, new_population)
            history = RequiredFeature('history').request()
            history.update(self.population)

            # select best individuals between current population and new one
            self.population[:] = self.toolbox.select(self.population + new_population, self.population_size)
            gen += 1

        return self.population
