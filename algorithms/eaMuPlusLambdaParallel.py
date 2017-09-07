import random
import time
import pickle
import multiprocessing as mp

from deap import tools

from devices import emulator
import settings


# global results for mp callback
results = []
idle_devices = []

# 0. prepare wrapper for eval function
def eval_suite_parallel_wrapper(eval_suite_parallel, individual, device, apk_dir, package_name, gen, pop):
	try:
		print "starting eval_suite_parallel_wrapper for individual ", pop
		return eval_suite_parallel(individual, device, apk_dir, package_name, gen, pop)
	except Exception as e:
		print "There was an error evaluating individual in parallel"
		print e
		return pop, (0, 0, 0), device

def process_results(data):
	indi_index, fitness, device = data
	print "Finished evaluating an individual: ", indi_index, " ", fitness, " ", device
	results.append((indi_index, fitness))
	idle_devices.append(device)
	print "Idle devices: ", idle_devices


def evaluate_in_parallel(eval_suite_parallel, individuals, apk_dir, package_name, gen):
	""" Evaluate the individuals fitnesses and assign them to each individual
	:param eval_fitness: The fitness evaluation fucntion
	:param individuals: The individuals under evaluation
	:param pool_size:
	:return: When all individuals have been evaluated
	"""
	
	if settings.DEBUG:
		print "### Starting evaluation in parallel"
		print "idle devices=", idle_devices

	# init global states
	while len(results) > 0:
		results.pop()
	while len(idle_devices) > 0:
		idle_devices.pop()

	# 1. get idle devices
	idle_devices.extend(emulator.get_devices())

	if settings.DEBUG:
		print "idle devices after extending from emulator.get_devices()=", idle_devices
		print "number of devices", len(idle_devices)

	# 2. aissign tasks to devices
	pool = mp.Pool(processes=len(idle_devices))
	for i in range(0, len(individuals)):
		while len(idle_devices) == 0:
			print "Waiting for idle_devices"
			print idle_devices
			time.sleep(1)
		
		device = idle_devices.pop(0)
		if settings.DEBUG:
			print "assigning task to device ", device
			print "individual is ", i

		pool.apply_async(eval_suite_parallel_wrapper, args=(eval_suite_parallel, individuals[i], device, apk_dir, package_name, gen, i), callback=process_results)

		# apply_result = pool.apply_async(eval_suite_parallel_wrapper, args=(eval_suite_parallel, individuals[i], device, apk_dir, package_name, gen, i))
		# process_results(apply_result.get())

	print "### evaluate_in_parallel is wating for all processes to finish ... "
	# should wait for all processes to finish
	pool.close()
	pool.join()

	print "### ... evaluate_in_parallel finished"
	# assign results
	while len(results) > 0:
		i, fitness = results.pop(0)
		individuals[i].fitness.values = fitness


def evolve(population, toolbox, mu, lambda_, cxpb, mutpb, ngen, apk_dir, package_name,
		   stats=None, halloffame=None, verbose=__debug__):
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

	if halloffame is not None:
		halloffame.update(population)

	record = stats.compile(population) if stats is not None else {}
	logbook.record(gen=0, nevals=len(invalid_ind), **record)
	if verbose:
		print logbook.stream

	# Begin the generational process
	for gen in range(1, ngen + 1):

		print "Starting generation ", gen

		# Vary the population
		offspring = varOr(population, toolbox, lambda_, cxpb, mutpb)

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in offspring if not ind.fitness.valid]

		# this function will eval and match each invalid_ind to its fitness
		evaluate_in_parallel(toolbox.evaluate, invalid_ind, apk_dir, package_name, gen)

		# if settings.DEBUG:
		# 	for indi in invalid_ind:
		# 		print indi.fitness.values

		# discard invalid offspring individual
		for i in range(len(offspring) - 1, -1, -1):
			if not offspring[i].fitness.valid:
				print "### Warning: Invalid Fitness"
				del offspring[i]

		# Update the hall of fame with the generated individuals
		print "### Updating Hall of Fame ..."
		if halloffame is not None:
			halloffame.update(offspring)

		# assert fitness
		invalid_ind_post = [ind for ind in population + offspring if not ind.fitness.valid]
		print "### assert len(invalid_ind) == 0, len = ", len(invalid_ind_post)
		assert len(invalid_ind_post) == 0

		# Select the next generation population
		population[:] = toolbox.select(population + offspring, mu)

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


def varOr(population, toolbox, lambda_, cxpb, mutpb):
	assert (cxpb + mutpb) <= 1.0, ("The sum of the crossover and mutation "
								   "probabilities must be smaller or equal to 1.0.")

	offspring = []
	for _ in xrange(lambda_):
		op_choice = random.random()
		if op_choice < cxpb:  # Apply crossover
			ind1, ind2 = map(toolbox.clone, random.sample(population, 2))
			ind1, ind2 = toolbox.mate(ind1, ind2)
			del ind1.fitness.values
			offspring.append(ind1)
		elif op_choice < cxpb + mutpb:  # Apply mutation
			ind = toolbox.clone(random.choice(population))
			ind, = toolbox.mutate(ind)
			del ind.fitness.values
			offspring.append(ind)
		else:  # Apply reproduction
			offspring.append(random.choice(population))

	return offspring
