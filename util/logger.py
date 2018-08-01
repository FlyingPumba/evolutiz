import sys
import time
from datetime import datetime

import multiprocessing
import os

import settings

orig_stdout = sys.stdout
orig_stderr = sys.stderr
output_file = None
error_file = None
output_filename = None
error_filename = None

lock = multiprocessing.Lock()

def prepare():
    # redirect stdout and stderr
    starting_datetime = datetime.today().strftime("%Y-%m-%d_%H-%M")
    global output_filename
    output_filename = settings.WORKING_DIR + "output." + starting_datetime + ".log"
    global error_filename
    error_filename = settings.WORKING_DIR + "output." + starting_datetime + ".log.err"

    global output_file
    output_file = open(output_filename, 'w')
    sys.stdout = output_file

    global error_file
    error_file = open(error_filename, 'w')
    sys.stderr = error_file

    prepare_fitness_log()

    print starting_datetime


def restore():
    ending_datetime = datetime.today().strftime("%Y-%m-%d_%H-%M")
    print ending_datetime

    global orig_stdout
    sys.stdout = orig_stdout
    global orig_stderr
    sys.stderr = orig_stderr


def log_progress(string):
    lock.acquire()
    orig_stdout.write(string)
    orig_stdout.flush()
    lock.release()


def clear_progress():
    os.system('cls' if os.name == 'nt' else 'clear')


def redirect_string(log_output = True):
    if log_output:
        return " 1>>" + str(output_filename) + " 2>>" + str(error_filename)
    else:
        return " 1>>/dev/null 2>>" + str(error_filename)

def prepare_fitness_log():
    fitness_log_file = settings.WORKING_DIR + "/fitness-historic.log"
    os.system("echo \"timestamp,coverage,crashes,length\" > " + fitness_log_file)

def log_fitness_result(fitness):
    fitness_log_file = settings.WORKING_DIR + "fitness-historic.log"
    coverage = str(fitness[0])
    length = str(fitness[1])
    crashes = str(fitness[2])
    timestamp = str(time.time())
    os.system("echo \"" + timestamp + "," + coverage + "," + crashes + "," + length + "\" >> " + fitness_log_file)