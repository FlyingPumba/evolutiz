import random
import pickle
from deap import tools

from algorithms.parallalel_evaluation import evaluate_in_parallel

def evolve(population, toolbox, mu, lambda_, cxpb, mutpb, ngen, apk_dir, package_name,
		   stats=None, halloffame=None, verbose=__debug__):
	pass

def varOr(population, toolbox, lambda_, cxpb, mutpb):
	pass