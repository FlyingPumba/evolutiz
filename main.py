import argparse
import os
import subprocess as sub
import traceback

from deap.base import Toolbox

import settings
from algorithms.dyna_mosa import DynaMosa
from algorithms.monotonic import Monotonic
from algorithms.mosa import Mosa
from algorithms.mu_plus_lambda import MuPlusLambda
from algorithms.one_plus_lambda_comma_lambda import OnePlusLambdaCommaLambda
from algorithms.pure_random import Random
from algorithms.standard import Standard
from algorithms.steady_state import SteadyState
from coverage.emma_coverage import EmmaCoverage
from dependency_injection.feature_broker import features
from dependency_injection.required_feature import RequiredFeature
from devices import adb
from devices.device_manager import DeviceManager
from evolutiz import Evolutiz
from test_runner.evolutiz.evolutiz_test_runner import EvolutizTestRunner
from test_runner.motifcore.motifcore_test_runner import MotifcoreTestRunner
from test_suite_evaluation.multi_objective import MultiObjectiveTestSuiteEvaluator
from test_suite_evaluation.single_objective import SingleObjectiveTestSuiteEvaluator
from test_suite_generation.population_generator import PopulationGenerator
from test_suite_generation.population_with_coverage_generator import PopulationWithCoverageGenerator
from util import logger
from util.budget_manager import BudgetManager
from util.command_checker import is_command_available


def run_one_app(strategy_with_runner_name):
    device_manager = RequiredFeature('device_manager').request()
    app_path = RequiredFeature('app_path').request()

    folder_name = os.path.basename(app_path)
    try:
        for repetition in range(0, settings.REPETITIONS):

            # prepare result_dir folder
            result_dir = settings.WORKING_DIR + "results/" + \
                         strategy_with_runner_name + "/" + folder_name + "/" + str(repetition)

            os.system("rm -rf " + result_dir + "/*" + logger.redirect_string())

            result_code = os.system("mkdir -p " + result_dir)
            if result_code != 0: raise Exception("Unable to create result dir")
            os.system("mkdir -p " + result_dir + "/intermediate")
            os.system("mkdir -p " + result_dir + "/coverage")
            os.system("mkdir -p " + result_dir + "/crashes")

            features.provide('result_dir', result_dir)
            adb.adb_logs_dir = result_dir

            #  reboot all devices and restart adb server before starting a repetition
            adb.restart_server()
            if len(device_manager.get_devices(refresh=True)) < device_manager.get_total_number_of_devices_expected():
                device_manager.boot_emulators(wait_to_be_ready=True)
            else:
                device_manager.reboot_devices(wait_to_be_ready=True)

            logger.log_progress("\n-----> Starting repetition: " + str(repetition) + " for app: " + folder_name)

            for device in device_manager.get_devices():
                device.clean_sdcard()

            test_generator = Evolutiz()
            test_generator.run()

        return True
    except Exception as e:
        logger.log_progress("\nThere was an error running evolutiz on app: " + folder_name)
        traceback.print_exc()
        return False


def run(strategy_name, app_paths):
    for i in range(0, len(app_paths)):
        features.provide('app_path', app_paths[i])
        # TODO: the coverage_fetcher should depend on whether we are processing a closed source or open source app
        features.provide('coverage_fetcher', EmmaCoverage)
        success = run_one_app(strategy_name)
        if not success:
            break


def get_subject_paths(subjects_directory):
    p = sub.Popen("ls -d " + subjects_directory + "*/", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    app_paths = []
    for line in output.strip().split('\n'):
        if "hydrate" not in line:  # hydrate app doesn't compile yet, so don't bother
            app_paths.append(line.rstrip('/'))  # remove trailing forward slash
    return app_paths


def check_needed_commands_available():
    if not is_command_available(adb.adb_cmd_prefix):
        cause = "Command 'adb' was not found in the " + adb.adb_cmd_prefix + " route"
        logger.log_progress(cause)
        raise Exception(cause)

    if not is_command_available("ant"):
        cause = "Command 'ant' needed but not found."
        logger.log_progress(cause)
        raise Exception(cause)


if __name__ == "__main__":
    possible_strategies = {
        "standard": Standard,
        "monotonic": Monotonic,
        "steady": SteadyState,
        "muPlusLambda": MuPlusLambda,
        "onePlusLambdaCommaLambda": OnePlusLambdaCommaLambda,
        "mosa": Mosa,
        "dynaMosa": DynaMosa,
        "random": Random
    }

    possible_test_suite_evaluators = {
        "multi-objective": MultiObjectiveTestSuiteEvaluator,
        "single-objective": SingleObjectiveTestSuiteEvaluator
    }

    possible_population_generators = {
        "default": PopulationGenerator,
        "with-coverage": PopulationWithCoverageGenerator
    }

    possible_test_runners = {
        "motifcore": MotifcoreTestRunner(),
        "motifcore-nm": MotifcoreTestRunner(use_motifgene=True),
        "evolutiz": EvolutizTestRunner()
    }

    # parse args
    parser = argparse.ArgumentParser(description='Run Evolutiz with different strategies.')
    parser.add_argument('-d', '--subjects', dest='subjects_directory',
                        default=settings.SUBJECTS_PATH + 'are-we-there-yet/',
                        help='Directory where subjects are located')
    parser.add_argument('-s', '--strategy', dest='selected_strategy', default='muPlusLambda',
                        choices=possible_strategies.keys(), help='Strategy to be used')
    parser.add_argument('-e', '--evaluator', dest='selected_evaluator', default='multi-objective',
                        choices=possible_test_suite_evaluators.keys(), help='Test suite evaluator to be used')
    parser.add_argument('-p', '--population-generator', dest='selected_population_generator', default='default',
                        choices=possible_population_generators.keys(), help='Population generator to be used')
    parser.add_argument('-t', '--test-runner', dest='selected_test_runner', default='motifcore',
                        choices=possible_test_runners.keys(), help='Test runner to be used')
    # parser.add_argument('-nm', '--no-motifgene', dest='use_motifgene', action='store_false',
    #                     default=True, help='Disable motifgenes')

    args = parser.parse_args()
    app_paths = get_subject_paths(args.subjects_directory)[0:1]

    strategy_with_runner_name = args.selected_strategy + "-" + args.selected_test_runner

    features.provide('strategy', possible_strategies[args.selected_strategy])
    features.provide('test_suite_evaluator', possible_test_suite_evaluators[args.selected_evaluator])
    features.provide('test_runner', possible_test_runners[args.selected_test_runner])
    features.provide('population_generator', possible_population_generators[args.selected_population_generator])

    logger.prepare()

    check_needed_commands_available()

    # singletons
    features.provide('toolbox', Toolbox())
    features.provide('device_manager', DeviceManager())
    features.provide('budget_manager', BudgetManager())

    # run Evolutiz
    logger.clear_progress()
    logger.log_progress("Evolutiz (" +
                        args.selected_strategy + ", " +
                        args.selected_evaluator + ", " +
                        args.selected_test_runner + ")")

    run(strategy_with_runner_name, app_paths)

    # process results
    # results_per_app = process_results(app_paths)

    # logger.log_progress("\n" + str(results_per_app))
    # print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
