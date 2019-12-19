import argparse
import os
import pickle

import matplotlib.pyplot as plt
import networkx
from deap import creator, base

from generation.fitness_cov_len_crash import FitnessCovLenCrash
from generation.individual_multi_objective import IndividualMultiObjective

creator.create(FitnessCovLenCrash.get_name(), base.Fitness, weights=(10.0, -0.5, 1000.0))
creator.create(IndividualMultiObjective.get_name(), list, fitness=getattr(creator, FitnessCovLenCrash.get_name()))

def draw_genealogy(history_file_path):
    coverages = []
    generations = []
    colors = []  # color stands for the ith gen

    history_file = open(history_file_path)
    history = pickle.load(history_file)

    fig, ax = plt.subplots()

    graph = networkx.DiGraph(history.genealogy_tree)
    graph = graph.reverse()  # Make the grah top-down
    colors = [history.genealogy_history[i].fitness.values[0] for i in graph]
    networkx.draw(graph, node_color=colors)

    fig.savefig(os.path.dirname(history_file_path) + "/genealogy_tree.png")
    plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('history_file_path', help='History pickle file path.')
    args = parser.parse_args()

    draw_genealogy(args.history_file_path)
