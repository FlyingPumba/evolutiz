import ConfigParser
import argparse
import os
import random
import re
import subprocess as sub
import traceback

import numpy
from deap import tools
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
from test_suite_generation.individual_with_coverage_generator import IndividualWithCoverageGenerator
from test_suite_generation.individual_without_coverage_generator import IndividualWithoutCoverageGenerator
from test_suite_generation.population_generator import PopulationGenerator
from util import logger
from util.budget_manager import BudgetManager
from util.command_checker import is_command_available


def run_one_app(strategy_with_runner_name):
    app_path = RequiredFeature('app_path').request()
    repetitions = RequiredFeature('repetitions').request()
    budget_manager = RequiredFeature('budget_manager').request()

    app_name = os.path.basename(app_path)

    try:
        for repetition in range(0, repetitions):
            result_dir = prepare_result_dir(app_name, repetition, strategy_with_runner_name)

            prepare_devices(result_dir)

            logger.log_progress("\n-----> Starting repetition: " + str(repetition) + " for app: " + app_name)

            test_generator = Evolutiz()

            budget_manager.start_time_budget()
            test_generator.run()

            logger.log_progress("\nEvolutiz finished for app: " + app_name)
            logger.log_progress("\nTime budget used: %.2f seconds\n" % (budget_manager.get_time_budget_used()))

        return True
    except Exception as e:
        logger.log_progress("\nThere was an error running evolutiz on app: " + app_name)
        traceback.print_exc()
        return False


def prepare_devices(result_dir):
    """Reboot all devices and restart adb server before starting a repetition."""
    device_manager = RequiredFeature('device_manager').request()
    adb.restart_server()

    if len(device_manager.get_devices(refresh=True)) < device_manager.get_total_number_of_devices_expected():
        device_manager.boot_emulators(wait_to_be_ready=True)
    else:
        device_manager.reboot_devices(wait_to_be_ready=True)

    for device in device_manager.get_devices():
        device.clean_sdcard()

    device_manager.wait_for_battery_threshold()
    device_manager.log_devices_battery("init", result_dir)


def prepare_result_dir(app_name, repetition, strategy_with_runner_name):
    result_dir = settings.WORKING_DIR + "results/" + \
                 strategy_with_runner_name + "/" + app_name + "/" + str(repetition)

    os.system("rm -rf " + result_dir + "/*" + logger.redirect_string())
    result_code = os.system("mkdir -p " + result_dir)
    if result_code != 0: raise Exception("Unable to create result dir")

    os.system("mkdir -p " + result_dir + "/intermediate")
    os.system("mkdir -p " + result_dir + "/coverage")
    os.system("mkdir -p " + result_dir + "/crashes")

    features.provide('result_dir', result_dir)
    adb.adb_logs_dir = result_dir

    return result_dir


def run(strategy_name, app_paths):
    for i in range(0, len(app_paths)):
        features.provide('app_path', app_paths[i])

        stats = RequiredFeature('stats').request()
        logbook = tools.Logbook()
        logbook.header = ['gen'] + stats.fields
        features.provide('logbook', logbook)

        history = tools.History()
        features.provide('history', history)

        # TODO: the coverage_fetcher should depend on whether we are processing a closed source or open source app
        features.provide('coverage_fetcher', EmmaCoverage)

        success = run_one_app(strategy_name)
        if not success:
            break


def get_subject_paths(arguments):
    subject_path = arguments.subject_path
    if subject_path is not None:
        features.provide('subjects_path', [subject_path])
        return [subject_path]
    else:
        subjects_path = arguments.subjects_path
        features.provide('subjects_path', subjects_path)

        p = sub.Popen("ls -d " + subjects_path + "*/", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
        output, errors = p.communicate()
        app_paths = []
        for line in output.strip().split('\n'):
            if "hydrate" not in line:  # hydrate app doesn't compile yet, so don't bother
                app_paths.append(line.rstrip('/'))  # remove trailing forward slash

        if arguments.randomize_subjects:
            random.shuffle(app_paths)

        if arguments.limit_subjects_number != -1:
            app_paths = app_paths[0:arguments.limit_subjects_number]

        return app_paths


def check_needed_commands_available():
    if not os.path.exists(settings.ANDROID_HOME):
        cause = "Declared ANDROID_HOME points to a missing directory: " + settings.ANDROID_HOME
        logger.log_progress(cause)
        raise Exception(cause)

    if not is_command_available(adb.adb_cmd_prefix):
        cause = "Command 'adb' was not found in the path " + adb.adb_cmd_prefix
        logger.log_progress(cause)
        raise Exception(cause)

    avdmanager_path = settings.ANDROID_HOME + "tools/bin/avdmanager"
    if os.path.exists(avdmanager_path):
        features.provide('avdmanager_path', avdmanager_path)
    else:
        cause = "Command 'avdmanager' was not found in the path " + avdmanager_path
        logger.log_progress(cause)
        raise Exception(cause)

    if not is_command_available("ant"):
        cause = "Command 'ant' needed but not found."
        logger.log_progress(cause)
        raise Exception(cause)

    if not is_command_available("java"):
        cause = "Command 'java' needed but not found."
        logger.log_progress(cause)
        raise Exception(cause)

    if not is_command_available("javac"):
        cause = "Command 'javac' needed but not found."
        logger.log_progress(cause)
        raise Exception(cause)

    p = sub.Popen("java -version", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    first_line = errors.split('\n')[0].strip()
    version = first_line.split(' ')[2]
    if not version[1:-1].startswith("1.8"):
        cause = "Found java with version " + version + ", but 1.8 is needed."
        logger.log_progress(cause)
        raise Exception(cause)

    p = sub.Popen("javac -version", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    version = errors.split(' ')[1].strip()
    if not version.startswith("1.8"):
        cause = "Found javac with version " + version + ", but 1.8 is needed."
        logger.log_progress(cause)
        raise Exception(cause)

    # search for aapt in build-tools folder
    # build_tools_cmd = "ls " + settings.ANDROID_HOME + "build-tools/ | sort -r"
    # p = sub.Popen(build_tools_cmd, stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    # output, errors = p.communicate()
    # aapt_found = False
    # for folder in output.strip().split('\n'):
    #     aapt_path = settings.ANDROID_HOME + "build-tools/" + folder + "/aapt"
    #     if os.path.exists(aapt_path):
    #         aapt_found = True
    #         features.provide('aapt_path', aapt_path)
    #         break
    #
    # if not aapt_found:
    #     cause = "Command 'aapt' needed but not found in " + settings.ANDROID_HOME + "build-tools/"
    #     logger.log_progress(cause)
    #     raise Exception(cause)


def add_arguments_to_parser(parser):
    global possible_strategies, possible_test_suite_evaluators, possible_individual_generators, possible_test_runners

    # subjects related arguments
    parser.add_argument('--subject-path', dest='subject_path',
                        help='Directory where the subject to be processed is located')
    parser.add_argument('--subjects-path', dest='subjects_path',
                        help='Directory where subjects are located')
    parser.add_argument('--instrumented-subjects-path', dest='instrumented_subjects_path',
                        help='Directory where instrumented subjects will be located')
    parser.add_argument('--emma-instrument-path', dest='emma_instrument_path',
                        help='Directory where is located the EmmaInstrument template')
    parser.add_argument('--randomize-subjects', dest='randomize_subjects',
                        action='store_true', help='Randomize subjects to be processed.')
    parser.add_argument('--limit-subjects-number', type=int, dest='limit_subjects_number',
                        help='Limit the number of subjects to be processed (-1 to disable limit).')

    # budget related arguments
    parser.add_argument('-r', '--repetitions', type=int, dest='repetitions',
                        help='Choose the number of repetitions per subject to run. Default is 1.')
    parser.add_argument('--time-budget', dest='time_budget',
                        help='Choose the time budget. Format can be a number (assumed to be seconds) or a number '
                             'followed by "s" (seconds), "m" (minutes) or "h" (hours). Default is 15 minutes.')
    parser.add_argument('--evaluations-budget', type=int, dest='evaluations_budget',
                        help='Choose the evaluations budget. Default is unlimited.')

    # devices related arguments
    parser.add_argument('--real-devices-number', type=int, dest='real_devices_number',
                        help='Set the number of real devices to use. Default is 0.')
    parser.add_argument('--emulators-number', type=int, dest='emulators_number',
                        help='Set the number of emulators to use. Default is 2.')
    parser.add_argument('--avd-series', type=str, dest='avd_series',
                        help='Set the prefix of avd names. Default is Nexus_4_API_19.')

    # logging related arguments
    parser.add_argument('-v', '--verbose', action='count')
    parser.add_argument('--write-logbook', dest='write_logbook',
                        action='store_false', help='Write logbook to a file.')
    parser.add_argument('--write-history', dest='write_history',
                        action='store_false', help='Write genealogical history of individuals to a file.')
    parser.add_argument('--write-hall-of-fame', dest='write_hall_of_fame',
                        action='store_false', help='Write hall of fame of best individuals to a file.')

    # strategy related arguments
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
    parser.add_argument('-s', '--strategy', dest='strategy',
                        choices=possible_strategies.keys(), help='Strategy to be used')

    # evaluators related argurments
    possible_test_suite_evaluators = {
        "multi-objective": MultiObjectiveTestSuiteEvaluator,
        "single-objective": SingleObjectiveTestSuiteEvaluator
    }
    parser.add_argument('-e', '--evaluator', dest='evaluator',
                        choices=possible_test_suite_evaluators.keys(), help='Test suite evaluator to be used')

    # individual generator related arguments
    possible_individual_generators = {
        "default": IndividualWithoutCoverageGenerator,
        "with-coverage": IndividualWithCoverageGenerator
    }
    parser.add_argument('-ig', '--individual-generator', dest='individual_generator',
                        choices=possible_individual_generators.keys(), help='Individual generator to be used')

    # test runner related arguments
    possible_test_runners = {
        "motifcore": MotifcoreTestRunner(),
        "motifcore-nm": MotifcoreTestRunner(use_motifgene=True),
        "evolutiz": EvolutizTestRunner()
    }
    parser.add_argument('-t', '--test-runner', dest='test_runner',
                        choices=possible_test_runners.keys(), help='Test runner to be used')


def init_arguments_defaults():
    global defaults
    defaults = {
        "subjects_path": "subjects/are-we-there-yet/",
        "instrumented_subjects_path": "instrumented-subjects/",
        "emma_instrument_path": "subjects/EmmaInstrument/",
        "randomize_subjects": False,
        "limit_subjects_number": 1,
        "repetitions": 1,
        "time_budget": 15 * 60,  # 15m
        "evaluations_budget": None,
        "real_devices_number": 0,
        "emulators_number": 1,
        "avd_series": "Nexus_4_API_19",
        "verbose": 0,
        "write_logbook": True,
        "write_history": True,
        "write_hall_of_fame": True,
        "strategy": "muPlusLambda",
        "evaluator": "multi-objective",
        "individual_generator": "default",
        "test_runner": "motifcore"
    }


def config_items_type_convert(items):
    result = []
    for (key, value) in items:
        try:
            # remove duplicated whitespaces
            key = re.sub(' +', ' ', key)
            type_tag, name = key.split(' ')
            # convert value to declared type
            if value == "None":
                result.append((name, None))
            elif type_tag == "string":
                result.append((name, value))
            elif type_tag == "int":
                result.append((name, int(value)))
            elif type_tag == "bool":
                result.append((name, bool(value)))
            else:
                raise ValueError('Invalid type tag "%s" found in config file file.' % type_tag)
                # alternatively: "everything else defaults to string"
        except Exception as e:
            raise ValueError('Unable to convert value for "%s" to declared type "%s".' % (name, type_tag))
    return result


def parse_config_file():
    global conf_parser, args, remaining_argv
    # Parse any conf_file specification
    # We make this parser with add_help=False so that
    # it doesn't parse -h and print help.
    conf_parser = argparse.ArgumentParser(
        # description='Run Evolutiz with different strategies.'
        description=__doc__,  # printed with -h/--help
        # Don't mess with format of description
        formatter_class=argparse.RawDescriptionHelpFormatter,
        # Turn off help, so we print all options in response to -h
        add_help=False
    )
    conf_parser.add_argument("-c", "--conf_file",
                             help="Specify config file", metavar="FILE")
    args, remaining_argv = conf_parser.parse_known_args()
    init_arguments_defaults()
    if args.conf_file:
        config = ConfigParser.SafeConfigParser()
        config.read([args.conf_file])
        defaults.update(dict(config_items_type_convert(config.items(ConfigParser.DEFAULTSECT))))


def get_fitness_values_of_individual(individual):
    return individual.fitness.values


def provide_features():
    # define subjects
    features.provide('instrumented_subjects_path', args.instrumented_subjects_path)
    features.provide('emma_instrument_path', args.emma_instrument_path)
    # define budget and repetitions
    features.provide('repetitions', args.repetitions)
    features.provide('budget_manager',
                     BudgetManager(time_budget=args.time_budget, evaluations_budget=args.evaluations_budget))
    # define devices configuration
    features.provide('emulators_number', args.emulators_number)
    features.provide('real_devices_number', args.real_devices_number)
    features.provide('avd_series', args.avd_series)
    features.provide('strategy', possible_strategies[args.strategy])
    features.provide('test_suite_evaluator', possible_test_suite_evaluators[args.evaluator])
    features.provide('test_runner', possible_test_runners[args.test_runner])
    features.provide('individual_generator', possible_individual_generators[args.individual_generator])
    features.provide('population_generator', PopulationGenerator)
    features.provide('verbose_level', args.verbose)
    features.provide('write_logbook', args.write_logbook)
    features.provide('write_history', args.write_history)
    features.provide('write_hall_of_fame', args.write_hall_of_fame)

    # singletons
    features.provide('toolbox', Toolbox())
    features.provide('device_manager', DeviceManager())

    stats = tools.Statistics(get_fitness_values_of_individual)
    # Use axis = 0 to get the desired statistic computed across all fitness values
    # Example:
    # >>> a = np.array([[1, 2], [3, 4]])
    # >>> np.mean(a, axis=0)
    # array([ 2.,  3.])
    stats.register("avg", numpy.mean, axis=0)
    stats.register("std", numpy.std, axis=0)
    stats.register("min", numpy.min, axis=0)
    stats.register("max", numpy.max, axis=0)
    features.provide('stats', stats)


if __name__ == "__main__":
    logger.prepare()
    logger.clear_progress()

    parse_config_file()

    # Parse rest of arguments
    # Don't suppress add_help here so it will handle -h
    parser = argparse.ArgumentParser(
        # Inherit options from config_parser
        parents=[conf_parser],
    )
    parser.set_defaults(**defaults)

    add_arguments_to_parser(parser)

    args = parser.parse_args(remaining_argv)

    # --------------------------------------------------------------------------------- #
    # --------------------------------------------------------------------------------- #

    check_needed_commands_available()

    app_paths = get_subject_paths(args)
    strategy_with_runner_name = args.strategy + "-" + args.test_runner

    provide_features()

    # run Evolutiz
    logger.log_progress("Evolutiz (" +
                        args.strategy + ", " +
                        args.evaluator + ", " +
                        args.test_runner + ")")

    logger.log_progress("\nSubjects to be processed: " + ''.join(map(lambda p: "\n -" + p, app_paths)))

    run(strategy_with_runner_name, app_paths)

    # process results
    # results_per_app = process_results(app_paths)

    # logger.log_progress("\n" + str(results_per_app))
    # print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
