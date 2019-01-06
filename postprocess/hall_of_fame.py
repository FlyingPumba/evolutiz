import pickle
import argparse

from deap import creator, base


def load_hof(hof_file):
    try:
        # Try multi-objective setup first:
        creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
        creator.create("Individual", list, fitness=creator.FitnessCovLen)
        hof = pickle.load(hof_file)
        return hof
    except Exception as e1:
        try:
            # An error occurred, perhaps this is a single-objective HoF:
            del creator.Individual
            creator.create("FitnessMax", base.Fitness, weights=(1.0,))
            creator.create("Individual", list, fitness=creator.FitnessMax)
            hof = pickle.load(hof_file)
            return hof
        except Exception as e2:
            # Unrelated problem
            raise Exception(str(e1) + str(e2))


def print_best_fitness(hof_file_path):
    hof_file = open(hof_file_path, 'rb')
    hof = load_hof(hof_file)

    max_coverage = 0
    min_length = None
    max_crashes = None

    for individual in hof:
        fitness = individual.fitness.values

        max_coverage = max(fitness[0], max_coverage)

        if len(fitness) > 1:
            # multi-objective hof
            min_length = min(fitness[1], min_length)
            max_crashes = max(fitness[2], max_crashes)

    if max_crashes is None:
        max_crashes = ""
        min_length = ""

    print("coverage,crashes,length")
    print("{0},{1},{2}".format(max_coverage, max_crashes, min_length))

def print_fitness_by_time(hof_file_path):
    hof_file = open(hof_file_path, 'rb')
    hof = load_hof(hof_file)

    evaluations = []
    for individual in hof:
        fitness = individual.fitness.values
        coverage = str(fitness[0])
        length = ""
        crashes = ""

        if len(fitness) > 1:
            # multi-objective hof
            length = str(fitness[1])
            crashes = str(fitness[2])

        evaluations.append({
            'timestamp': str(individual.evaluation_finish_timestamp),
            'coverage': coverage,
            'crashes': crashes,
            'length': length
        })

    # sort information by timestamp
    sorted_values = sorted(evaluations, key=lambda k: k['timestamp'])

    print("timestamp,coverage,crashes,length")
    print("\n".join(map(lambda x: "{0},{1},{2},{3}".format(
        x['timestamp'],
        x['coverage'],
        x['crashes'],
        x['length']), sorted_values)))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('hof_file_path', help='Hall of fame pickle file path.')
    parser.add_argument('processing', default='print-all', nargs='?', help='Processing to do')
    args = parser.parse_args()

    if args.processing == 'print-best':
        print_best_fitness(args.hof_file_path)
    elif args.processing == 'fitness-by-time':
        print_fitness_by_time(args.hof_file_path)
    else:
        print(args.processing)
