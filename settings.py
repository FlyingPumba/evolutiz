DEBUG = False
# if False, "0" will be used
ENABLE_STRING_SEEDING = True
# use headless evaluator
HEADLESS = True


# === Emulator ===
DEVICE_NUM = 1
AVD_BOOT_DELAY = 60
AVD_SERIES = "Nexus_4_API_19_"
EVAL_TIMEOUT = 150
# if run on Mac OS, use "gtimeout"
TIMEOUT_CMD = "timeout"


# === Env. Paths ===
# path should end with a '/'
ANDROID_HOME = '/usr/local/android-sdk/'
# the path of sapienz folder
WORKING_DIR = '/home/ivan/src/sapienz/'


# === GA parameters ===
SEQUENCE_LENGTH_MIN = 20
SEQUENCE_LENGTH_MAX = 500
SUITE_SIZE = 5
POPULATION_SIZE = 50
OFFSPRING_SIZE = 50
GENERATION = 100
# Crossover probability
CXPB = 0.7
# Mutation probability
MUTPB = 0.3


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
