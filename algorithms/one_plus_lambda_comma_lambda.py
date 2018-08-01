from deap import tools, base, creator

import settings
from evaluation import sapienz_mut_suite
from evaluation import evaluate_in_parallel


class OnePlusLambdaCommaLambda(object):

    def __init__(self):
        self.cxpb = settings.CXPB
        self.mutpb = settings.MUTPB
        self.ngen = settings.GENERATION
        self.lambda_ = settings.OFFSPRING_SIZE

    def setup(self, toolbox, apk_dir, package_name, verbose=False):
        # assumes toolbox has registered:
        # "individual" to generate individuals
        # "population" to generate population
        self.toolbox = toolbox
        self.apk_dir = apk_dir
        self.package_name = package_name
        self.verbose = verbose

        ### deap framework setup
        creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
        creator.create("Individual", list, fitness=creator.FitnessCovLen)

        self.toolbox.register("evaluate", eval_suite)
        # mate crossover two suites
        self.toolbox.register("mate", tools.cxUniform, indpb=0.5)
        # mutate should change seq order in the suite as well
        self.toolbox.register("mutate", sapienz_mut_suite, indpb=0.5)

        # self.toolbox.register("select", tools.selTournament, tournsize=5)
        self.toolbox.register("select", tools.selNSGA2)

        print "### Initialising population ...."
        self.population = self.toolbox.population(n=settings.POPULATION_SIZE, apk_dir=self.apk_dir,
                                                  package_name=self.package_name)

    def evolve(self):
        # 1 + (lambda, lambda) starts with population of only one individual
        assert len(self.population) == 1

        # Evaluate the individuals with an invalid fitness
        invalid_ind = [ind for ind in self.population if not ind.fitness.valid]
        # fitnesses = toolbox.map(toolbox.evaluate, invalid_ind)
        # for ind, fit in zip(invalid_ind, fitnesses):
        # 	ind.fitness.values = fit
        evaluate_in_parallel(self.toolbox.evaluate, invalid_ind, self.apk_dir, self.package_name, 0)

        # discard invalid population individual
        for i in range(len(self.population) - 1, -1, -1):
            if not self.population[i].fitness.valid:
                del self.population[i]

        # Begin the generational process
        for gen in range(1, self.ngen + 1):

            print "Starting generation ", gen

            # Vary the population
            offspring = self.varOr(population)

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in offspring if not ind.fitness.valid]

            # this function will eval and match each invalid_ind to its fitness
            evaluate_in_parallel(self.toolbox.evaluate, invalid_ind, self.apk_dir, self.package_name, gen)

            best_ind = tools.sortNondominated(offspring + population, 1)
            if (best_ind != population[0]):
                # the parent was improved by one individual of the offspring
                population = [best_ind]

        return population

    def varOr(self, population):
        parent = population[0]
        # generate lambda_ mutants
        mutants = []
        for _ in xrange(self.lambda_):
            ind = self.toolbox.clone(parent)
            ind, = self.toolbox.mutate(ind)
            del ind.fitness.values
            mutants.append(ind)

        evaluate_in_parallel(self.toolbox.evaluate, mutants, self.apk_dir, self.package_name, self.ngen)
        best_ind = tools.sortNondominated(mutants, 1)

        # generate lambda_ offspring
        offspring = []
        while len(offspring) < self.lambda_:
            p1 = self.toolbox.clone(parent)
            p2 = self.toolbox.clone(best_ind)
            ind1, ind2 = self.toolbox.mate(p1, p2)
            del ind1.fitness.values
            del ind2.fitness.values
            offspring.append(ind1)
            offspring.append(ind2)

        return offspring
