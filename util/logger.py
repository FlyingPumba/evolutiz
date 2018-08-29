import multiprocessing
import os
import sys
import time
from datetime import datetime

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


def redirect_string(log_output=True):
    if log_output:
        return " 1>>" + str(output_filename) + " 2>>" + str(error_filename)
    else:
        return " 1>>/dev/null 2>>" + str(error_filename)

