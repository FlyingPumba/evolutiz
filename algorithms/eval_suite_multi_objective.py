import os

import numpy

import settings
from coverages import act_coverage, emma_coverage

def eval_suite(individual, device, result_dir, apk_dir, package_name, gen, pop):
	# for get_motifcore_suite_coverage
	script_path = []

	# for length objective
	if settings.DEBUG:
		print "Generating motifcore scripts to evaluate individual"
	suite_lengths = []
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

			suite_lengths.append(length)
		script_path.append(os.path.abspath(filename))
	# give a script and package, return the coverage by running all seqs
	if settings.DEBUG:
		print "Sending motifcore scripts to evaluate individual"
	if apk_dir.endswith(".apk_output"):
		coverage, num_crashes = act_coverage.get_suite_coverage(script_path, device, apk_dir, package_name, gen, pop)
	else:
		coverage, num_crashes = emma_coverage.get_suite_coverage(script_path, device, result_dir, apk_dir, package_name, gen, pop)
	print "### Coverage = ", coverage
	print "### Lengths = ", suite_lengths
	print "### #Crashes = ", num_crashes

	# 1st obj: coverage, 2nd: average seq length of the suite, 3nd: #crashes
	return pop, (coverage, numpy.mean(suite_lengths), num_crashes), device