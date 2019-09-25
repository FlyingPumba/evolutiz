import sys

# if False, "0" will be used
ENABLE_STRING_SEEDING: bool = True
ANDROID_TARGET: str = "android-10"  # TODO: this should depend on a configuration file in the subject itself

TEST_CASE_EVAL_TIMEOUT: int = 600
ADB_REGULAR_COMMAND_TIMEOUT: int = 30

# === Emulator ===
AVD_BOOT_DELAY: int = 40  # 40
# if run on Mac OS, use "gtimeout"
TIMEOUT_CMD: str = "timeout --kill-after=5"

# === Env. Paths ===
# path should end with a '/'
ANDROID_HOME: str = '/usr/local/android-sdk/'
# the path of evolutiz folder
WORKING_DIR: str = '/home/ivan/src/evolutiz/'

# === GA parameters ===
SEQUENCE_LENGTH_MIN: int = 20
SEQUENCE_LENGTH_MAX: int = 500
SUITE_SIZE: int = 2  # 5
POPULATION_SIZE: int = 5  # 50
OFFSPRING_SIZE: int = 5  # 50
ELITISM_SIZE: int = 1 # By default 1, the same that uses Evosuite
GENERATION: int = sys.maxsize
# Crossover probability
CXPB: float = 0.7
# Mutation probability
MUTPB: float = 0.3

# header for evolved scripts
SCRIPT_HEADER: str = 'type= raw events\ncount= -1\nspeed= 1.0\nstart data >>\n'
