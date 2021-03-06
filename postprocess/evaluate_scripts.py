import os
from typing import Optional, Dict, List

from deap import creator

from algorithms.strategy import Strategy
from dependency_injection.required_feature import RequiredFeature
from generation.individual import Individual
from util import logger

ScriptsByTestCaseIndex = Dict[int, str]
ScriptsByIndividualIndex = Dict[int, ScriptsByTestCaseIndex]
ScriptsByGeneration = Dict[int, ScriptsByIndividualIndex]

class EvaluateScripts(Strategy):

    def run(self) -> bool:
        script_paths = self.fetch_script_paths()

        # transform all script_paths to individuals
        individuals_by_generation = {}
        individuals_to_evaluate: List[Individual] = []
        for generation, individuals_script_paths in sorted(script_paths.items()):
            individuals = self.script_paths_to_individuals(generation, individuals_script_paths)
            
            individuals_by_generation[generation] = individuals
            individuals_to_evaluate.extend(individuals)

        # evaluate all individuals together for performance
        success = self.parallel_evaluator.evaluate(individuals_to_evaluate)

        if not success:
            logger.log_progress("\nBudget ran out during parallel evaluation")
            return False

        # update logbook for each generation
        for generation, individuals in sorted(individuals_by_generation.items()):
            self.parallel_evaluator.test_suite_evaluator.update_logbook(generation, individuals)

        return True

    def fetch_script_paths(self) -> ScriptsByGeneration:
        """
        Analyzes "evaluate_scripts_folder_path" and clusters scripts paths by generation and individual index.
        :return:
        """
        evaluate_scripts_folder_path: Optional[str] = RequiredFeature('evaluate_scripts_folder_path').request()
        if evaluate_scripts_folder_path is None or evaluate_scripts_folder_path.strip() == "":
            raise Exception("EvaluateScripts strategy selected but 'evaluate-scripts-folder-path'"
                            " argument not provided.")

        evaluate_scripts_folder_path = evaluate_scripts_folder_path.rstrip('/')
        logger.log_progress(f"\nEvaluating scripts in folder: {evaluate_scripts_folder_path}")

        script_paths: ScriptsByGeneration = {}

        for file_name in os.listdir(evaluate_scripts_folder_path):
            if not file_name.startswith("script"):
                continue

            aux = file_name.split('.')
            if len(aux) != 4:
                # to avoid double evaluation of initial scripts, the format for file names has to be "script.G.I.T",
                # where G is the generation at which the script was created,
                # I the index of individual in that generation, and
                # T is the number of test sequence for that individual.
                continue

            script_path = f"{evaluate_scripts_folder_path}/{file_name}"

            generation = int(aux[1])
            individual_index = int(aux[2])
            test_case_index = int(aux[3])

            if generation not in script_paths:
                script_paths[generation] = {}

            if individual_index not in script_paths[generation]:
                # we have to store 5 test sequences per individual
                script_paths[generation][individual_index] = {}

            script_paths[generation][individual_index][test_case_index] = script_path

        return script_paths

    def script_paths_to_individuals(self, generation: int, script_paths: Dict[int, Dict[int, str]]) -> List[Individual]:
        test_runner = RequiredFeature('test_runner').request()
        individuals = []

        for individual_index, test_suite_script_paths in sorted(script_paths.items()):
            test_suite = []

            for test_case_index, script_path in sorted(test_suite_script_paths.items()):
                logger.log_progress(f"\n- {script_path}")
                test_case_content = test_runner.get_test_case_content_from_file(script_path)
                test_suite.append(test_case_content)

            individual: Individual = getattr(creator, Individual.get_name())(test_suite)
            individual.creation_finish_timestamp = 0
            individual.creation_elapsed_time = 0
            individual.index_in_generation = individual_index
            individual.generation = generation

            individuals.append(individual)

        return individuals

