import numpy
import pickle
from deap import tools

from application.apk_instrumentator import ApkInstrumentator
from application.prepare_apk_parallel import prepare_apk
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature


class Evolutiz(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.strategy = RequiredFeature('strategy').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.test_runner = RequiredFeature('test_runner').request()
        self.population_generator = RequiredFeature('population_generator').request()
        self.toolbox = RequiredFeature('toolbox').request()
        self.result_dir = RequiredFeature('result_dir').request()

        self.budget_manager = RequiredFeature('budget_manager').request()

        self.test_runner.register_crossover_operator(self.toolbox)
        self.test_runner.register_mutation_operator(self.toolbox)
        self.test_suite_evaluator.register_selection_operator(self.toolbox)

        self.apk_instrumentator = ApkInstrumentator()

        self.history = tools.History()
        self.toolbox.decorate("mate", self.history.decorator)
        self.toolbox.decorate("mutate", self.history.decorator)

        self.stats = tools.Statistics(lambda ind: ind.fitness.values)
        # axis = 0, the numpy.mean will return an array of results
        self.stats.register("avg", numpy.mean, axis=0)
        self.stats.register("std", numpy.std, axis=0)
        self.stats.register("min", numpy.min, axis=0)
        self.stats.register("max", numpy.max, axis=0)
        self.stats.register("pop_fitness", lambda x: x)

    def run(self):
        # give test runner opportunity to install on devices
        self.test_runner.install_on_devices(self.device_manager)

        devices = self.device_manager.get_devices()

        instrumented_app_path, package_name = self.apk_instrumentator.instrument()
        features.provide('package_name', package_name)
        features.provide('instrumented_app_path', instrumented_app_path)

        prepare_apk(devices, instrumented_app_path, package_name, self.result_dir)

        # setup toolbox specific stuff by strategy
        self.strategy.setup(stats=self.stats)

        # run the strategy
        population = self.strategy.run()

        self.write_summary_files()

    def write_summary_files(self):
        if RequiredFeature('write_hall_of_fame').request():
            self.test_suite_evaluator.dump_hall_of_fame_to_file()

        if RequiredFeature('write_history').request():
            history_file = open(self.result_dir + "/intermediate/history.pickle", 'wb')
            pickle.dump(self.history, history_file)
            history_file.close()

        if RequiredFeature('write_logbook').request():
            self.strategy.dump_logbook_to_file()
