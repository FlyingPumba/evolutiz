import multiprocessing as mp
import os

import settings
from dependency_injection.required_feature import RequiredFeature
from util.command import run_cmd

def compress_results(strategy_with_runner_name):
    app_path = RequiredFeature('app_path').request()
    app_name = os.path.basename(app_path)

    repetitions_dir = settings.WORKING_DIR + "results/" + strategy_with_runner_name + "/" + app_name + "/"

    output, errors, return_code = run_cmd("find " + repetitions_dir + " -maxdepth 1 -mindepth 1 -type d")
    repetition_dir_paths = output.split('\n')

    pool = mp.Pool(processes=len(repetition_dir_paths))
    for path in repetition_dir_paths:
        pool.apply_async(compress_repetition, args=(path,))
    pool.close()
    pool.join()

def compress_repetition(path):
    run_cmd("tar --use-compress-program=pigz -cf " + path + ".tar.gz " + path)