import os
import pickle
import time
from datetime import datetime

import numpy
from deap import base
from deap.benchmarks import tools

import settings
from application.instrumentator import instrument_apk
from application.prepare_apk_parallel import prepare_apk
from coverage.emma_coverage import EmmaCoverage
from devices import adb
from init import initRepeatParallel, initRepeatParallelWithCoverage
from plot import two_d_line
from util import logger


class Evolutiz(object):

    def __init__(self, device_manager, strategy_class, test_suite_evaluator_class, test_runner, result_dir):
        self.device_manager = device_manager
        self.strategy = strategy_class()
        self.test_suite_evaluator_class = test_suite_evaluator_class
        self.test_runner = test_runner
        self.result_dir = result_dir

    def run(self, app_path):
        app_name = os.path.basename(app_path)

        # give test runner opportunity to install on devices
        self.test_runner.install_on_devices(self.device_manager)

        devices = self.device_manager.get_devices()

        instrument_apk(app_path, self.result_dir)
        self.package_name = prepare_apk(devices, app_path, self.result_dir)

        self.device_manager.wait_for_battery_threshold()
        self.device_manager.log_devices_battery("init", self.result_dir)

        # TODO: allow to use other coverage fetcher than EMMA, based on whether we are generating tests with source code or not
        self.self.test_suite_evaluator = self.test_suite_evaluator_class(self.test_runner, EmmaCoverage(),
                                                                         self.result_dir, self.app_path,
                                                                         self.package_name)

        # start time budget
        self.start_time = time.time()
        print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")

        for device in devices:
            # clear package data from previous runs
            adb.shell_command(device, "pm clear " + self.package_name, timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)

        # register common functions in toolbox
        toolbox = base.Toolbox()
        toolbox.register("individual", gen_individual, self.test_runner)
        toolbox.register("population", initRepeatParallel.initPop, self.device_manager, toolbox.individual)
        toolbox.register("individual_with_coverage", gen_individual_with_coverage, self.test_runner)
        toolbox.register("population_with_coverage", initRepeatParallelWithCoverage.initPop,
                         toolbox.individual_with_coverage)
        toolbox.register("time_budget_available",
                         lambda: time.time() - self.start_time < settings.SEARCH_BUDGET_IN_SECONDS)
        toolbox.register("get_apk_dir", lambda: app_path)
        toolbox.register("get_result_dir", lambda: self.result_dir)
        toolbox.register("get_package_name", lambda: self.package_name)
        toolbox.register("get_device_manager", lambda: self.device_manager)
        toolbox.register("log_devices_battery", self.device_manager.log_devices_battery)

        stats = tools.Statistics(lambda ind: ind.fitness.values)
        # axis = 0, the numpy.mean will return an array of results
        stats.register("avg", numpy.mean, axis=0)
        stats.register("std", numpy.std, axis=0)
        stats.register("min", numpy.min, axis=0)
        stats.register("max", numpy.max, axis=0)
        stats.register("pop_fitness", lambda x: x)

        # hof = tools.HallOfFame(6)
        # pareto front can be large, there is a similarity option parameter
        hof = tools.ParetoFront()

        # setup toolbox specific stuff by strategy
        self.strategy.setup(toolbox, self.test_runner, stats=stats)

        # log the history
        history = tools.History()
        # Decorate the variation operators
        toolbox.decorate("mate", history.decorator)
        toolbox.decorate("mutate", history.decorator)

        # run the strategy
        success = self.strategy.initPopulation()
        if not success:
            logger.log_progress("\nThere was an error initializing pupulation for app: " + app_name)
            return False

        population, logbook = self.strategy.evolve()

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
