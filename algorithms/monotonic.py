# coding=utf-8

from algorithms.standard import Standard
from concurrency.mapper_on_devices import MapperOnDevices
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

    def __init__(self):
        super(Monotonic, self).__init__()

        self.new_population = []

    def generate_offspring_in_parallel(self):
        self.new_population = []
        offspring_pairs_to_generate = [i for i in range(0, self.offspring_size)]

        logger.log_progress("\nGenerating offspring of " + str(self.offspring_size) + " individuals in parallel")

        mapper = MapperOnDevices(self.generate_two_offspring,
                                 items_to_map=offspring_pairs_to_generate,
                                 idle_devices_only=True)

        try:
            mapper.run()
            return True
        except TimeoutError:
            return False

    def generate_two_offspring(self, device, pair_index):
        device.mark_work_start()

        p1, p2 = self.toolbox.select(self.population, 2)
        o1, o2 = self.toolbox.mate(p1, p2)
        o1, = self.toolbox.mutate(device, self.package_name, o1)
        o2, = self.toolbox.mutate(device, self.package_name, o2)

        del o1.fitness.values
        del o2.fitness.values

        self.parallel_evaluator.test_suite_evaluator.evaluate(device, o1)
        self.parallel_evaluator.test_suite_evaluator.evaluate(device, o1)

        # TODO: the following comparisons of fitness will only work with single-objective fitness functions.
        best_parent = None
        if p1.fitness.values > p2.fitness.values:
            best_parent = p1
        else:
            best_parent = p2

        best_offspring = None
        if o1.fitness.values > o2.fitness.values:
            best_offspring = o1
        else:
            best_offspring = o2

        if best_parent.fitness.values > best_offspring.fitness.values:
            self.new_population.append(best_parent)
        else:
            self.new_population.append(best_offspring)

        device.mark_work_stop()

        return True
