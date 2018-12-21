import argparse
import numpy
import os
import pickle

import matplotlib.pyplot as plt


# This script needs to be run like:
# python -m postprocess.logbook logbook.pickle

def print_best_fitness(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    min_fitness_values_per_generation = numpy.array(logbook.select("min"))
    max_fitness_values_per_generation = numpy.array(logbook.select("max"))

    # CAUTION: these min and max are from different individuals
    max_fitness_values_all_generations = max_fitness_values_per_generation.max(axis=0)
    min_fitness_values_all_generations = min_fitness_values_per_generation.min(axis=0)

    max_coverage = str(max_fitness_values_all_generations[0])
    min_length = ""
    max_crashes = ""

    if len(max_fitness_values_all_generations) > 1:
        # multi-objective logbook
        min_length = str(min_fitness_values_all_generations[1])
        max_crashes = str(max_fitness_values_all_generations[2])

    print("coverage,crashes,length")
    print("{0},{1},{2}".format(max_coverage, max_crashes, min_length))


def print_avg_fitness(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    print("gen,avg_coverage,avg_crashes,avg_length")

    for gen, avg_fitness in enumerate(logbook.select("avg")):
        avg_coverage = str(avg_fitness[0])
        avg_crashes = ""
        avg_length = ""

        if len(avg_fitness) > 1:
            # multi-objective logbook
            avg_crashes = str(avg_fitness[1])
            avg_length = str(avg_fitness[2])

        print("{0},{1},{2},{3}".format(gen, avg_coverage, avg_crashes, avg_length))


def print_all(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    print("Fitness records:")
    print("gen\tid\tcovrg.\tcrashes\t\tlength")
    print("---------------------------------------------------")
    fitness_by_gen = logbook.select("fitness")
    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            if 'crashes' in fitness:
                # multi-objective logbook
                print("%d\t%s\t%d\t%d\t%d" % (gen,
                                              str(fitness['generation']) + "." + str(fitness['index_in_generation']),
                                              fitness['coverage'],
                                              fitness['crashes'],
                                              fitness['length']))
            else:
                # single-objective logbook
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

            coverage = str(fitness['coverage'])
            crashes = ""
            length = ""

            if 'crashes' in fitness:
                # multi-objective logbook
                crashes = str(fitness['crashes'])
                length = str(fitness['length'])

            evaluations[id] = {
                'coverage': coverage,
                'crashes': crashes,
                'length': length
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
    print("\n".join(map(lambda x: "{0},{1},{2},{3}".format(
        x['timestamp'] - first_timestamp,
        x['coverage'],
        x['crashes'],
        x['length']), sorted_values)))

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
    elif args.processing == 'fitness-by-time':
        print_fitness_by_time(args.logbook_file_path)
    else:
        print(args.processing)
