import argparse
import os
import pickle
import sys

import matplotlib.pyplot as plt
import numpy


# This script needs to be run like:
# python -m postprocess.logbook logbook.pickle

def _print_best_metric(logbook_file_path, metric_name, default_metric_value, aggregation_func):
    """
    Calculate the "best" historic value for a metric (e.g., 'coverage') in the fitness values of the provided logbook.
    "Best" is determined by the aggregation function (e.g., max).
    A default value for the metric is needed in case the metric is not found in the fitness values.
    :param logbook_file_path:
    :param metric_name:
    :param default_metric_value:
    :param aggregation_func:
    :return:
    """
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)
    fitness_by_gen = logbook.select("fitness")

    # collect all values for this metric
    metric_values = [default_metric_value]
    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            metric_value = fitness[metric_name]
            metric_values.append(metric_value)

    best_metric_value = aggregation_func(metric_values)
    print("{0}".format(best_metric_value))

def _find_best_individual(fitness_by_gen, at_least_as_good_func, partially_better_func):
    """
    Find the best individual across generations.
    To determine if an individual dominates the current best one, it has to be at least as good and partially better.
    Functions to calculate those facts are provided by arguments (since they are evaluation-dependent).
    :param fitness_by_gen:
    :param at_least_as_good_func:
    :param partially_better_func:
    :return:
    """
    best_fitness = None
    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            if best_fitness is None:
                best_fitness = fitness
            elif at_least_as_good_func(fitness, best_fitness) and \
                    partially_better_func(fitness, best_fitness):
                best_fitness = fitness

    return best_fitness

def _is_single_objective_logbook(logbook_file_path):
    """
    Determine if this logbook was generated using a single-objective evaluation or not.
    Since the logbooks we are using have changed in the past, this process is not so straightforward.

    To begin, we assume that the logbook comes from a multi-objective evaluation.
    If the fitness values do not have information regarding crashes or length at all (i.e., not part of the records),
    then we can confirm that it IS a single-objective logbook.
    If the fitness values have the "evaluation" property, we use it to determine the output.
    :param logbook_file_path:
    :return:
    """
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)
    fitness_by_gen = logbook.select("fitness")

    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            if 'crashes' not in fitness:
                # definitely single-objective
                return True

            if 'evaluation' in fitness:
                # the fitness values is declaring which evaluation was used on it.
                return fitness['evaluation'] == 'single-objective'

    # we did not find evidence of single-objective logbook, assume multi-objective
    return False

def _at_least_as_good_multi_objective(current_fitness, best_fitness_so_far):
    return current_fitness['coverage'] >= best_fitness_so_far['coverage'] \
           and current_fitness['length'] <= best_fitness_so_far['length'] \
           and current_fitness['crashes'] >= best_fitness_so_far['crashes']

def _partially_better_multi_objective(current_fitness, best_fitness_so_far):
    return current_fitness['coverage'] > best_fitness_so_far['coverage'] \
           or current_fitness['length'] < best_fitness_so_far['length'] \
           or current_fitness['crashes'] > best_fitness_so_far['crashes']

def _at_least_as_good_single_objective(current_fitness, best_fitness_so_far):
    return current_fitness['coverage'] >= best_fitness_so_far['coverage']

def _partially_better_single_objective(current_fitness, best_fitness_so_far):
    return current_fitness['coverage'] > best_fitness_so_far['coverage']

def _print_individual(fitness):
    """
    Prints whatever is possible to find on the fitness values of an individual.
    :param fitness:
    :return:
    """
    coverage = ""
    length = ""
    crashes = ""

    if fitness is not None:
        if 'coverage' in fitness:
            coverage = fitness['coverage']
        if 'length' in fitness:
            length = fitness['length']
        if 'crashes' in fitness:
            crashes = fitness['crashes']

    print("coverage,crashes,length")
    print("{0},{1},{2}".format(coverage, crashes, length))

def print_number_of_generations(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)

    fitness_by_gen = logbook.select("fitness")
    print("generations")
    print("{0}".format(len(fitness_by_gen)))

def print_best_historic_coverage(logbook_file_path):
    _print_best_metric(logbook_file_path, 'coverage', 0, max)

def print_best_historic_length(logbook_file_path):
    _print_best_metric(logbook_file_path, 'length', sys.maxsize, min)

def print_best_historic_crashes(logbook_file_path):
    _print_best_metric(logbook_file_path, 'crashes', 0, max)

def print_best_individual_all_generations(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)
    fitness_by_gen = logbook.select("fitness")

    if _is_single_objective_logbook(logbook_file_path):
        individual = _find_best_individual(
            fitness_by_gen,
            _at_least_as_good_single_objective,
            _partially_better_single_objective)
        _print_individual(individual)
    else:
        individual = _find_best_individual(
            fitness_by_gen,
            _at_least_as_good_multi_objective,
            _partially_better_multi_objective)
        _print_individual(individual)

def print_best_individual_last_generation(logbook_file_path):
    logbook_file = open(logbook_file_path, 'rb')
    logbook = pickle.load(logbook_file)
    fitness_by_gen = logbook.select("fitness")
    fitness_last_gen = fitness_by_gen[-1]

    if _is_single_objective_logbook(logbook_file_path):
        individual = _find_best_individual(
            [fitness_last_gen],
            _at_least_as_good_single_objective,
            _partially_better_single_objective)
        _print_individual(individual)
    else:
        individual = _find_best_individual(
            [fitness_last_gen],
            _at_least_as_good_multi_objective,
            _partially_better_multi_objective)
        _print_individual(individual)

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
                print("%d\t%s\t%d\t%d\t%d" % (gen,
                                              str(fitness['generation']) + "." + str(fitness['index_in_generation']),
                                              fitness['coverage'],
                                              fitness['crashes'],
                                              fitness['length']))
            else:
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
            original_generation = str(fitness['generation'])
            id = original_generation + "." + str(fitness['index_in_generation'])

            coverage = str(fitness['coverage'])
            crashes = ""
            length = ""

            if 'crashes' in fitness:
                # multi-objective logbook
                crashes = str(fitness['crashes'])
                length = str(fitness['length'])

            evaluations[id] = {
                'generation': original_generation,
                'coverage': coverage,
                'crashes': crashes,
                'length': length
            }

    # gather fitness evaluation info
    evaluation_by_gen = logbook.select("evaluation")
    for gen, population in enumerate(evaluation_by_gen):
        for evaluation in population:
            original_generation = str(evaluation['generation'])
            id = original_generation + "." + str(evaluation['index_in_generation'])
            evaluations[id]['evaluation.timestamp'] = evaluation['evaluation_finish_timestamp']
            evaluations[id]['evaluation.elapsed'] = evaluation['evaluation_elapsed_time']

    # gather individual creation info
    creation_by_gen = logbook.select("creation")
    for gen, population in enumerate(creation_by_gen):
        for creation in population:
            original_generation = str(creation['generation'])
            id = original_generation + "." + str(creation['index_in_generation'])
            evaluations[id]['creation.timestamp'] = creation['creation_finish_timestamp']
            evaluations[id]['creation.elapsed'] = creation['creation_elapsed_time']

            # evaluations[id]['timestamp'] = evaluations[id]['timestamp'] - creation['creation_elapsed_time']

    # sort information by timestamp
    values = evaluations.values()
    sorted_values = sorted(values, key=lambda k: k['evaluation.timestamp'])
    sorted_values = sorted(sorted_values, key=lambda k: k['generation'])

    print("generation,"
          "evaluation.timestamp,evaluation.elapsed,"
          "creation.timestamp,creation.elapsed,"
          "coverage,crashes,length")
    print("\n".join(map(lambda x: "{0},{1},{2},{3},{4},{5},{6},{7}".format(
        x['generation'],
        x['evaluation.timestamp'],
        x['evaluation.elapsed'],
        x['creation.timestamp'],
        x['creation.elapsed'],
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

    if args.processing == 'all':
        print_all(args.logbook_file_path)
    elif args.processing == 'draw-fitness':
        draw_pop_fitness(args.logbook_file_path)
    elif args.processing == 'fitness-by-time':
        print_fitness_by_time(args.logbook_file_path)
    elif args.processing == 'generations':
        print_number_of_generations(args.logbook_file_path)
    elif args.processing == 'best-historic-coverage':
        print_best_historic_coverage(args.logbook_file_path)
    elif args.processing == 'best-historic-length':
        print_best_historic_length(args.logbook_file_path)
    elif args.processing == 'best-historic-crashes':
        print_best_historic_crashes(args.logbook_file_path)
    elif args.processing == 'best-individual-all-generations':
        print_best_individual_all_generations(args.logbook_file_path)
    elif args.processing == 'best-individual-last-generation':
        print_best_individual_last_generation(args.logbook_file_path)
    else:
        print(args.processing)
