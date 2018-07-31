import matplotlib
# Force matplotlib to not use any Xwindows backend.
matplotlib.use('Agg')

import settings
from test_generator.evolutiz import Evolutiz

from devices.device_manager import DeviceManager
from test_runner.evolutiz_runner import EvolutizTestRunner
from test_runner.motifcore_runner import MotifcoreTestRunner

import argparse
import os
import subprocess as sub
import traceback

import logger
from algorithms.eaDynaMosaParallel import eaDynaMosaParallel
from algorithms.eaMonotonicParallel import eaMonotonicParallel
from algorithms.eaMosaParallel import eaMosaParallel
from algorithms.eaMuPlusLambdaParallel import eaMuPlusLambdaParallel
from algorithms.eaOnePlusLambdaCommaLambdaParallel import eaOnePlusLambdaCommaLambdaParallel
from algorithms.eaStandardParallel import eaStandardParallel
from algorithms.eaSteadyStateParallel import eaSteadyStateParallel
from algorithms.randomParallel import randomParallel
from devices import adb

def run_one_app(strategy_with_runner_name, strategy_class, test_runner, app_path):
    device_manager = DeviceManager()

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

            adb.adb_logs_dir = result_dir

            #  reboot all devices and restart adb server before starting a repetition
            adb.restart_server()
            device_manager.reboot_devices()

            logger.log_progress("\n-----> Starting repetition: " + str(repetition) + " for app: " + folder_name)

            device_manager.clean_sdcard()

            test_generator = Evolutiz(device_manager, test_runner, strategy_class, result_dir)
            test_generator.run(app_path)

        return True
    except Exception as e:
        logger.log_progress("\nThere was an error running evolutiz on app: " + folder_name)
        traceback.print_exc()
        return False

def run(strategy_name, strategy, test_runner, app_paths):
    for i in range(0, len(app_paths)):
        success = run_one_app(strategy_name, strategy, test_runner, app_paths[i])
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


if __name__ == "__main__":
    possible_strategies = {
        "standard": eaStandardParallel,
        "monotonic": eaMonotonicParallel,
        "steady": eaSteadyStateParallel,
        "muPlusLambda": eaMuPlusLambdaParallel,
        "onePlusLambdaCommaLambda": eaOnePlusLambdaCommaLambdaParallel,
        "mosa": eaMosaParallel,
        "dynaMosa": eaDynaMosaParallel,
        "random": randomParallel
    }

    possible_test_runners = {
        "motifcore": MotifcoreTestRunner(),
        "motifcore-nm": MotifcoreTestRunner(use_motifgene=True),
        "evolutiz": EvolutizTestRunner()
    }

    # parse args
    parser = argparse.ArgumentParser(description='Run Evolutiz with different strategies.')
    parser.add_argument('-d', '--subjects', dest='subjects_directory', default=settings.SUBJECTS_PATH + 'are-we-there-yet/',
                        help='Directory where subjects are located')
    parser.add_argument('-s', '--strategy', dest='selected_strategy', default='muPlusLambda',
                        choices=possible_strategies.keys(), help='Strategy to be used')
    parser.add_argument('-t', '--test-runner', dest='selected_test_runner', default='motifcore',
                        choices=possible_test_runners.keys(), help='Test runner to be used')
    # parser.add_argument('-nm', '--no-motifgene', dest='use_motifgene', action='store_false',
    #                     default=True, help='Disable motifgenes')

    args = parser.parse_args()
    app_paths = get_subject_paths(args.subjects_directory)[0:1]

    strategy_with_runner_name = args.selected_strategy + "-" + args.selected_test_runner
    strategy_class = possible_strategies[args.selected_strategy]
    test_runner = possible_test_runners[args.selected_test_runner]

    # run Evolutiz exp
    logger.prepare()
    logger.clear_progress()
    logger.log_progress("Evolutiz (" + args.selected_strategy + ", " + args.selected_test_runner + ")")

    run(strategy_with_runner_name, strategy_class, test_runner, app_paths)

    # process results
    # results_per_app = process_results(app_paths)

    # logger.log_progress("\n" + str(results_per_app))
    # print str(results_per_app)

    # recover stdout and stderr
    logger.restore()
