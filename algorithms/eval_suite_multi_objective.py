import sys

import numpy
import os

import settings
from coverages import act_coverage, emma_coverage


def eval_suite(test_runner, individual, device, result_dir, apk_dir, package_name, gen, pop):
	# for get_motifcore_suite_coverage
	script_path = []

	# for length objective
	if settings.DEBUG:
		print "Generating motifcore scripts to evaluate individual"
	suite_lengths = {}
	for index, seq in enumerate(individual):
		# generate script file list
		filename = result_dir + "/intermediate/motifcore.evo.script." + str(gen) + "." + str(pop) + "." + str(index)
		# check that directory exists before creating file
		dirname = os.path.dirname(filename)
		if not os.path.exists(dirname):
			os.makedirs(dirname)
		with open(filename, "w+") as script:
			script.write(settings.MOTIFCORE_SCRIPT_HEADER)

			length = 0
			for line in seq:
				script.write(line + "\n")
				length += 1

		script = os.path.abspath(filename)
		suite_lengths[script] = length
		script_path.append(script)

	scripts_crash_status = {}
	# give a script and package, return the coverage by running all seqs
	if apk_dir.endswith(".apk_output"):
		coverage, num_crashes = act_coverage.get_suite_coverage(script_path, device, apk_dir, package_name, gen, pop)
	else:
		coverage, num_crashes, scripts_crash_status = emma_coverage.get_suite_coverage(test_runner, script_path, device,
																 result_dir, apk_dir, package_name, gen, pop)

	# remove from suite lengths the scripts that did NOT cause a crash
	for script, had_crash in scripts_crash_status.iteritems():
		if not had_crash:
			suite_lengths.pop(script, None)

	print "### Coverage = ", coverage
	print "### Lengths = ", suite_lengths
	print "### #Crashes = ", num_crashes

	# 1st obj: coverage, 2nd: average seq length of the suite, 3nd: #crashes
	if suite_lengths:
		return pop, (coverage, numpy.mean(suite_lengths.values()), num_crashes), device
	else:
		return pop, (coverage, sys.maxint, num_crashes), device
