#!.env/bin/python
# coding=utf-8

import argparse
import random
import re
import threading
import time
import traceback
from configparser import ConfigParser, DEFAULTSECT
from typing import Any, List, Type, Iterable, TYPE_CHECKING

import numpy
from deap import tools
from deap.base import Toolbox

from algorithms.check_instrumentation import CheckInstrumentation
from algorithms.dyna_mosa import DynaMosa
from algorithms.monotonic import Monotonic
from algorithms.mosa import Mosa
from algorithms.mu_comma_lambda import MuCommaLambda
from algorithms.mu_plus_lambda import MuPlusLambda
from algorithms.one_plus_lambda_comma_lambda import OnePlusLambdaCommaLambda
from algorithms.random_search import RandomSearch
from algorithms.standard import Standard
from algorithms.steady_state import SteadyState
from algorithms.strategy import Strategy
from concurrency.multiple_queue_consumer_thread import MultipleQueueConsumerThread
from coverage.coverage_fetcher import CoverageFetcher
from coverage.ella.ella_coverage_fetcher import EllaCoverageFetcher
from coverage.emma.emma_coverage_fetcher import EmmaCoverageFetcher
from coverage.jacoco.jacoco_coverage_fetcher import JacocoCoverageFetcher
from dependency_injection.feature_broker import features
from devices import adb
from devices.avd_manager import AvdManager
from devices.device_manager import DeviceManager
from evaluation.multi_objective import MultiObjectiveTestSuiteEvaluator
from evaluation.single_objective import SingleObjectiveTestSuiteEvaluator
from evaluation.test_suite_evaluator import TestSuiteEvaluator
from generation.individual import Individual
from generation.individual_generator import IndividualGenerator
from generation.individual_with_coverage_generator import IndividualWithCoverageFetcherGenerator
from generation.individual_without_coverage_generator import IndividualWithoutCoverageGenerator
from generation.population_generator import PopulationGenerator
from postprocess.evaluate_scripts import EvaluateScripts
from test_runner.evolutiz.evolutiz_test_runner import EvolutizTestRunner
from test_runner.evolutiz_atomic.evolutiz_atomic_test_runner import EvolutizAtomicTestRunner
from test_runner.motifcore.motifcore_test_runner import MotifcoreTestRunner
from test_runner.test_runner import TestRunner
from util.budget_manager import BudgetManager
from util.command import *
from util.compress import compress_results
from util.evolutiz import Evolutiz

if TYPE_CHECKING:
    import os
    from typing import Dict, Tuple
    import settings
    from dependency_injection.required_feature import RequiredFeature
    from util import logger

    conf_parser: argparse.ArgumentParser
    defaults: Dict[str, Any]
    remaining_argv: List[str]

possible_strategies: Dict[str, Type[Strategy]]
possible_test_suite_evaluators: Dict[str, Type[TestSuiteEvaluator]]
possible_individual_generators: Dict[str, Type[IndividualGenerator]]
possible_test_runners: Dict[str, TestRunner]
possible_coverage_fetchers: Dict[str, CoverageFetcher]


def run_one_app(strategy_with_runner_name: str) -> bool:
    app_path = RequiredFeature('app_path').request()
    repetitions = RequiredFeature('repetitions').request()
    repetitions_offset = RequiredFeature('repetitions_offset').request()
    budget_manager = RequiredFeature('budget_manager').request()
    test_suite_evaluator = RequiredFeature('test_suite_evaluator').request()
    verbose_level = RequiredFeature('verbose_level').request()

    continue_on_repetition_failure = RequiredFeature('continue_on_repetition_failure').request()

    app_name = os.path.basename(app_path)

    try:
        there_was_a_failed_repetition = False
        for repetition in range(repetitions_offset, repetitions):
            try:
                os.chdir(settings.WORKING_DIR)

                logbook = tools.Logbook()
                logbook.header = ['gen']
                features.provide('logbook', logbook)

                history = tools.History()
                features.provide('history', history)

                hall_of_fame = test_suite_evaluator.new_hall_of_fame()
                features.provide('hall_of_fame', hall_of_fame)

                result_dir = prepare_result_dir(app_name, repetition, strategy_with_runner_name)

                get_emulators_running(result_dir)

                test_generator = Evolutiz()

                budget_manager.start_budget()

                logger.log_progress(f"\n-----> Starting repetition: {str(repetition)} for app: {app_name}, "
                                    f"initial timestamp is: {str(budget_manager.start_time)}")
                test_generator.run()

                logger.log_progress(f"\nEvolutiz finished for app: {app_name}")

                time_budget_used = budget_manager.get_time_budget_used()
                if time_budget_used is not None:
                    logger.log_progress(f"\nTime budget used: {time_budget_used:.2f} seconds\n")

                evaluations_budget_used = budget_manager.get_evaluations_budget_used()
                if evaluations_budget_used is not None:
                    logger.log_progress(f"\nEvaluations budget used: {evaluations_budget_used:d}\n")

                # wait for all MultipleQueueConsumerThread to terminate
                wait_for_working_threas_to_finish()
            except Exception as e:
                there_was_a_failed_repetition = True
                if not continue_on_repetition_failure:
                    # there was a problem during current repetition, halt further executions of this subject
                    raise e
                # otherwise, keep running the remaining repetitions

        return not there_was_a_failed_repetition
    except Exception as e:
        logger.log_progress(f"\nThere was an error running evolutiz on app: {app_name}")
        if verbose_level > 0:
            logger.log_progress(f"\n{str(e)}")
        if verbose_level > 1:
            logger.log_progress(f"\n{traceback.format_exc()}")
        traceback.print_exc()
        return False


def wait_for_working_threas_to_finish() -> None:
    threads_working = [thread for thread in threading.enumerate()
                       if isinstance(thread, MultipleQueueConsumerThread) and thread.isAlive()]
    for thread in threads_working:
        thread.stop()

    while True:
        threads_working = [thread for thread in threading.enumerate()
                           if isinstance(thread, MultipleQueueConsumerThread) and thread.isAlive()]
        if len(threads_working) == 0:
            return

        time.sleep(3)


def get_emulators_running(result_dir: str) -> None:
    """Reboot all devices before starting a repetition."""
    device_manager = RequiredFeature('device_manager').request()

    if len(device_manager.get_devices(refresh=True)) > 0:
        device_manager.shutdown_emulators(remove=True)

    device_manager.boot_emulators(wait_to_be_ready=True)


def prepare_result_dir(app_name: str, repetition: int, strategy_with_runner_name: str) -> str:
    repetition_folder = str(repetition)
    algorithm_folder = strategy_with_runner_name

    # check if the user wants to overwrite repetition_folder because it's evaluating scripts
    evaluate_scripts_repetition_number = RequiredFeature('evaluate_scripts_repetition_number').request()
    if evaluate_scripts_repetition_number is not None:
        repetition_folder = str(evaluate_scripts_repetition_number)

    evaluate_scripts_algorithm_name = RequiredFeature('evaluate_scripts_algorithm_name').request()
    if evaluate_scripts_algorithm_name is not None:
        algorithm_folder = str(evaluate_scripts_algorithm_name)

    # build result_dir path
    result_dir = f"{settings.WORKING_DIR}results/{algorithm_folder}/{app_name}/{repetition_folder}"

    skip_subject_if_logbook_in_results = RequiredFeature('skip_subject_if_logbook_in_results').request()
    if skip_subject_if_logbook_in_results:
        if os.path.exists(f"{result_dir}/logbook.pickle"):
            raise Exception(f"Skipping run for {algorithm_folder}/{app_name}/{repetition_folder} since there is "
                            f"already a results folder with a valid logbook file inside it.")

    # clean and create result_dir
    os.system(f"rm -rf {result_dir}/*{logger.redirect_string()}")
    result_code = os.system(f"mkdir -p {result_dir}")
    if result_code != 0: raise Exception("Unable to create result dir")

    os.system(f"mkdir -p {result_dir}/intermediate")
    os.system(f"mkdir -p {result_dir}/coverage")
    os.system(f"mkdir -p {result_dir}/crashes")

    features.provide('result_dir', result_dir)
    adb.adb_logs_dir = result_dir

    return result_dir


def run(strategy_name: str, app_paths: List[str]) -> None:
    compress = RequiredFeature('compress').request()
    continue_on_subject_failure = RequiredFeature('continue_on_subject_failure').request()

    for i in range(0, len(app_paths)):
        features.provide('app_path', app_paths[i])

        success = run_one_app(strategy_name)
        if not success and not continue_on_subject_failure:
            break

        if compress:
            compress_results(strategy_name)


def get_subject_paths(arguments: argparse.Namespace) -> List[str]:
    features.provide('assume_subjects_instrumented', arguments.assume_subjects_instrumented)

    subject_path = arguments.subject_path
    if subject_path is not None:
        features.provide('subjects_path', [subject_path])
        return [subject_path.rstrip('/')]
    else:
        subjects_path = arguments.subjects_path.rstrip('/') + '/'
        features.provide('subjects_path', subjects_path)

        app_paths = []

        if arguments.assume_subjects_instrumented:
            output, errors, result_code = run_cmd(f"find -L {subjects_path} -name \"*.apk\"")
            for line in output.strip().split('\n'):
                app_paths.append(line.rstrip('/'))  # remove trailing forward slash
        else:
            output, errors, result_code = run_cmd(f"ls -1 -d \"{subjects_path}\"*")
            for line in output.strip().split('\n'):
                path = line.rstrip('/')
                if os.path.isdir(path):
                    app_paths.append(path)
                elif os.path.isfile(path):
                    if path.endswith(".apk"):
                        if "hydrate" not in line:  # hydrate app doesn't compile yet, so don't bother
                            app_paths.append(path)  # remove trailing forward slash
                    else:
                        logger.log_progress(f"Ignoring non-APK file {path} in subjects path")
                else:
                    # special file (e.g., socket)
                    logger.log_progress(f"Ignoring special file {path} in subjects path")
                    continue

        if arguments.randomize_subjects:
            random.shuffle(app_paths)

        if arguments.limit_subjects_number != -1:
            app_paths = app_paths[0:arguments.limit_subjects_number]

        # return list(filter(lambda p: 'com.zhiliaoapp.musically' in p, app_paths))
        return app_paths


def check_virtualbox_is_not_running() -> None:
    if is_command_available("vboxmanage"):
        output, errors, result_code = run_cmd("vboxmanage list runningvms")
        if output.strip() != "":
            cause = "Android emulators can't be run while VirtualBox is running.\n" \
                    "Shutdown VirtualBox before running Evolutiz."
            logger.log_progress(cause)
            raise Exception(cause)


def check_needed_commands_available() -> None:
    if not os.path.exists(settings.ANDROID_HOME):
        cause = f"Declared ANDROID_HOME points to a missing directory: {settings.ANDROID_HOME}"
        logger.log_progress(cause)
        raise Exception(cause)

    if not is_command_available(adb.adb_cmd_prefix):
        cause = f"Command \'adb\' was not found in the path {adb.adb_cmd_prefix}"
        logger.log_progress(cause)
        raise Exception(cause)

    avd_manager_path = f"{settings.ANDROID_HOME}tools/bin/avdmanager"
    if os.path.exists(avd_manager_path):
        features.provide('avd_manager_path', avd_manager_path)
    else:
        cause = f"Command \'avdmanager\' was not found in the path {avd_manager_path}"
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

    output, errors, result_code = run_cmd("java -version")
    first_line = errors.split('\n')[0].strip()
    version = first_line.split(' ')[2]
    if not version[1:-1].startswith("1.8"):
        cause = f"Found java with version {version}, but 1.8 is needed."
        logger.log_progress(cause)
        raise Exception(cause)

    output, errors, result_code = run_cmd("javac -version")
    version = errors.split(' ')[1].strip()
    if not version.startswith("1.8"):
        cause = f"Found javac with version {version}, but 1.8 is needed."
        logger.log_progress(cause)
        raise Exception(cause)

    # search for aapt in build-tools folder
    # build_tools_cmd = "ls " + settings.ANDROID_HOME + "build-tools/ | sort -r"
    # output, errors, result_code = run_cmd(build_tools_cmd)
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


def add_arguments_to_parser(parser: argparse.ArgumentParser) -> None:
    global possible_strategies, possible_test_suite_evaluators, possible_individual_generators, possible_test_runners, \
        possible_coverage_fetchers

    # subjects related arguments
    parser.add_argument('--subject-path', dest='subject_path',
                        help='Directory where the subject to be processed is located')
    parser.add_argument('--continue-on-subject-failure', dest='continue_on_subject_failure',
                        action='store_true', help='Continue processing other subjects if one fails')
    parser.add_argument('--continue-on-repetition-failure', dest='continue_on_repetition_failure',
                        action='store_true', help='Continue processing other repetitions if one fails')
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
    parser.add_argument('--assume-subjects-instrumented', dest='assume_subjects_instrumented',
                        action='store_true', help='Search for APKs in the provided subjects path. '
                                                  'These files are assumed to be already instrumented.')

    # budget related arguments
    parser.add_argument('-r', '--repetitions', type=int, dest='repetitions',
                        help='Choose the number of repetitions per subject to run. Default is 1.')
    parser.add_argument('-ro', '--repetitions-offset', type=int, dest='repetitions_offset',
                        help='Choose the offset from which to start counting the repetitions. '
                             'For example, an offset of 5 means that the first repetition will be the number 5.')
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
    parser.add_argument('--no-logbook', dest='write_logbook',
                        action='store_false', help='Write logbook to a file.')
    parser.add_argument('--no-history', dest='write_history',
                        action='store_false', help='Write genealogical history of individuals to a file.')
    parser.add_argument('--no-hall-of-fame', dest='write_hall_of_fame',
                        action='store_false', help='Write hall of fame of best individuals to a file.')
    parser.add_argument('--no-compress', dest='compress',
                        action='store_false', help='Compress results after successful run.')

    # strategy related arguments
    possible_strategies = {
        "standard": Standard,
        "monotonic": Monotonic,
        "steady": SteadyState,
        "muPlusLambda": MuPlusLambda,
        "muCommaLambda": MuCommaLambda,
        "onePlusLambdaCommaLambda": OnePlusLambdaCommaLambda,
        "mosa": Mosa,
        "dynaMosa": DynaMosa,
        "randomSearch": RandomSearch,
        "evaluateScripts": EvaluateScripts,
        "checkInstrumentation": CheckInstrumentation,
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
        "with-coverage": IndividualWithCoverageFetcherGenerator
    }
    parser.add_argument('-ig', '--individual-generator', dest='individual_generator',
                        choices=possible_individual_generators.keys(), help='Individual generator to be used')

    # test runner related arguments
    possible_test_runners = {
        "motifcore": MotifcoreTestRunner(),
        "motifcore-no-motifgenes": MotifcoreTestRunner(use_motifgene=False),
        "evolutiz": EvolutizTestRunner(),
        "evolutiz-atomic": EvolutizAtomicTestRunner(),
        "evolutiz-atomic-no-motifgenes": EvolutizAtomicTestRunner(use_motifgene=False),
    }
    parser.add_argument('-t', '--test-runner', dest='test_runner',
                        choices=possible_test_runners.keys(), help='Test runner to be used')

    # coverage fetchers related arguments
    possible_coverage_fetchers = {
        "emma": EmmaCoverageFetcher(),
        "jacoco": JacocoCoverageFetcher(),
        "ella": EllaCoverageFetcher(),
    }
    parser.add_argument('--coverage', dest='coverage',
                        choices=possible_coverage_fetchers.keys(), help='Coverage fetcher to be used')

    parser.add_argument('--seed', type=int, dest='seed',
                        help='Choose the random seed to be used in the Evolutiz runner. This seed doesn\'t affect the '
                             'one used inside the test runners.')

    parser.add_argument('--evaluate-scripts-folder-path', dest='evaluate_scripts_folder_path',
                        help='Path to folder with scripts to evaluate. '
                             'For example: results/steady-motifcore-multi-objective/arity/0/intermediate/')

    parser.add_argument('--evaluate-scripts-repetition-number', type=int, dest='evaluate_scripts_repetition_number',
                        help='This argument is useful for changing the result directory path when evaluating several '
                             'repetitions of a same algorithm-app.')

    parser.add_argument('--evaluate-scripts-algorithm-name', dest='evaluate_scripts_algorithm_name',
                        help='This argument is useful for changing the result directory path when evaluating scripts '
                             'from a previous run.')

    parser.add_argument('--skip-subject-if-logbook-in-results', dest='skip_subject_if_logbook_in_results',
                        action='store_true', help='Skip a subject\'s repetition if there is already a result folder '
                                                  'with a non-empty logbook inside it.')


def init_arguments_defaults() -> None:
    global defaults
    defaults = {
        "subjects_path": "subjects/are-we-there-yet/",
        "instrumented_subjects_path": "instrumented-subjects/",
        "emma_instrument_path": "subjects/EmmaInstrument/",
        "randomize_subjects": False,
        "assume_subjects_instrumented": False,
        "continue_on_subject_failure": True,
        "continue_on_repetition_failure": True,
        "limit_subjects_number": 1,
        "repetitions": 1,
        "repetitions_offset": 0,
        "time_budget": 15 * 60,  # 15m
        "evaluations_budget": None,
        "real_devices_number": 0,
        "emulators_number": 1,
        "avd_series": "Nexus_4_API_19",
        "verbose": 0,
        "write_logbook": True,
        "write_history": True,
        "write_hall_of_fame": True,
        "compress": True,
        "strategy": "muPlusLambda",
        "evaluator": "multi-objective",
        "individual_generator": "default",
        "test_runner": "motifcore",
        "coverage": "emma",
        "seed": None,
        'evaluate_scripts_folder_path': None,
        'evaluate_scripts_repetition_number': None,
        'evaluate_scripts_algorithm_name': None,
        'skip_subject_if_logbook_in_results': False,
    }


def config_items_type_convert(items: Iterable[Tuple[str, Any]]) -> List[Tuple[str, Any]]:
    result: List[Tuple[str, Any]] = []
    key: str
    value: Any

    for (key, value) in items:
        try:
            # remove duplicated whitespaces
            key = re.sub(' +', ' ', key)
            key_split = key.split(' ')
            if len(key_split) != 2:
                raise ValueError(f'Invalid type key "{key}" found in config file file.')

            type_tag = key_split[0]
            name = key_split[1]

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
                raise ValueError(f'Invalid type tag "{type_tag}" found in config file file.')
                # alternatively: "everything else defaults to string"
        except Exception as e:
            raise ValueError(f'Unable to convert value for "{name}" to declared type "{type_tag}".')
    return result


def parse_config_file() -> None:
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
        config = ConfigParser()
        config.read([args.conf_file])
        defaults.update(dict(config_items_type_convert(config.items(DEFAULTSECT))))


def provide_features() -> None:
    # define subjects
    features.provide('instrumented_subjects_path', args.instrumented_subjects_path)
    features.provide('continue_on_subject_failure', args.continue_on_subject_failure)
    features.provide('continue_on_repetition_failure', args.continue_on_repetition_failure)

    # define budget and repetitions
    features.provide('repetitions', args.repetitions)
    features.provide('repetitions_offset', args.repetitions_offset)
    features.provide('budget_manager',
                     BudgetManager(time_budget=args.time_budget, evaluations_budget=args.evaluations_budget))

    # define devices configuration
    features.provide('emulators_number', args.emulators_number)
    features.provide('real_devices_number', args.real_devices_number)
    features.provide('avd_series', args.avd_series)
    features.provide('avd_manager', AvdManager())
    features.provide('strategy', possible_strategies[args.strategy])
    features.provide('test_suite_evaluator', possible_test_suite_evaluators[args.evaluator])

    # define test runner
    test_runner = possible_test_runners[args.test_runner]
    features.provide('test_runner', test_runner)
    test_runner.register_minimum_api()

    # define coverage fetcher app instrumentator
    coverage_fetcher = possible_coverage_fetchers[args.coverage]
    features.provide('coverage_fetcher', coverage_fetcher)
    coverage_fetcher.register_app_instrumentator()

    features.provide('emma_instrument_path', args.emma_instrument_path)

    # define individual and population generators
    features.provide('individual_generator', possible_individual_generators[args.individual_generator])
    features.provide('population_generator', PopulationGenerator)

    # define extras
    features.provide('verbose_level', args.verbose)
    features.provide('write_logbook', args.write_logbook)
    features.provide('write_history', args.write_history)
    features.provide('write_hall_of_fame', args.write_hall_of_fame)
    features.provide('compress', args.compress)

    # singletons
    toolbox = Toolbox()
    toolbox.register("selectBest", tools.selBest)
    features.provide('toolbox', toolbox)
    features.provide('device_manager', DeviceManager())

    features.provide('evaluate_scripts_folder_path', args.evaluate_scripts_folder_path)
    features.provide('evaluate_scripts_repetition_number', args.evaluate_scripts_repetition_number)
    features.provide('evaluate_scripts_algorithm_name', args.evaluate_scripts_algorithm_name)

    features.provide('skip_subject_if_logbook_in_results', args.skip_subject_if_logbook_in_results)


if __name__ == "__main__":
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

    logger.prepare()
    logger.clear_progress()

    check_virtualbox_is_not_running()

    check_needed_commands_available()

    app_paths = get_subject_paths(args)
    strategy_with_runner_name = f"{args.strategy}-{args.test_runner}-{args.evaluator}"

    random.seed(args.seed)

    provide_features()

    # run Evolutiz
    logger.log_progress(f"\nEvolutiz ("
                        f"{args.strategy}, "
                        f"{args.evaluator}, "
                        f"{args.test_runner}, "
                        f"{args.coverage}, "
                        f"emulators: {str(args.emulators_number)}, "
                        f"real devices: {str(args.real_devices_number)})")

    output, errors, result_code = run_cmd("git rev-parse HEAD")
    logger.log_progress("\nLast Git commit hash: {0}".format(output.split('\n')[0]))

    logger.log_progress(f"\nVerbose level: {str(args.verbose)}")
    logger.log_progress("\nSubjects to be processed: {0}".format(''.join(map(lambda p: "\n -" + p, app_paths))))

    run(strategy_with_runner_name, app_paths)

    # process results
    # results_per_app = process_results(app_paths)

    # logger.log_progress("\n" + str(results_per_app))
    # print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
