import os

import settings
from coverages import act_coverage, emma_coverage


def eval_suite(test_runner, individual, device, result_dir, apk_dir, package_name, gen, pop):
    script_path = []

    for index, seq in enumerate(individual):
        # generate script file list
        filename = result_dir + "/intermediate/motifcore.evo.script." + str(gen) + "." + str(pop) + "." + str(index)
        # check that directory exists before creating file
        dirname = os.path.dirname(filename)
        if not os.path.exists(dirname):
            os.makedirs(dirname)
        with open(filename, "w+") as script:
            script.write(settings.MOTIFCORE_SCRIPT_HEADER)

            for line in seq:
                script.write(line + "\n")

        script_path.append(os.path.abspath(filename))

    if apk_dir.endswith(".apk_output"):
        coverage, num_crashes = act_coverage.get_suite_coverage(script_path, device, apk_dir, package_name, gen, pop)
    else:
        coverage, num_crashes, scripts_crash_status = emma_coverage.get_suite_coverage(test_runner,
                                                                                       script_path, device,
                                                                                       result_dir, apk_dir,
                                                                                       package_name, gen, pop)
    print "### Coverage = ", coverage
    print "### #Crashes = ", num_crashes

    # fitness = coverage + num_crashes
    fitness = coverage
    return pop, fitness, device
