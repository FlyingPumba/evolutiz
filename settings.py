# if False, "0" will be used
ENABLE_STRING_SEEDING = True
ANDROID_TARGET = "android-10"  # TODO: this should depend on a configuration file in the subject itself

TEST_CASE_EVAL_TIMEOUT = 600
ADB_REGULAR_COMMAND_TIMEOUT = 30

# === Emulator ===
AVD_BOOT_DELAY = 40  # 40
# if run on Mac OS, use "gtimeout"
TIMEOUT_CMD = "timeout --kill-after=5"

# === Env. Paths ===
# path should end with a '/'
ANDROID_HOME = '/usr/local/android-sdk/'
# the path of evolutiz folder
WORKING_DIR = '/home/ivan/src/evolutiz/'

# === GA parameters ===
SEQUENCE_LENGTH_MIN = 20
SEQUENCE_LENGTH_MAX = 500
SUITE_SIZE = 5  # 5
POPULATION_SIZE = 50  # 50
OFFSPRING_SIZE = 50  # 50
ELITISM_SIZE = 1 # By default 1, the same that uses Evosuite
GENERATION = 100  # 100
# Crossover probability
CXPB = 0.7
# Mutation probability
MUTPB = 0.3

# header for evolved scripts
SCRIPT_HEADER = 'type= raw events\ncount= -1\nspeed= 1.0\nstart data >>\n'
