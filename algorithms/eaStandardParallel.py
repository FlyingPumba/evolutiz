import pickle
import random
from deap import tools, base, creator

import settings
from algorithms.mut_suite import mut_suite
from algorithms.parallalel_evaluation import evaluate_in_parallel
from algorithms.eval_suite_single_objective import eval_suite

class eaStandardParallel:

	def __init__(self):
		self.cxpb = settings.CXPB
		self.mutpb = settings.MUTPB
		self.ngen = settings.GENERATION

	def setup(self, toolbox, apk_dir, package_name, verbose=False):

		# assumes toolbox has registered:
		# "individual" to generate individuals
		# "population" to generate population
		self.toolbox = toolbox
		self.apk_dir = apk_dir
		self.package_name = package_name
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

		print "### Initialising population ...."
		self.population = self.toolbox.population(n=settings.POPULATION_SIZE, apk_dir=self.apk_dir,
										package_name=self.package_name)

	def evolve(self):

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
		evaluate_in_parallel(self.toolbox.evaluate, invalid_ind, self.apk_dir, self.package_name, 0)

		# discard invalid self.population individual
		for i in range(len(self.population) - 1, -1, -1):
			if not self.population[i].fitness.valid:
				del self.population[i]

		# Begin the generational process
		for gen in range(1, self.ngen + 1):

			print "Starting generation ", gen

			next_population = []
			while len(next_population) < len(self.population):
				offspring = self.varOr(self.population)
				next_population.extend(offspring)

			self.population = next_population

			# Evaluate the individuals with an invalid fitness
			invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
			evaluate_in_parallel(self.toolbox.evaluate, invalid_ind, self.apk_dir, self.package_name, gen)

		return self.population


	def varOr(self, population, toolbox, cxpb, mutpb):

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
