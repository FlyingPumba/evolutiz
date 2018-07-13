import random

# Max and min values taken from Oracle documentation:
# https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html

def get_random_integer():
    return random.randint(-2147483648, # -2^(31)
                          2147483647)  # 2^(31)-1

def get_random_long():
    return random.randint(-9223372036854775808, # -2^(63)
                          9223372036854775807)  # 2^(63)-1

def get_random_float():
    return float(0)