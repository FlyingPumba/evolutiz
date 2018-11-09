import argparse
import os
import pickle

import matplotlib.pyplot as plt

# This script needs to be run like:
# python -m postprocess.logbook logbook.pickle


def print_avg_fitness(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    print("Average fitness by generation:\n")
    print("gen\tcovrg.\tcrashes")
    print("--------------------")

    for gen, (coverage, length, crashes) in enumerate(logbook.select("avg")):
        print("%d\t%d\t%d" % (gen, coverage, crashes))


def print_fitness(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    print("Fitness records:")
    print("gen\tid\tcovrg.\tcrashes\t\tlength")
    print("---------------------------------------------------")
    fitness_by_gen = logbook.select("fitness")
    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            print("%d\t%s\t%d\t%d\t%d" % (gen,
                       str(fitness['generation']) + "." + str(fitness['index_in_generation']),
                       fitness['coverage'],
                       fitness['crashes'],
                       fitness['length']))


    print("\nEvaluation records:")
    print("gen\tid\teval elapsed time\teval finish timestamp")
    print("-------------------------------------------------------------")
    evaluation_by_gen = logbook.select("evaluation")
    for gen, population in enumerate(evaluation_by_gen):
        for evaluation in population:
            print("%d\t%s\t\t%d\t\t%d" % (gen,
                                      str(evaluation['generation']) + "." + str(evaluation['index_in_generation']),
                                      evaluation['evaluation_elapsed_time'],
                                      evaluation['evaluation_finish_timestamp']))

    print("\nCreation records:")
    print("gen\tid\tcreation elapsed time\tcreation finish timestamp")
    print("-----------------------------------------------------------------")
    creation_by_gen = logbook.select("creation")
    for gen, population in enumerate(creation_by_gen):
        for creation in population:
            print("%d\t%s\t\t%d\t\t%d" % (gen,
                                      str(creation['generation']) + "." + str(creation['index_in_generation']),
                                      creation['creation_elapsed_time'],
                                      creation['creation_finish_timestamp']))

def draw_pop_fitness(logbook_file_path):
    coverages = []
    generations = []
    colors = []  # color stands for the ith gen

    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    fitness_by_gen = logbook.select("fitness")

    gen_size = len(fitness_by_gen)
    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            coverages.append(fitness['coverage'])
            generations.append(gen)
            colors.append(int(gen + 1))

    # print coverages, lengths, colors

    fig, ax = plt.subplots()
    ax.set_xlabel("Generations")
    ax.set_ylabel("Coverage")

    # ax.scatter(lengths, coverages, color="red", marker="^")
    im = ax.scatter(generations, coverages, c=colors, cmap=plt.cm.jet, marker=".", s=100)

    fig.colorbar(im, ax=ax, ticks=range(1, gen_size + 1))
    im.set_clim(1, gen_size)

    fig.savefig(os.path.dirname(logbook_file_path) + "/logbook_fitness_by_generation.png")
    plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('logbook_file_path', help='Logbook pickle file path.')
    args = parser.parse_args()

    # print_avg_fitness(args.logbook_file_path)
    # draw_pop_fitness(args.logbook_file_path)
    print_fitness(args.logbook_file_path)
