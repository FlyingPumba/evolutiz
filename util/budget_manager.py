import time
from datetime import datetime


class BudgetManager(object):

    def __init__(self, time_budget=None, evaluations_budget=None):
        self.start_time = None
        self.time_limit = time_budget

        self.evaluations = 0
        self.evaluations_limit = evaluations_budget

    def start_time_budget(self):
        print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")
        self.start_time = time.time()

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
