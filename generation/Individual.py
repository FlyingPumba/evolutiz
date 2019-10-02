class Individual(list):

    def __init__(self):
        self.creation_finish_timestamp = 0
        self.creation_elapsed_time = 0
        self.evaluation_finish_timestamp = 0
        self.evaluation_elapsed_time = 0

        self.generation = 0
        self.index_in_generation = 0

        self.history_index = 0

        self.fitness = None

    @staticmethod
    def get_name() -> str:
        return Individual.__name__
