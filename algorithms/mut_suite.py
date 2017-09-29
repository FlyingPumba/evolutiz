import random

from deap import tools

import settings


def mut_suite(individual, indpb):
	# shuffle seq
	individual, = tools.mutShuffleIndexes(individual, indpb)

	# crossover inside the suite
	for i in range(1, len(individual), 2):
		if random.random() < settings.MUTPB:
			if len(individual[i - 1]) <= 2:
				print "\n\n### Indi Length =", len(individual[i - 1]), " ith = ", i - 1, individual[i - 1]
				continue  # sys.exit(1)
			if len(individual[i]) <= 2:
				print "\n\n### Indi Length =", len(individual[i]), "ith = ", i, individual[i]
				continue  # sys.exit(1)

			individual[i - 1], individual[i] = tools.cxOnePoint(individual[i - 1], individual[i])

	# shuffle events
	for i in range(len(individual)):
		if random.random() < settings.MUTPB:
			if len(individual[i]) <= 2:
				print "\n\n### Indi Length =", len(individual[i]), "ith = ", i, individual[i]
				continue  # sys.exit(1)
			individual[i], = tools.mutShuffleIndexes(individual[i], indpb)

	return individual,