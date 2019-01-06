import pickle
import argparse

from deap import creator, base

# For multi-objective:
# creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
# creator.create("Individual", list, fitness=creator.FitnessCovLen)

# For single-objective:
creator.create("FitnessMax", base.Fitness, weights=(1.0,))
creator.create("Individual", list, fitness=creator.FitnessMax)

def show_hof(hof_file_path):
    hof_file = open(hof_file_path, 'rb')
    hof = pickle.load(hof_file)
    print("Hall of fame contains: " + str(len(hof)) + " individuals")
    print("Stored fitness are: ")

    for individual in hof:
        coverage, length, crashes = individual.fitness.values
        if crashes == 0:
            length = "--"
        else:
            length = str(length)
        print("(%d, %s, %d)" % (coverage, length, crashes))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('hof_file_path', help='Hall of fame pickle file path.')
    args = parser.parse_args()

    show_hof(args.hof_file_path)
