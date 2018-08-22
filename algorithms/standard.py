from algorithms.genetic_algorithm import GeneticAlgorithm
from devices.mapper_on_devices import MapperOnDevices
from util import logger


class Standard(GeneticAlgorithm):

    def __init__(self):
        super(Standard, self).__init__()

        self.mutation_add_probability = 1 / float(3)
        self.mutation_modify_probability = 1 / float(3)
        self.mutation_delete_probability = 1 / float(3)

        self.offspring_generated = []

    def evolve(self):
        for gen in range(1, self.max_generations + 1):

            if not self.budget_manager.time_budget_available():
                print "Time budget run out, exiting evolve"
                break

            logger.log_progress("\n---> Starting generation " + str(gen))

            self.generate_offspring_in_parallel()

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in self.offspring_generated if not ind.fitness.valid]
            individuals_evaluated = self.parallel_evaluator.evaluate(invalid_ind)

            if individuals_evaluated is None:
                print "Time budget run out during parallel evaluation, exiting evolve"
                break

            # Select the next generation population
            self.population = self.offspring_generated

            self.device_manager.log_devices_battery(gen, self.result_dir)
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen, self.population)

        return self.population

    def generate_offspring_in_parallel(self):
        self.offspring_generated = []
        offspring_pairs_to_generate = [i for i in range(0, self.offspring_size/2)]

        logger.log_progress("\nInit population of " + str(self.offspring_size) + " individuals in parallel")

        mapper = MapperOnDevices(self.generate_two_offspring,
                                 items_to_map=offspring_pairs_to_generate,
                                 idle_devices_only=True)

        mapper.run()

    def generate_two_offspring(self, device, ):
        device.mark_work_start()

        p1, p2 = self.toolbox.select(self.population, 2)
        o1, o2 = self.toolbox.mate(p1, p2)
        o1 = self.toolbox.mutate(device, self.package_name, o1)
        o2 = self.toolbox.mutate(device, self.package_name, o2)

        self.offspring_generated.append(o1)
        self.offspring_generated.append(o2)

        device.mark_work_stop()

        return o1, o2, device
