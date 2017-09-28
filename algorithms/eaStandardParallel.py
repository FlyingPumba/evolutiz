import pickle
import random

from deap import tools
from algorithms.parallalel_evaluation import evaluate_in_parallel

def evolve(population, toolbox, cxpb, mutpb, ngen, apk_dir, package_name,
		   stats=None, verbose=__debug__):
	logbook = tools.Logbook()
	logbook.header = ['gen', 'nevals'] + (stats.fields if stats else [])

	# Evaluate the individuals with an invalid fitness
	invalid_ind = [ind for ind in population if not ind.fitness.valid]
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

		next_population = []
		while len(next_population) < len(population):
			offspring = varOr(population, toolbox, cxpb, mutpb)
			next_population.extend(offspring)

		population = next_population

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in population if not ind.fitness.valid]
		evaluate_in_parallel(toolbox.evaluate, invalid_ind, apk_dir, package_name, gen)

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


def varOr(population, toolbox, cxpb, mutpb):

	parents = tools.selTournament(population, 2, tournsize=5)  # TODO: check if tournsize is correct

	ind1, ind2 = map(toolbox.clone, parents)

	op_choice = random.random()
	if op_choice < cxpb:  # Apply crossover
		ind1, ind2 = toolbox.mate(ind1, ind2)
		del ind1.fitness.values
		del ind2.fitness.values

	op_choice = random.random()
	if op_choice < mutpb:  # Apply mutation
		ind1 = toolbox.mutate(ind1)
		del ind1.fitness.values
		ind2 = toolbox.mutate(ind2)
		del ind2.fitness.values

	return [ind1, ind2]
