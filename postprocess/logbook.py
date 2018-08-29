import argparse
import os
import pickle

import matplotlib.pyplot as plt


def print_avg_fitness(logbook_file_path):
    logbook_file = open(logbook_file_path)
    logbook = pickle.load(logbook_file)

    print "Average fitness by generation:\n"
    print "gen\tcovrg.\tcrashes"
    print "--------------------"

    for gen, (coverage, length, crashes) in enumerate(logbook.select("avg")):
        print "%d\t%d\t%d" % (gen, coverage, crashes)


def draw_pop_fitness(logbook_file_path):
    coverages = []
    generations = []
    colors = []  # color stands for the ith gen

    logbook_file = open(logbook_file_path)
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

    print_avg_fitness(args.logbook_file_path)
    draw_pop_fitness(args.logbook_file_path)
