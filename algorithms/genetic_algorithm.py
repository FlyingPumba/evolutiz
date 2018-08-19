import settings
from algorithms.Strategy import Strategy
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

        self.population = None

        assert (self.crossover_probability + self.mutation_probability) <= 1.0, (
            "The sum of the crossover and mutation "
            "probabilities must be smaller or equal to 1.0.")

    def run(self):
        self.package_name = RequiredFeature('package_name').request()

        success = self.initPopulation()
        if not success:
            logger.log_progress("\nThere was an error initializing pupulation for app.")
            return False

        return self.evolve()

    def initPopulation(self):
        self.population = self.population_generator.generate(n=self.population_size)
        if len(self.population) < self.population_size:
            logger.log_progress("\nFailed to initialise population with proper size, exiting setup")
            return False

        # Evaluate the individuals with an invalid fitness
        invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
        individuals_evaluated = self.parallel_evaluator.evaluate(invalid_ind, 0)

        if individuals_evaluated is None:
            logger.log_progress("\nTime budget run out during parallel evaluation, exiting setup")
            return False

        self.population = individuals_evaluated[:]

        self.device_manager.log_devices_battery(0, self.result_dir)
        self.parallel_evaluator.test_suite_evaluator.update_logbook(0, self.population)

        return True

    def evolve(self):
        raise NotImplementedError


