import settings
from algorithms.strategy import Strategy
from dependency_injection.required_feature import RequiredFeature
from util import logger


class GeneticAlgorithm(Strategy):

    def __init__(self):
        super(GeneticAlgorithm, self).__init__()
        self.crossover_probability = settings.CXPB
        self.mutation_probability = settings.MUTPB
        self.max_generations = settings.GENERATION
        self.population_size = settings.POPULATION_SIZE
        self.offspring_size = settings.OFFSPRING_SIZE
        self.elitism_size = settings.ELITISM_SIZE

        self.population = None
        self.package_name = None

        assert (self.crossover_probability + self.mutation_probability) <= 1.0, (
            "The sum of the crossover and mutation "
            "probabilities must be smaller or equal to 1.0.")

    def run(self):
        self.package_name = RequiredFeature('package_name').request()

        success = self.initPopulation()
        if not success:
            logger.log_progress("\nThere was an error initializing population for app.")
            raise Exception("There was an error initializing population for app.")

        return self.evolve()

    def initPopulation(self):
        verbose_level = RequiredFeature('verbose_level').request()

        if verbose_level > 0:
            logger.log_progress(
                "\n---> Starting to generate initial population at " + str(self.budget_manager.get_time_budget_used()))

        self.population = self.population_generator.generate(self.population_size, gen=0)
        if len(self.population) < self.population_size:
            logger.log_progress("\nFailed to initialise population with proper size, exiting setup")
            return False

        if verbose_level > 0:
            logger.log_progress(
                "\n---> Starting to evaluate initial population at " + str(self.budget_manager.get_time_budget_used()))

        # Evaluate the individuals with an invalid fitness
        invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
        success = self.parallel_evaluator.evaluate(invalid_ind)

        if not success:
            logger.log_progress("\nTime budget run out during parallel evaluation, exiting setup")
            return False

        self.population = invalid_ind.copy()

        self.device_manager.log_devices_battery(0, self.result_dir)
        self.parallel_evaluator.test_suite_evaluator.update_logbook(0, self.population)

        history = RequiredFeature('history').request()
        history.update(self.population)

        if verbose_level > 0:
            logger.log_progress(
                "\n---> Finished creating initial population at " + str(self.budget_manager.get_time_budget_used()))

        return True

    def evolve(self):
        raise NotImplementedError


