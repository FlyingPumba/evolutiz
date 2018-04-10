import random
import pickle
from deap import tools, creator, base

import logger
import settings
from algorithms.eval_suite_multi_objective import eval_suite
from algorithms.mut_suite import mut_suite
from algorithms.parallalel_evaluation import evaluate_in_parallel

class eaMuPlusLambdaParallel:

	def __init__(self):
		self.cxpb = settings.CXPB
		self.mutpb = settings.MUTPB
		self.ngen = settings.GENERATION
		self.mu = settings.POPULATION_SIZE
		self.lambda_ = settings.OFFSPRING_SIZE

		self.best_historic_crashes = 0
		self.best_historic_length = 0
		self.best_historic_coverage = 0

		assert (self.cxpb + self.mutpb) <= 1.0, ("The sum of the crossover and mutation "
									   "probabilities must be smaller or equal to 1.0.")

	def setup(self, toolbox, stats = None, verbose=False):
		# assumes toolbox has registered:
		# "individual" to generate individuals
		# "population" to generate population
		self.toolbox = toolbox
		self.apk_dir = toolbox.get_apk_dir()
		self.package_name = toolbox.get_package_name()
		self.stats = stats
		self.verbose = verbose

		### deap framework setup
		creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
		creator.create("Individual", list, fitness=creator.FitnessCovLen)

		self.toolbox.register("evaluate", eval_suite)
		# mate crossover two suites
		self.toolbox.register("mate", tools.cxUniform, indpb=0.5)
		# mutate should change seq order in the suite as well
		self.toolbox.register("mutate", mut_suite, indpb=0.5)

		# self.toolbox.register("select", tools.selTournament, tournsize=5)
		self.toolbox.register("select", tools.selNSGA2)

	def initPopulation(self):
		print "### Initialising population ...."
		self.population = self.toolbox.population(n=settings.POPULATION_SIZE, apk_dir=self.apk_dir,
												  package_name=self.package_name)
		if (len(self.population) < settings.POPULATION_SIZE):
			logger.log_progress("\nFailed to initialise population with proper size, exiting setup")
			return

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
		completed_evaluation = evaluate_in_parallel(self.toolbox,
													invalid_ind,
													self.apk_dir,
													self.package_name,
													0)

		if not completed_evaluation:
			logger.log_progress("\nTime budget run out durring parallel evaluation, exiting setup")
			return

		# discard invalid population individual
		for i in range(len(self.population) - 1, -1, -1):
			if not self.population[i].fitness.valid:
				del self.population[i]

		self.update_best_historic_objectives_achieved(self.population)

	def evolve(self):
		# record first population in logbook
		logbook = tools.Logbook()
		logbook.header = ['gen', 'nevals'] + (self.stats.fields if self.stats else [])

		record = self.stats.compile(self.population) if self.stats is not None else {}
		invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
		logbook.record(gen=0, nevals=len(invalid_ind), **record)

		# Begin the generational process
		for gen in range(1, self.ngen + 1):

			if not self.toolbox.time_budget_available():
				print "Time budget run out, exiting evolve"
				break

			print "Starting generation ", gen
			logger.log_progress("\nStarting generation " + str(gen))

			# Vary the population
			offspring = self.varOr(self.population)

			# Evaluate the individuals with an invalid fitness
			invalid_ind = [ind for ind in offspring if not ind.fitness.valid]

			# this function will eval and match each invalid_ind to its fitness
			completed_evaluation = evaluate_in_parallel(self.toolbox,
														invalid_ind,
														self.apk_dir,
														self.package_name,
														gen)

			if not completed_evaluation:
				print "Time budget run out durring parallel evaluation, exiting evolve"
				break

			# discard invalid offspring individual
			for i in range(len(offspring) - 1, -1, -1):
				if not offspring[i].fitness.valid:
					print "### Warning: Invalid Fitness"
					del offspring[i]

			self.update_best_historic_objectives_achieved(offspring)

			# Select the next generation population
			self.population[:] = self.toolbox.select(self.population + offspring, self.mu)

			# Update the statistics with the new population
			record = self.stats.compile(self.population) if self.stats is not None else {}
			logbook.record(gen=gen, nevals=len(invalid_ind), **record)

			# dump logbook in case we are interrupted
			logbook_file = open(self.apk_dir + "/intermediate/logbook.pickle", 'wb')
			pickle.dump(logbook, logbook_file)
			logbook_file.close()

		return self.population, logbook


	def varOr(self, population):

		offspring = []
		for _ in xrange(self.lambda_):
			op_choice = random.random()
			if op_choice < self.cxpb:  # Apply crossover
				ind1, ind2 = map(self.toolbox.clone, random.sample(population, 2))
				ind1, ind2 = self.toolbox.mate(ind1, ind2)
				del ind1.fitness.values
				offspring.append(ind1)
			elif op_choice < self.cxpb + self.mutpb:  # Apply mutation
				ind = self.toolbox.clone(random.choice(population))
				ind, = self.toolbox.mutate(ind)
				del ind.fitness.values
				offspring.append(ind)
			else:  # Apply reproduction
				offspring.append(random.choice(population))

		return offspring

	def update_best_historic_objectives_achieved(self, population):
		for ind in population:
			fit = ind.fitness.values
			crashes = fit[0]
			coverage = fit[1]
			length = fit[2]

			if crashes > self.best_historic_crashes:
				self.best_historic_crashes = crashes
			if coverage > self.best_historic_coverage:
				self.best_historic_coverage = coverage
			if crashes > 0 and length < self.best_historic_length:
				self.best_historic_length = length

		logger.log_progress("\n- Best historic crashes: " + str(self.best_historic_crashes))
		logger.log_progress("\n- Best historic coverage: " + str(self.best_historic_coverage))
		logger.log_progress("\n- Best historic length: " + str(self.best_historic_length))

