DEBUG = True # or False
# if False, "0" will be used
ENABLE_STRING_SEEDING = True
# use headless evaluator
HEADLESS = True
EMMA_ED = "emma/" # folder for instrumented builds with emma
ORIGINAL_PATH = "subjects/" # folder for origignal sources

USE_REAL_DEVICES = False
USE_EMULATORS = True

# === Emulator ===
DEVICE_NUM = 0
AVD_BOOT_DELAY = 40 # 40
AVD_SERIES = "Nexus_4_API_19"
EVAL_TIMEOUT = 120
# if run on Mac OS, use "gtimeout"
TIMEOUT_CMD = "timeout"


# === Env. Paths ===
# path should end with a '/'
ANDROID_HOME = '/opt/android-sdk/'
# the path of sapienz folder
WORKING_DIR = '/home/ivan/src/sapienz/'


# === GA parameters ===
SEQUENCE_LENGTH_MIN = 20
SEQUENCE_LENGTH_MAX = 500
SUITE_SIZE = 5 # 5
POPULATION_SIZE = 5 # 50
OFFSPRING_SIZE = 5 # 50
GENERATION = 100 # 100
# Crossover probability
CXPB = 0.7
# Mutation probability
MUTPB = 0.3
SEARCH_BUDGET_IN_SECONDS = 60 * 15 # 60 * 60 # = 1 hour


# === Only for main_multi ===
# start from the ith apk
APK_OFFSET = 0
APK_DIR = ""
REPEATED_RESULTS_DIR = ""
REPEATED_RUNS = 20


# === MOTIFCORE script ===
# for initial population
MOTIFCORE_SCRIPT_PATH = '/mnt/sdcard/motifcore.script'
# header for evolved scripts
MOTIFCORE_SCRIPT_HEADER = 'type= raw events\ncount= -1\nspeed= 1.0\nstart data >>\n'
