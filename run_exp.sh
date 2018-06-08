#!/bin/bash
python run_sapienz.py > tmux-output-08-6-random-$1.txt
python run_sapienz.py -s random > tmux-output-08-6-random-$1.txt
python run_sapienz.py --no-motifgene > tmux-output-08-6-no-motifgene-$1.txt
mkdir results-$1
mv monkey/results/* results-$1/
mv output* results-$1/
mv tmux* results-$1/
