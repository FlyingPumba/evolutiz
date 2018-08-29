import argparse
import pickle
import sys

def calculate_effiency(logbook_file_path, delta_time):
    print "Using Delta time: " + str(delta_time) + "s"

    coverage_data, crashes_data, length_data, timestamp_data = gather_data(logbook_file_path)

    coverage_accumulated_data, crashes_accumulated_data, length_accumulated_data = calculate_accumulated_data(
        coverage_data, crashes_data, delta_time, length_data, timestamp_data)

    coverage_auc, crashes_auc = calculate_auc(coverage_accumulated_data, crashes_accumulated_data, delta_time,
                                              length_accumulated_data)

    print "Coverage AUC: " + str(coverage_auc)
    print "Crashes AUC: " + str(crashes_auc)
    # print "Length AUC: " + str(length_auc)


def calculate_auc(coverage_accumulated_data, crashes_accumulated_data, delta_time, length_accumulated_data):
    total_time = len(coverage_accumulated_data) * delta_time
    coverage_auc = 0
    crashes_auc = 0
    length_auc = 0

    index = 0
    while index < len(coverage_accumulated_data) - 1:
        coverage_auc += (coverage_accumulated_data[index] + coverage_accumulated_data[index + 1])
        crashes_auc += (crashes_accumulated_data[index] + crashes_accumulated_data[index + 1])
        length_auc += (length_accumulated_data[index] + length_accumulated_data[index + 1])

        index += 1

    coverage_auc = (coverage_auc * delta_time) / (2 * total_time)
    crashes_auc = (crashes_auc * delta_time) / (2 * total_time)
    length_auc = (length_auc * delta_time) / (2 * total_time)

    return coverage_auc, crashes_auc


def calculate_accumulated_data(coverage_data, crashes_data, delta_time, length_data, timestamp_data):
    coverage_accumulated_data = []
    crashes_accumulated_data = []
    length_accumulated_data = []

    coverage_best = -1
    crashes_best = -1
    length_best = sys.maxint

    start_time = timestamp_data[0]
    end_time = timestamp_data[-1]

    delta_time_slot_start = start_time
    delta_time_slot_end = delta_time_slot_start + delta_time

    index = 0
    while index < len(timestamp_data):
        next_timestamp = timestamp_data[index]

        if next_timestamp > delta_time_slot_end:
            #  init new delta time
            delta_time_slot_start = delta_time_slot_end
            delta_time_slot_end = delta_time_slot_start + delta_time

            # save best values that occurred in elapsed delta time
            coverage_accumulated_data.append(coverage_best)
            crashes_accumulated_data.append(crashes_best)
            length_accumulated_data.append(length_best)
            # reset values
            # coverage_best = -1
            # crashes_best = -1
            # length_best = sys.maxint

        # check if we increased a value
        if coverage_data[index] > coverage_best or coverage_best == -1:
            coverage_best = coverage_data[index]

        if crashes_data[index] > crashes_best or crashes_best == -1:
            crashes_best = crashes_data[index]

        if length_data[index] < length_best or length_best == sys.maxint:
            length_best = length_data[index]

        index += 1

    if coverage_best != -1:
        # push best values from last delta time
        coverage_accumulated_data.append(coverage_best)
        crashes_accumulated_data.append(crashes_best)
        length_accumulated_data.append(length_best)

    return coverage_accumulated_data, crashes_accumulated_data, length_accumulated_data

def gather_data(logbook_file_path):
    coverage_data = []
    crashes_data = []
    length_data = []
    timestamp_data = []

    logbook_file = open(logbook_file_path)
    logbook = pickle.load(logbook_file)

    fitness_by_gen = logbook.select("fitness")

    for gen, population in enumerate(fitness_by_gen):
        for fitness in population:
            timestamp_data.append(float(fitness['timestamp']))
            coverage_data.append(float(fitness['coverage']))
            crashes_data.append(float(fitness['crashes']))
            length_data.append(float(fitness['length']))

    print "Number of generations: %d" % (len(fitness_by_gen))
    print "Number of fitness evaluations: %d" % (len(coverage_data))

    # normalize coverage
    coverage_data = list(map(lambda c: c / 100, coverage_data))

    # normalize number of crashes found
    max_crashes = max(crashes_data)
    if max_crashes > 0:
        crashes_data = list(map(lambda c: c / max_crashes, crashes_data))

    return coverage_data, crashes_data, length_data, timestamp_data

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('logbook_file_path', help='Logbook pickle file path.')
    parser.add_argument('delta_time', type=int, help='Delta time to use for AUC calculations.', default=5*60) # 5 minutes
    args = parser.parse_args()

    calculate_effiency(args.logbook_file_path, args.delta_time)