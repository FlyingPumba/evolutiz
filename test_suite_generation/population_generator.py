from dependency_injection.di_assertions import HasMethods
from dependency_injection.required_feature import RequiredFeature
from devices.mapper_on_devices import MapperOnDevices
from util import logger


class PopulationGenerator(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.individual_generator = RequiredFeature('individual_generator', HasMethods('gen_individual')).request()

    def generate(self, n, gen=""):
        individuals_index_to_generate = [i for i in range(0, n)]

        logger.log_progress("\nInit population of " + str(n) + " individuals in parallel")

        mapper = MapperOnDevices(self.individual_generator.gen_individual,
                                 items_to_map=individuals_index_to_generate,
                                 extra_args=(gen,))

        individuals_generated = mapper.run()

        return individuals_generated
