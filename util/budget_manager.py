import time
from datetime import datetime

import settings


class BudgetManager(object):

    def __init__(self):
        self.start_time = None
        self.time_limit = settings.SEARCH_BUDGET_IN_SECONDS

        self.evaluations = 0
        self.evaluations_limit = None

    def start_time_budget(self):
        print "Start time is " + datetime.today().strftime("%Y-%m-%d_%H-%M")
        self.start_time = time.time()

    def time_budget_available(self):
        if self.time_limit is not None:
            return time.time() - self.start_time < self.time_limit
        else:
            return True

    def reset_evaluations_budget(self):
        self.evaluations = 0

    def evaluations_budget_available(self):
        if self.evaluations_limit is not None:
            return self.evaluations < self.evaluations_limit
        else:
            return True
