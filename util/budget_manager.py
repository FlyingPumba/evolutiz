import time

from datetime import datetime


class BudgetManager(object):

    def __init__(self, time_budget=None, evaluations_budget=None):
        self.start_time = None
        if type(time_budget) is int:
            self.time_limit = time_budget
        elif type(time_budget) is str and time_budget.endswith('h'): # format is 50h ?
            self.time_limit = int(time_budget[:-1]) * 60 * 60
        elif type(time_budget) is str and time_budget.endswith('m'): # format is 50m ?
            self.time_limit = int(time_budget[:-1]) * 60
        elif type(time_budget) is str and time_budget.endswith('s'): # format is 50s ?
            self.time_limit = int(time_budget)
        else:
            # unknown format
            raise ValueError('Invalid time budget "%s".' % time_budget)

        self.evaluations = 0
        self.evaluations_limit = evaluations_budget

    def start_time_budget(self):
        print("Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M"))
        self.start_time = time.time()

    def increase_time_budget(self, seconds):
        if self.time_limit is not None:
            self.time_limit += seconds

    def get_time_budget_used(self):
        if self.time_limit is not None:
            return time.time() - self.start_time
        else:
            return None

    def time_budget_available(self):
        if self.time_limit is not None:
            return time.time() - self.start_time < self.time_limit
        else:
            return True

    def reset_evaluations_budget(self):
        self.evaluations = 0

    def get_evaluations_budget_used(self):
        if self.evaluations_limit is not None:
            return self.evaluations
        else:
            return None

    def evaluations_budget_available(self):
        if self.evaluations_limit is not None:
            return self.evaluations < self.evaluations_limit
        else:
            return True
