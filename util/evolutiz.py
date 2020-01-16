import pickle
import traceback

from coverage.apk_preparer import ApkPreparer
from concurrency.mapper_on_devices import MapperOnDevices
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices.device_setup import DeviceSetupThread
from util import logger


class Evolutiz(object):

    def __init__(self) -> None:
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

        self.apk_preparer = ApkPreparer()
        features.provide('apk_preparer', self.apk_preparer)

        self.history = RequiredFeature('history').request()
        self.toolbox.decorate("mate", self.history.decorator)
        self.toolbox.decorate("mutate", self.history.decorator)

    def run(self) -> None:
        self.apk_preparer.prepare()

        emulators_number = RequiredFeature('emulators_number').request()
        if (emulators_number < 1):
            return

        logger.log_progress("\nSetting up devices.")

        minimum_api = RequiredFeature('minimum_api').request()
        mapper = MapperOnDevices(DeviceSetupThread.setup, minimum_api=minimum_api)

        try:
            mapper.run()
        except Exception as e:
            logger.log_progress(f"An error happened setting up devices: {str(traceback.format_exc())}")
            return

        # run the strategy
        population = self.strategy.run()

        self.write_summary_files()

    def write_summary_files(self) -> None:
        if RequiredFeature('write_hall_of_fame').request():
            self.test_suite_evaluator.dump_hall_of_fame_to_file()

        if RequiredFeature('write_history').request():
            history_file = open(f"{self.result_dir}/history.pickle", 'wb')
            pickle.dump(self.history, history_file)
            history_file.close()

        if RequiredFeature('write_logbook').request():
            self.test_suite_evaluator.dump_logbook_to_file()
