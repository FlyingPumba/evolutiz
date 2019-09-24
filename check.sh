#!/bin/bash
mypy --config-file mypy.ini main.py
find . -name '*.py' -not -path './.env/**' -exec pylint {} \;
find . -name '*.py' -not -path './.env/**' -exec pep8 {} \;
