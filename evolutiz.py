import os
import pickle

import numpy
from deap import tools

import settings
from application.instrumentator import instrument_apk
from application.prepare_apk_parallel import prepare_apk
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from plot import two_d_line
from util import logger


class Evolutiz(object):

    def __init__(self):
        self.device_manager = RequiredFeature('device_manager').request()
        self.strategy = RequiredFeature('strategy').request()
        self.test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
        self.test_runner = RequiredFeature('test_runner').request()
        self.population_generator = RequiredFeature('population_generator').request()
        self.toolbox = RequiredFeature('toolbox').request()
        self.result_dir = RequiredFeature('result_dir').request()

        self.budget_manager = RequiredFeature('budget_manager')

        self.toolbox.register("get_apk_dir", lambda: self.app_path)
        self.toolbox.register("get_package_name", lambda: self.package_name)

        self.test_runner.register_crossover_operator(self.toolbox)
        self.test_runner.register_mutation_operator(self.toolbox)

        self.stats = tools.Statistics(lambda ind: ind.fitness.values)
        # axis = 0, the numpy.mean will return an array of results
        self.stats.register("avg", numpy.mean, axis=0)
        self.stats.register("std", numpy.std, axis=0)
        self.stats.register("min", numpy.min, axis=0)
        self.stats.register("max", numpy.max, axis=0)
        self.stats.register("pop_fitness", lambda x: x)

    def run(self):
        app_path = RequiredFeature('app_path').request()
        app_name = os.path.basename(app_path)

        # give test runner opportunity to install on devices
        self.test_runner.install_on_devices(self.device_manager)

        devices = self.device_manager.get_devices()

        instrument_apk(app_path, self.result_dir)
        package_name = prepare_apk(devices, app_path, self.result_dir)
        features.provide('package_name', package_name)

        self.device_manager.wait_for_battery_threshold()
        self.device_manager.log_devices_battery("init", self.result_dir)

        self.budget_manager.start_time_budget()

        for device in devices:
            # clear package data from previous runs
            adb.shell_command(device, "pm clear " + package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # hof = tools.HallOfFame(6)
        # pareto front can be large, there is a similarity option parameter
        hof = tools.ParetoFront()

        # setup toolbox specific stuff by strategy
        self.strategy.setup(self.toolbox, self.test_runner, stats=self.stats)

        # log the history
        history = tools.History()
        # Decorate the variation operators
        self.toolbox.decorate("mate", history.decorator)
        self.toolbox.decorate("mutate", history.decorator)

        # run the strategy
        population, logbook = self.strategy.run()

        logger.log_progress("\nEvolutiz finished for app: " + app_name + "\n")

        # write stats
        logbook_file = open(self.result_dir + "/intermediate/logbook.pickle", 'wb')
        pickle.dump(logbook, logbook_file)
        logbook_file.close()

        hof_file = open(self.result_dir + "/intermediate/hof.pickle", 'wb')
        pickle.dump(hof, hof_file)
        hof_file.close()

        history_file = open(self.result_dir + "/intermediate/history.pickle", 'wb')
        pickle.dump(history, history_file)
        history_file.close()

        # draw graph
        two_d_line.plot(logbook, 0, self.result_dir)
        two_d_line.plot(logbook, 1, self.result_dir)
        two_d_line.plot(logbook, 2, self.result_dir)
