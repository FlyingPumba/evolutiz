import argparse
import numpy
import os
import pickle

import matplotlib.pyplot as plt


# This script needs to be run like:
# python -m postprocess.logbook_single_objective logbook.pickle

def print_best_fitness(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    max_fitness_values_per_generation = numpy.array(logbook.select("max"))

    max_fitness_values_all_generations = max_fitness_values_per_generation.max(axis=0)

    max_coverage = max_fitness_values_all_generations[0]

    # CAUTION: these min and max are from different individuals
    print(str(max_coverage))


def print_avg_fitness(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    print("Average fitness by generation:\n")
    print("gen\tcovrg")
    print("--------------------")

    for gen, coverage in enumerate(logbook.select("avg")):
        print("%d\t%d" % (gen, coverage))


def print_all(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    print("Fitness records:")
    print("gen\tid\tcovrg.")
    print("---------------------------------------------------")
    fitness_by_gen = logbook.select("fitness")
    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            print("%d\t%s\t%d" % (gen,
                                  str(fitness['generation']) + "." + str(fitness['index_in_generation']),
                                  fitness['coverage']))

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

def print_fitness_by_time(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    # gather each fitness by id
    fitness_by_gen = logbook.select("fitness")
    evaluations = {}
    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            id = str(fitness['generation']) + "." + str(fitness['index_in_generation'])
            evaluations[id] = {
                'coverage': fitness['coverage']
            }

    # gather timestamp of each fitness evaluation
    evaluation_by_gen = logbook.select("evaluation")
    for gen, population in enumerate(evaluation_by_gen):
        for evaluation in population:
            id = str(evaluation['generation']) + "." + str(evaluation['index_in_generation'])
            evaluations[id]['timestamp'] = evaluation['evaluation_finish_timestamp']

    # sort information by timestamp
    values = evaluations.values()
    sorted_values = sorted(values, key=lambda k: k['timestamp'])

    first_timestamp = sorted_values[0]['timestamp']
    print("timestamp,coverage,crashes,length")
    print("\n".join(map(lambda x: "{0},{1},,".format(x['timestamp'] - first_timestamp, x['coverage']), sorted_values)))


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
    parser.add_argument('processing', default='print-all', nargs='?', help='Processing to do')
    args = parser.parse_args()

    if args.processing == 'print-all':
        print_all(args.logbook_file_path)
    elif args.processing == 'draw-fitness':
        draw_pop_fitness(args.logbook_file_path)
    elif args.processing == 'print-avg':
        print_avg_fitness(args.logbook_file_path)
    elif args.processing == 'print-best':
        print_best_fitness(args.logbook_file_path)
    else:
        print(args.processing)
