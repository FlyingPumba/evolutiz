import datetime
import multiprocessing as mp
import os
import time

import settings
from algorithms.genetic_algorithm import GeneticAlgorithm
from devices import adb
from util import logger
from util.pickable import pickable_function


class Standard(GeneticAlgorithm):

    def __init__(self):
        super(Standard, self).__init__()

        self.mutation_add_probability = 1 / float(3)
        self.mutation_modify_probability = 1 / float(3)
        self.mutation_delete_probability = 1 / float(3)

    def evolve(self):
        for gen in range(1, self.max_generations + 1):

            if not self.budget_manager.time_budget_available():
                print "Time budget run out, exiting evolve"
                break

            logger.log_progress("\n---> Starting generation " + str(gen))

            offspring = self.generate_offspring_in_parallel()

            # Evaluate the individuals with an invalid fitness
            invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
            individuals_evaluated = self.parallel_evaluator.evaluate(invalid_ind, gen)

            if individuals_evaluated is None:
                print "Time budget run out durring parallel evaluation, exiting evolve"
                break

            # Select the next generation population
            self.population = offspring

            self.device_manager.log_devices_battery(gen, self.result_dir)
            self.parallel_evaluator.test_suite_evaluator.update_logbook(gen)

        return self.population

    def generate_offspring_in_parallel(self):
        self.idle_devices = []
        self.idle_devices.extend(self.device_manager.get_devices())

        self.offspring_generated = []
        self.remaining_offspring_to_generate = self.offspring_size

        pool = mp.Pool(processes=len(self.idle_devices))
        while len(self.offspring_generated) < self.offspring_size:

            if self.remaining_offspring_to_generate > 0 and len(self.idle_devices) > 0:
                device = self.idle_devices.pop(0)
                self.remaining_offspring_to_generate -= 1

                pool.apply_async(pickable_function,
                                 args=(self, 'generate_two_offspring',
                                       (self, device,)),
                                 callback=self.two_offspring_generated)
            else:
                time.sleep(2)

        pool.close()  # prevent any more tasks from being submitted to the pool
        pool.terminate()  # stops the worker processes immediately without completing outstanding work
        pool.join()  # wait for the worker processes to exit

        return self.offspring_generated

    def two_offspring_generated(self, result):
        o1, o2, device = result
        self.offspring_generated.append(o1)
        self.offspring_generated.append(o2)

    def generate_two_offspring(self, device):
        p1, p2 = self.toolbox.select(self.population, 2)
        o1, o2 = self.generate_offspring(device, p1, p2)
        return o1, o2, device

    def generate_offspring(self, device, p1, p2):
        ts = datetime.datetime.now().strftime("%Y_%m_%d_%H_%M_%S")

        parentFilename1 = "evolutiz.parent.1." + ts
        parentFilename2 = "evolutiz.parent.2." + ts
        offspringFilename1 = "evolutiz.offspring.1." + ts
        offspringFilename2 = "evolutiz.offspring.2." + ts

        parentLocalPath1 = self.toolbox.get_result_dir() + "/intermediate/" + parentFilename1
        parentLocalPath2 = self.toolbox.get_result_dir() + "/intermediate/" + parentFilename2
        offspringLocalPath1 = self.toolbox.get_result_dir() + "/intermediate/" + offspringFilename1
        offspringLocalPath2 = self.toolbox.get_result_dir() + "/intermediate/" + offspringFilename2

        parentRemotePath1 = "/mnt/sdcard/" + parentFilename1
        parentRemotePath2 = "/mnt/sdcard/" + parentFilename2
        offspringRemotePath1 = "/mnt/sdcard/" + offspringFilename1
        offspringRemotePath2 = "/mnt/sdcard/" + offspringFilename2

        # write parent tests to local files
        self.write_test_case_to_file(p1, parentLocalPath1)
        self.write_test_case_to_file(p2, parentLocalPath2)

        # push parent scripts
        result_code = adb.push(device, parentLocalPath1, parentRemotePath1,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to push motifcore script " + parentLocalPath1 + " to device: " + device.name)

        result_code = adb.push(device, parentLocalPath2, parentRemotePath2,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to push motifcore script " + parentLocalPath2 + " to device: " + device.name)

        # generate offspring
        self.parallel_evaluator.test_suite_evaluator.test_runner.generate_ga_offspring(device, self.package_name,
                                                                                       parentFilename1, parentFilename2,
                                                                                       offspringFilename1,
                                                                                       offspringFilename2)

        # fetch offspring remote files
        result_code = adb.pull(device, offspringRemotePath1, offspringLocalPath1,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to pull motifcore script " + offspringRemotePath1 + " to device: " + device.name)

        result_code = adb.pull(device, offspringRemotePath2, offspringLocalPath2,
                               timeout=settings.ADB_REGULAR_COMMAND_TIMEOUT)
        if result_code != 0:
            device.flag_as_malfunctioning()
            raise Exception(
                "Unable to pull motifcore script " + offspringLocalPath2 + " to device: " + device.name)

        o1 = self.get_test_case_from_file(offspringLocalPath1)
        o2 = self.get_test_case_from_file(offspringLocalPath2)

        return o1, o2

    def write_test_case_to_file(self, content, filename):
        # check that directory exists before creating file
        dirname = os.path.dirname(filename)
        if not os.path.exists(dirname):
            os.makedirs(dirname)
        with open(filename, "w") as script:
            script.write(settings.SCRIPT_HEADER)
            for line in content:
                script.write(line + "\n")

    def get_test_case_from_file(self, filename):
        test_content = []

        script = open(filename)
        is_content = False
        is_skipped_first = False
        for line in script:
            line = line.strip()
            if line.find("start data >>") != -1:
                is_content = True
                continue
            if is_content and line != "":
                if not is_skipped_first:
                    is_skipped_first = True
                    continue
                if is_skipped_first:
                    test_content.append(line)

        script.close()
        return test_content
