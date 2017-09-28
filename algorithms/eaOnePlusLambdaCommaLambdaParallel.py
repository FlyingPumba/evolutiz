import random
import pickle
from deap import tools

from algorithms.parallalel_evaluation import evaluate_in_parallel

def evolve(population, toolbox, lambda_, cxpb, mutpb, ngen, apk_dir, package_name,
		   stats=None, verbose=__debug__):
	# 1 + (lambda, lambda) starts with population of only one individual
	assert len(population) == 1

	logbook = tools.Logbook()
	logbook.header = ['gen', 'nevals'] + (stats.fields if stats else [])

	# Evaluate the individuals with an invalid fitness
	invalid_ind = [ind for ind in population if not ind.fitness.valid]
	# fitnesses = toolbox.map(toolbox.evaluate, invalid_ind)
	# for ind, fit in zip(invalid_ind, fitnesses):
	# 	ind.fitness.values = fit
	evaluate_in_parallel(toolbox.evaluate, invalid_ind, apk_dir, package_name, 0)

	# discard invalid population individual
	for i in range(len(population) - 1, -1, -1):
		if not population[i].fitness.valid:
			del population[i]

	record = stats.compile(population) if stats is not None else {}
	logbook.record(gen=0, nevals=len(invalid_ind), **record)
	if verbose:
		print logbook.stream

	# Begin the generational process
	for gen in range(1, ngen + 1):

		print "Starting generation ", gen

		# Vary the population
		offspring = varOr(population, toolbox, lambda_, apk_dir, package_name, gen)

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in offspring if not ind.fitness.valid]

		# this function will eval and match each invalid_ind to its fitness
		evaluate_in_parallel(toolbox.evaluate, invalid_ind, apk_dir, package_name, gen)

		best_ind = tools.sortNondominated(offspring + population, 1)
		if (best_ind != population[0]):
			# the parent was improved by one individual of the offspring
			population = [best_ind]

		# Update the statistics with the new population
		record = stats.compile(population) if stats is not None else {}
		logbook.record(gen=gen, nevals=len(invalid_ind), **record)
		if verbose:
			print logbook.stream

		# in case interrupted
		logbook_file = open(apk_dir + "/intermediate/logbook.pickle", 'wb')
		pickle.dump(logbook, logbook_file)
		logbook_file.close()

	return population, logbook


def varOr(population, toolbox, lambda_, apk_dir, package_name, gen):
	parent = population[0]
	# generate lambda_ mutants
	mutants = []
	for _ in xrange(lambda_):
		ind = toolbox.clone(parent)
		ind, = toolbox.mutate(ind)
		del ind.fitness.values
		mutants.append(ind)

	evaluate_in_parallel(toolbox.evaluate, mutants, apk_dir, package_name, gen)
	best_ind = tools.sortNondominated(mutants, 1)

	# generate lambda_ offspring
	offspring = []
	while len(offspring) < lambda_:
		p1 = toolbox.clone(parent)
		p2 = toolbox.clone(best_ind)
		ind1, ind2 = toolbox.mate(p1, p2)
		del ind1.fitness.values
		del ind2.fitness.values
		offspring.append(ind1)
		offspring.append(ind2)

	return offspring
