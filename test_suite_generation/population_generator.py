import time

from concurrency.mapper_on_devices import MapperOnDevices
from dependency_injection.di_assertions import HasMethods
from dependency_injection.required_feature import RequiredFeature
from util import logger
from util.integer import Integer


class PopulationGenerator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.individual_generator = RequiredFeature('individual_generator', HasMethods('gen_individual')).request()

    def generate(self, n, gen=0):
        budget_manager = RequiredFeature('budget_manager').request()
        individuals_index_to_generate = [Integer(i) for i in range(0, n)]

        logger.log_progress("\nInit population of " + str(n) + " individuals in parallel")

        mapper = MapperOnDevices(self.individual_generator.gen_individual,
                                 items_to_map=individuals_index_to_generate,
                                 fail_times_limit=3,
                                 extra_args=(gen,))

        try:
            start_time = time.time()

            individuals_generated = mapper.run()

            finish_time = time.time()
            elapsed_time = finish_time - start_time
            budget_manager.increase_time_budget(elapsed_time)

            return individuals_generated
        except TimeoutError:
            return None

