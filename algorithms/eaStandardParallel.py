import os

import random
import pickle
import sys

from deap import tools, creator, base

import logger
import settings
from algorithms.eval_suite_single_objective import eval_suite
from algorithms.mut_suite import mut_suite
from algorithms.parallalel_evaluation import evaluate_in_parallel

class eaStandardParallel:

	def __init__(self):
		self.cxpb = settings.CXPB
		self.mutpb = settings.MUTPB
		self.ngen = settings.GENERATION
		self.mu = settings.POPULATION_SIZE
		self.lambda_ = settings.OFFSPRING_SIZE
		self.population = None

		self.best_historic_crashes = 0
		self.best_historic_length = sys.maxint
		self.best_historic_coverage = 0

		assert (self.cxpb + self.mutpb) <= 1.0, ("The sum of the crossover and mutation "
									   "probabilities must be smaller or equal to 1.0.")

	def setup(self, toolbox, stats = None, verbose=False):
		# assumes toolbox has registered:
		# "individual" to generate individuals
		# "population" to generate population
		self.toolbox = toolbox
		self.stats = stats
		self.verbose = verbose

		### deap framework setup
		creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
		creator.create("Individual", list, fitness=creator.FitnessCovLen)

		self.toolbox.register("evaluate", eval_suite)
		# mate crossover two suites
		self.toolbox.register("mate", tools.cxOnePoint, indpb=0.5)
		# mutate should change seq order in the suite as well
		self.toolbox.register("mutate", mut_suite, indpb=0.5)

		self.toolbox.register("select", tools.selTournament, tournsize=5)

		self.targets_historic_log_file = self.toolbox.get_result_dir() + "/targets-historic.log"
		self.setup_log_best_historic_objectives_achieved()

	def initPopulation(self):
		print "### Initialising population ...."
		self.population = self.toolbox.population(n=settings.POPULATION_SIZE, result_dir=self.toolbox.get_result_dir(), package_name=self.toolbox.get_package_name())
		if (len(self.population) < settings.POPULATION_SIZE):
			logger.log_progress("\nFailed to initialise population with proper size, exiting setup")
			return False

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
		completed_evaluation = evaluate_in_parallel(self.toolbox, invalid_ind, 0)

		if not completed_evaluation:
			logger.log_progress("\nTime budget run out durring parallel evaluation, exiting setup")
			return False

		# discard invalid population individual
		for i in range(len(self.population) - 1, -1, -1):
			if not self.population[i].fitness.valid:
				del self.population[i]

		self.update_best_historic_objectives_achieved(self.population, 0)

		self.toolbox.log_devices_battery(0, self.toolbox.get_result_dir())
		return True

	def evolve(self):
		# record first population in logbook
		logbook = tools.Logbook()
		logbook.header = ['gen', 'nevals'] + (self.stats.fields if self.stats else [])

		if self.population is None:
			return [], logbook

		record = self.stats.compile(self.population) if self.stats is not None else {}
		invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
		logbook.record(gen=0, nevals=len(invalid_ind), **record)

		# Begin the generational process
		for gen in range(1, self.ngen + 1):

			if not self.toolbox.time_budget_available():
				print "Time budget run out, exiting evolve"
				break

			print "Starting generation ", gen
			logger.log_progress("\n---> Starting generation " + str(gen))

			offspring = []

			while len(offspring) < self.lambda_:
				# select parents
				p1, p2 = self.toolbox.select(self.population, 2)

				# clone parents to create children
				o1, o2 = map(self.toolbox.clone, [p1, p2])

				op_choice = random.random()
				if op_choice < self.cxpb:
					# Apply crossover (in-place)
					self.toolbox.mate(o1, o2)

				# mutate each child
				self.toolbox.mutate(o1) # TODO: check mutation function (and fix if needed), I think it's not using what a standard GA would use.
				self.toolbox.mutate(o2)

				# add children to new population
				offspring.append(o1)
				offspring.append(o2)

			# Evaluate the individuals with an invalid fitness
			invalid_ind = [ind for ind in offspring if not ind.fitness.valid]

			# this function will eval and match each invalid_ind to its fitness
			completed_evaluation = evaluate_in_parallel(self.toolbox, invalid_ind, gen)

			if not completed_evaluation:
				print "Time budget run out durring parallel evaluation, exiting evolve"
				break

			self.update_best_historic_objectives_achieved(offspring, gen)

			self.toolbox.log_devices_battery(gen, self.toolbox.get_result_dir())

			# Select the next generation population
			self.population = offspring

			# Update the statistics with the new population
			record = self.stats.compile(self.population) if self.stats is not None else {}
			logbook.record(gen=gen, nevals=len(invalid_ind), **record)

			# dump logbook in case we are interrupted
			logbook_file = open(self.toolbox.get_result_dir() + "/intermediate/logbook.pickle", 'wb')
			pickle.dump(logbook, logbook_file)
			logbook_file.close()

		return self.population, logbook

	def update_best_historic_objectives_achieved(self, population, gen):
		for ind in population:
			fit = ind.fitness.values
			coverage = fit[0]
			length = fit[1]
			crashes = fit[2]

			if crashes > self.best_historic_crashes:
				self.best_historic_crashes = crashes

			if coverage > self.best_historic_coverage:
				self.best_historic_coverage = coverage

			if crashes > 0 and length < self.best_historic_length:
				self.best_historic_length = length

		logger.log_progress("\n- Best historic crashes: " + str(self.best_historic_crashes))
		logger.log_progress("\n- Best historic coverage: " + str(self.best_historic_coverage))
		if self.best_historic_crashes > 0:
			logger.log_progress("\n- Best historic length: " + str(self.best_historic_length))

		self.log_best_historic_objectives_achieved(gen)

	def setup_log_best_historic_objectives_achieved(self):
		log_file = self.targets_historic_log_file
		os.system("echo \"gen,coverage,crashes,length\" > " + log_file)

	def log_best_historic_objectives_achieved(self, gen):
		log_file = self.targets_historic_log_file
		echo_cmd = "echo \"" + \
				   str(gen) + "," + \
				   str(self.best_historic_coverage) + "," + \
				   str(self.best_historic_crashes) + ","

		if self.best_historic_crashes > 0:
			echo_cmd += str(self.best_historic_length) + " "
		else:
			echo_cmd += "-- "

		echo_cmd += "\" >> "
		os.system(echo_cmd + log_file)

