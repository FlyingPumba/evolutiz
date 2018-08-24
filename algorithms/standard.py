# coding=utf-8
from algorithms.genetic_algorithm import GeneticAlgorithm
from devices.mapper_on_devices import MapperOnDevices
from util import logger


class Standard(GeneticAlgorithm):
    """Implements the Standard Evolutionary Algorithm as described in [CamposGFEA17]_.

    It starts by creating an initial random population of size p_n. Then, a pair of individuals is selected from the
    population using a strategy s_f, such as rank-based, elitism or tournament selection. Next, both selected
    individuals are recombined using crossover c_f (e.g., single point, multiple-point) with a probability of c_p to
    produce two new offspring o_1, o_2. Afterwards, mutation is applied on both offspring, independently changing the
    genes with a probability of m_p, which usually is equal to 1/n, where n is the number of genes in a chromosome.
    The two mutated offspring are then included in the next population. At the end of each iteration the fitness value
    of all individuals is computed.

    .. [CamposGFEA17] J. Campos, Y. Ge, G. Fraser, M. Eler, and A. Arcuri,
        “An Empirical Evaluation of Evolutionary Algorithms for Test Suite Generation”,
        in Search Based Software Engineering, 2017, pp. 33–48.
    """

    def __init__(self):
        super(Standard, self).__init__()

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
            success = self.parallel_evaluator.evaluate(invalid_ind)

            if not success:
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

        logger.log_progress("\nGenerating offspring of " + str(self.offspring_size) + " individuals in parallel")

        mapper = MapperOnDevices(self.generate_two_offspring,
                                 items_to_map=offspring_pairs_to_generate,
                                 idle_devices_only=True)

        mapper.run()

    def generate_two_offspring(self, device, pair_index):
        device.mark_work_start()

        p1, p2 = self.toolbox.select(self.population, 2)
        o1, o2 = self.toolbox.mate(p1, p2)
        o1 = self.toolbox.mutate(device, self.package_name, o1)
        o2 = self.toolbox.mutate(device, self.package_name, o2)

        del o1.fitness.values
        del o2.fitness.values

        self.offspring_generated.append(o1)
        self.offspring_generated.append(o2)

        device.mark_work_stop()

        return True
