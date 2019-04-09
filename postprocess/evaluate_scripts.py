import os
from deap import creator

from algorithms.strategy import Strategy
from dependency_injection.required_feature import RequiredFeature
from util import logger


class EvaluateScripts(Strategy):
    def run(self):
        generation, script_paths = self.get_script_paths_for_individuals()
        individuals = self.script_paths_to_individuals(generation, script_paths)

        success = self.parallel_evaluator.evaluate(individuals)

        if not success:
            logger.log_progress("\nTime budget run out during parallel evaluation")
            return False

        self.parallel_evaluator.test_suite_evaluator.update_logbook(0, individuals)

        return True

    def get_script_paths_for_individuals(self):
        evaluate_scripts_folder_path = RequiredFeature('evaluate_scripts_folder_path').request()
        if evaluate_scripts_folder_path is None or evaluate_scripts_folder_path.strip() == "":
            raise Exception("EvaluateScripts strategy selected but 'evaluate-scripts-folder-path'"
                            " argument not provided.")

        evaluate_scripts_folder_path = evaluate_scripts_folder_path.rstrip('/')
        logger.log_progress("\nEvaluating scripts in folder: " + evaluate_scripts_folder_path)

        current_generation = -1
        script_paths_for_individuals = {}

        for file_name in sorted(os.listdir(evaluate_scripts_folder_path)):
            if not file_name.startswith("script"):
                continue

            script_path = evaluate_scripts_folder_path + "/" + file_name

            aux = file_name.split('.')
            if len(aux) != 4:
                # to avoid double evaluation of initial scripts, the format for file names has to be "script.G.I.T",
                # where G is the generation at which the script was created,
                # I the index of individual in that generation, and
                # T is the number of test sequence for that individual.
                continue

            generation = int(aux[1])
            individual_index = int(aux[2])
            test_case_index = int(aux[3])

            if generation > current_generation:
                # we found a script for a generation greater than the one we previously had
                # clean previous individuals
                current_generation = generation
                script_paths_for_individuals = {}

            if individual_index not in script_paths_for_individuals:
                script_paths_for_individuals[individual_index] = {}

            script_paths_for_individuals[individual_index][test_case_index] = script_path

        return current_generation, script_paths_for_individuals

    def script_paths_to_individuals(self, generation, script_paths):
        test_runner = RequiredFeature('test_runner').request()
        individuals = []

        for individual_index, test_suite in script_paths.items():
            suite = []
            for test_case_index, script_path in test_suite.items():
                logger.log_progress("\n- " + script_path)
                test_case_content = test_runner.get_test_case_content_from_file(script_path)
                suite.append(test_case_content)

            individual = creator.Individual(suite)
            individual.creation_finish_timestamp = 0
            individual.creation_elapsed_time = 0
            individual.index_in_generation = individual_index
            individual.generation = generation

            individuals.append(individual)

        return individuals

