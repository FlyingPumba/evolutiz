from enum import Enum


class LineCoverageStatus(Enum):
    non_covered = 0
    partially_covered = 1
    covered = 2