#!/bin/bash
python run_sapienz.py > tmux-output-08-6-sapienz-$1.txt
python run_sapienz.py -s random > tmux-output-08-6-random-$1.txt
python run_sapienz.py --no-motifgene > tmux-output-08-6-no-motifgene-$1.txt

# prepare results folder
mkdir results-$1
mv monkey/results/* results-$1/
mv output* results-$1/
mv tmux* results-$1/

# copy results and backup
mkdir ~/Dropbox/datos-tesis/muPlusLambda/$1
cp results-$1/muPlusLambda/arity/0/*-evaluations.log ~/Dropbox/datos-tesis/muPlusLambda/$1/
cp results-$1/muPlusLambda/arity/0/targets-historic.log ~/Dropbox/datos-tesis/muPlusLambda/$1/

mkdir ~/Dropbox/datos-tesis/muPlusLambda-nm/$1/
cp results-$1/muPlusLambda-nm/arity/0/*-evaluations.log ~/Dropbox/datos-tesis/muPlusLambda-nm/$1/
cp results-$1/muPlusLambda-nm/arity/0/targets-historic.log ~/Dropbox/datos-tesis/muPlusLambda-nm/$1/

mkdir ~/Dropbox/datos-tesis/random/$1/
cp results-$1/random/arity/0/*-evaluations.log ~/Dropbox/datos-tesis/random/$1/
cp results-$1/random/arity/0/targets-historic.log ~/Dropbox/datos-tesis/random/$1/

mv -n results-$1 ~/backup_resultados/1h/ # only move if folder doesn't exist
