import pickle
from deap import tools, creator, base

import logger
import settings
from algorithms.eval_suite_multi_objective import eval_suite
from algorithms.mut_suite import mut_suite
from algorithms.parallalel_evaluation import evaluate_in_parallel

class randomParallel:

	def __init__(self):
		self.cxpb = settings.CXPB
		self.mutpb = settings.MUTPB
		self.ngen = settings.GENERATION
		self.mu = settings.POPULATION_SIZE
		self.lambda_ = settings.OFFSPRING_SIZE

		self.best_historic_crashes_measure = 0
		self.best_historic_length_measure = 0
		self.best_historic_coverage_measure = 0
		self.best_historic_crashes_individual = 0
		self.best_historic_length_individual = 0
		self.best_historic_coverage_individual = 0

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
		self.population = self.toolbox.population(n=settings.DEVICE_NUM, apk_dir=self.apk_dir,
												  package_name=self.package_name)
		if (len(self.population) < settings.DEVICE_NUM):
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
			new_population = self.toolbox.population(n=settings.DEVICE_NUM, apk_dir=self.apk_dir,
													  package_name=self.package_name)

			# Evaluate the individuals with an invalid fitness
			invalid_ind = [ind for ind in new_population if not ind.fitness.valid]

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
			for i in range(len(new_population) - 1, -1, -1):
				if not new_population[i].fitness.valid:
					print "### Warning: Invalid Fitness"
					del new_population[i]

			self.update_best_historic_objectives_achieved(new_population)

			# Update the statistics with the new population
			record = self.stats.compile(new_population) if self.stats is not None else {}
			logbook.record(gen=gen, nevals=len(invalid_ind), **record)

			# dump logbook in case we are interrupted
			logbook_file = open(self.apk_dir + "/intermediate/logbook.pickle", 'wb')
			pickle.dump(logbook, logbook_file)
			logbook_file.close()

		best_population = self.best_historic_coverage_individual, self.best_historic_crashes_individual, \
						  self.best_historic_length_individual

		return best_population, logbook

	def update_best_historic_objectives_achieved(self, population):
		for ind in population:
			fit = ind.fitness.values
			crashes = fit[0]
			coverage = fit[1]
			length = fit[2]

			if crashes > self.best_historic_crashes_measure:
				self.best_historic_crashes_measure = crashes
				self.best_historic_crashes_individual = ind

			if coverage > self.best_historic_coverage_measure:
				self.best_historic_coverage_measure = coverage
				self.best_historic_coverage_individual = ind

			if crashes > 0 and length < self.best_historic_length_measure:
				self.best_historic_length_measure = length
				self.best_historic_length_individual = ind

		logger.log_progress("\n- Best historic crashes: " + str(self.best_historic_crashes_measure))
		logger.log_progress("\n- Best historic coverage: " + str(self.best_historic_coverage_measure))
		logger.log_progress("\n- Best historic length: " + str(self.best_historic_length_measure))

