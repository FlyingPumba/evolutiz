import random

from deap import tools

import settings
from events.event_generator import generate_new_event


def sapienz_mut_suite(individual, indpb):
    # shuffle seq
    individual, = tools.mutShuffleIndexes(individual, indpb)

    # crossover inside the suite
    for i in range(1, len(individual), 2):
        if random.random() < settings.MUTPB:
            if len(individual[i - 1]) <= 2:
                # print "\n\n### Indi Length =", len(individual[i - 1]), " ith = ", i - 1, individual[i - 1]
                continue  # sys.exit(1)
            if len(individual[i]) <= 2:
                # print "\n\n### Indi Length =", len(individual[i]), "ith = ", i, individual[i]
                continue  # sys.exit(1)

            individual[i - 1], individual[i] = tools.cxOnePoint(individual[i - 1], individual[i])

    # shuffle events
    for i in range(len(individual)):
        if random.random() < settings.MUTPB:
            if len(individual[i]) <= 2:
                # print "\n\n### Indi Length =", len(individual[i]), "ith = ", i, individual[i]
                continue  # sys.exit(1)
            individual[i], = tools.mutShuffleIndexes(individual[i], indpb)

    return individual,


def standard_mut_suite(individual, mut_add_pb, mut_modify_pb, mut_delete_pb):
    op_choice = random.random()
    if op_choice < mut_add_pb:
        # add line to individual
        new_line = generate_new_event()
        position = random.randint(0, len(individual))
        individual.insert(position, new_line)
        pass
    elif op_choice < mut_add_pb + mut_modify_pb:
        # modify line of individual
        position = random.randint(0, len(individual))
        event = individual[position]
        event.mutate()
        pass
    else:
        # delete line to individual
        position = random.randint(0, len(individual) - 1)
        individual.pop(position)
        pass

    del individual.fitness.values

