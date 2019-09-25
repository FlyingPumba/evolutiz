#!/bin/bash
mypy --config-file mypy.ini main.py
find . -name '*.py' -not -path './.env/**' -not -path './monkey/android-sdk-linux/**' -exec pylint --max-line-length=120 {} \;
find . -name '*.py' -not -path './.env/**' -not -path './monkey/android-sdk-linux/**' -exec pycodestyle --max-line-length=120 {} \;
