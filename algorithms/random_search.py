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

        # use expected number of devices as sampling size to leverage parallelism when using more than one emulator.
        self.device_manager = RequiredFeature('device_manager').request()
        self.sampling_size = self.device_manager.get_total_number_of_devices_expected()

        self.population_size = 1
        self.population = []

    def run(self):
        gen = 0
        while self.budget_manager.is_budget_available():
            logger.log_progress(
                "\n---> Starting generation " + str(gen) + " at " + str(self.budget_manager.get_time_budget_used()))

            new_individuals = self.population_generator.generate(self.sampling_size, gen=gen)
            if new_individuals is None:
                # Timeout occurred
                break

            success = self.parallel_evaluator.evaluate(new_individuals)
            if not success:
                # Timeout occurred
                break

            # save stats
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, new_individuals)
            history = RequiredFeature('history').request()
            history.update(self.population)

            # select best individuals between current population and new one
            self.population[:] = self.toolbox.selBest(self.population + new_individuals, self.population_size)
            gen += 1

        return self.population
