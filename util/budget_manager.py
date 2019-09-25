import time
from datetime import datetime
from threading import Lock
from typing import Optional, Union


class BudgetManager(object):

    def __init__(
            self,
            time_budget: Optional[Union[str, int]] = None,
            evaluations_budget: Optional[int] = None
    ) -> None:

        self.time_limit: int
        if time_budget is not None:
            if isinstance(time_budget, int):
                self.time_limit = time_budget
            elif isinstance(time_budget, str):
                if time_budget.endswith('h'):  # format is 50h ?
                    self.time_limit = int(time_budget[:-1]) * 60 * 60
                if time_budget.endswith('m'):  # format is 50m ?
                    self.time_limit = int(time_budget[:-1]) * 60
                if time_budget.endswith('s'):  # format is 50s ?
                    self.time_limit = int(time_budget)
            else:
                # unknown format
                raise ValueError(f'Invalid time budget "{time_budget}".')

        self.start_time: float = 0
        self.time_limit_increase: float = 0

        self.evaluations = 0
        self.evaluations_limit = evaluations_budget
        self.evaluations_lock = Lock()

    def start_budget(self) -> None:
        print(f"Start time is {datetime.today().strftime('%Y-%m-%d_%H-%M')}")
        self.start_time = time.time()
        self.time_limit_increase = 0
        self.evaluations = 0

    def is_budget_available(self) -> bool:
        return self.time_budget_available() and self.evaluations_budget_available()

    def increase_evaluations_used(self):
        self.evaluations_lock.acquire()
        self.evaluations += 1
        self.evaluations_lock.release()

    def increase_time_budget(self, seconds: float) -> None:
        self.time_limit_increase += seconds

    def get_time_budget_used(self) -> Optional[float]:
        if self.time_limit is not None:
            return time.time() - self.start_time
        else:
            return None

    def time_budget_available(self) -> bool:
        if self.time_limit is not None:
            return time.time() - self.start_time < self.time_limit + self.time_limit_increase
        else:
            return True

    def get_evaluations_budget_used(self) -> Optional[int]:
        if self.evaluations_limit is not None:
            self.evaluations_lock.acquire()
            aux = self.evaluations
            self.evaluations_lock.release()
            return aux
        else:
            return None

    def evaluations_budget_available(self) -> bool:
        if self.evaluations_limit is not None:
            self.evaluations_lock.acquire()
            aux = self.evaluations < self.evaluations_limit
            self.evaluations_lock.release()
            return aux
        else:
            return True
