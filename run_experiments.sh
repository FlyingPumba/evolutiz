#!/bin/bash
SUBJECTS_FOLDER="$1"
EMULATORS=8
REPETITIONS=1
TIME="120m"

EVOLUTIZ_CMD_PREFIX=".env/bin/python main.py -vv \
--no-compress \
--continue-on-subject-failure \
--continue-on-repetition-failure \
--coverage ella"

EVOLUTIZ_CMD="$EVOLUTIZ_CMD_PREFIX --emulators-number $EMULATORS --repetitions $REPETITIONS --time-budget $TIME"

for subject in $SUBJECTS_FOLDER*.apk; do
  filename=$(basename $subject)

  notify -hn -m "JSEP $filename (sapienz)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s muPlusLambda -t motifcore -e multi-objective 2>&1 >sapienz-$filename-jsep-$(ls -1 sapienz-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (sapienz-no-motifgenes)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s muPlusLambda -t motifcore-no-motifgenes -e multi-objective 2>&1 >sapienz-no-motifgenes-$filename-jsep-$(ls -1 sapienz-no-motifgenes-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (random-search)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s randomSearch -t motifcore -e multi-objective 2>&1 >random-search-$filename-jsep-$(ls -1 random-search-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (random-search-no-motifgenes)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s randomSearch -t motifcore-no-motifgenes -e multi-objective 2>&1 >random-search-no-motifgenes-$filename-jsep-$(ls -1 random-search-no-motifgenes-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (standard)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s standard -t motifcore-no-motifgenes -e single-objective 2>&1 >standard-$filename-jsep-$(ls -1 standard-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (monotonic)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s monotonic -t motifcore-no-motifgenes -e single-objective 2>&1 >monotonic-$filename-jsep-$(ls -1 monotonic-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (steady)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s steady -t motifcore-no-motifgenes -e single-objective 2>&1 >steady-$filename-jsep-$(ls -1 steady-$filename-jsep* 2>/dev/null | wc -l).out"

#  notify -hn -m "JSEP $filename (one-plus)" \
#    "$EVOLUTIZ_CMD --subject-path $subject  -s onePlusLambdaCommaLambda -t motifcore-no-motifgenes -e single-objective  2>&1 >one-plus-$filename-jsep-$(ls -1 one-plus-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (sapienz-single-objective-no-motifgene)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s muPlusLambda -t motifcore-no-motifgenes -e single-objective 2>&1 >sapienz-single-objective-no-motifgene-$filename-jsep-$(ls -1 sapienz-single-objective-no-motifgene-$filename-jsep* 2>/dev/null | wc -l).out"

  notify -hn -m "JSEP $filename (mu-comma-lambda)" \
    "$EVOLUTIZ_CMD --subject-path $subject -s muCommaLambda -t motifcore-no-motifgenes -e single-objective 2>&1 >mu-comma-lambda-$filename-jsep-$(ls -1 mu-comma-lambda-$filename-jsep* 2>/dev/null | wc -l).out"
done

