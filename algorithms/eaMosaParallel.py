import random
import pickle
from deap import tools, base, creator

import settings
from algorithms.eval_suite_multi_objective import eval_suite
from algorithms.mut_suite import mut_suite

class eaMosaParallel:

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
		pass

	def varOr(self, population):
		pass