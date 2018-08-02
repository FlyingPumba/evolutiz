import os
import pickle
import time
from datetime import datetime

import numpy
from deap import base, tools

import settings
from application.instrumentator import instrument_apk
from application.prepare_apk_parallel import prepare_apk
from coverage.emma_coverage import EmmaCoverage
from devices import adb
from test_suite_generation import init_population, init_population_with_coverage
from plot import two_d_line
from util import logger


class Evolutiz(object):

    def __init__(self, device_manager, strategy_class, test_suite_evaluator_class, test_runner, result_dir):
        self.device_manager = device_manager
        self.strategy = strategy_class()
        self.test_suite_evaluator_class = test_suite_evaluator_class
        self.test_runner = test_runner
        self.result_dir = result_dir

        # register common functions in toolbox
        self.toolbox = base.Toolbox()
        self.toolbox.register("individual", gen_individual, self.test_runner)
        self.toolbox.register("population", init_population.initPop, self.device_manager, self.toolbox.individual)
        self.toolbox.register("individual_with_coverage", gen_individual_with_coverage, self.test_runner)
        self.toolbox.register("population_with_coverage", init_population_with_coverage.initPop,
                              self.toolbox.individual_with_coverage)

        self.toolbox.register("get_device_manager", lambda: self.device_manager)
        self.toolbox.register("log_devices_battery", self.device_manager.log_devices_battery)

        self.toolbox.register("time_budget_available",
                              lambda: time.time() - self.start_time < settings.SEARCH_BUDGET_IN_SECONDS)
        self.toolbox.register("get_apk_dir", lambda: self.app_path)
        self.toolbox.register("get_result_dir", lambda: self.result_dir)
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

    def run(self, app_path):
        self.app_path = app_path
        app_name = os.path.basename(app_path)

        # give test runner opportunity to install on devices
        self.test_runner.install_on_devices(self.device_manager)

        devices = self.device_manager.get_devices()

        instrument_apk(app_path, self.result_dir)
        self.package_name = prepare_apk(devices, app_path, self.result_dir)

        self.device_manager.wait_for_battery_threshold()
        self.device_manager.log_devices_battery("init", self.result_dir)

        # TODO: allow to use other coverage fetcher than EMMA, based on whether we are generating tests with source code or not
        coverage_fetcher = EmmaCoverage(self.test_runner, self.result_dir, self.apk_dir, self.package_name)
        self.test_suite_evaluator = self.test_suite_evaluator_class(self.test_runner, coverage_fetcher,
                                                                         self.result_dir, self.app_path,
                                                                         self.package_name)

        # start time budget
        self.start_time = time.time()
        print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")



        for device in devices:
            # clear package data from previous runs
            adb.shell_command(device, "pm clear " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)


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
