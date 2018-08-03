DEBUG = False # or False
# if False, "0" will be used
ENABLE_STRING_SEEDING = True
# use headless evaluator
HEADLESS = True
INSTRUMENTED_SUBJECTS_PATH = "instrumented-subjects/"  # folder for instrumented builds with emma
SUBJECTS_PATH = "subjects/"  # folder for original sources
ANDROID_TARGET = "android-10"  # TODO: this should depend on a configuration file in the subject itself

MOTIFCORE_EVAL_TIMEOUT = 120
ADB_REGULAR_COMMAND_TIMEOUT = 30

# === Emulator ===
AVD_BOOT_DELAY = 40  # 40
# if run on Mac OS, use "gtimeout"
TIMEOUT_CMD = "timeout"

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
GENERATION = 100  # 100
# Crossover probability
CXPB = 0.7
# Mutation probability
MUTPB = 0.3
SEARCH_BUDGET_IN_SECONDS = 60 * 60  # 60 * 60 # = 1 hour

# === Only for main_multi ===
# start from the ith apk
APK_OFFSET = 0
APK_DIR = ""
REPEATED_RESULTS_DIR = ""
REPETITIONS = 1

# === MOTIFCORE script ===
# for initial population
MOTIFCORE_SCRIPT_PATH = '/mnt/sdcard/motifcore.script'
# header for evolved scripts
SCRIPT_HEADER = 'type= raw events\ncount= -1\nspeed= 1.0\nstart data >>\n'
