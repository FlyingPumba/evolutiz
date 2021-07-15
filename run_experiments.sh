#!/bin/bash
SUBJECTS_FOLDER="$1"
EMULATORS=8
REPETITIONS=1
TIME="120m"

if [ -z "$SUBJECTS_FOLDER" ]; then
    echo "A subjects folder is required as first parameter. E.g., ./run_experiments subjects/jsep/"
    exit 1
fi

EVOLUTIZ_CMD_PREFIX=".env/bin/python main.py -vv \
--no-compress \
--skip-subject-if-logbook-in-results \
--continue-on-subject-failure \
--continue-on-repetition-failure \
--coverage ella"

EVOLUTIZ_CMD="$EVOLUTIZ_CMD_PREFIX --emulators-number $EMULATORS --repetitions $REPETITIONS --time-budget $TIME"

# Delete logs for skipped runs in top folder
find . -maxdepth 1 -name "*.out" -exec bash -c "grep -q 'Exception: Skipping run' {} && echo {} && rm {}" \;

# Delete empty logs in top folder
find . -maxdepth 1 -type f -name "*.out" -size 0 -delete

for subject in $SUBJECTS_FOLDER*.apk; do
  filename=$(basename $subject)

  $EVOLUTIZ_CMD --subject-path $subject -s muPlusLambda -t motifcore -e multi-objective > "sapienz-$filename-jsep-$(ls -1 sapienz-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s muPlusLambda -t motifcore-no-motifgenes -e multi-objective > "sapienz-no-motifgenes-$filename-jsep-$(ls -1 sapienz-no-motifgenes-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s randomSearch -t motifcore -e multi-objective > "random-search-$filename-jsep-$(ls -1 random-search-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s randomSearch -t motifcore-no-motifgenes -e multi-objective > "random-search-no-motifgenes-$filename-jsep-$(ls -1 random-search-no-motifgenes-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s standard -t motifcore-no-motifgenes -e single-objective > "standard-$filename-jsep-$(ls -1 standard-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s monotonic -t motifcore-no-motifgenes -e single-objective > "monotonic-$filename-jsep-$(ls -1 monotonic-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s steady -t motifcore-no-motifgenes -e single-objective > "steady-$filename-jsep-$(ls -1 steady-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  # $EVOLUTIZ_CMD --subject-path $subject  -s onePlusLambdaCommaLambda -t motifcore-no-motifgenes -e single-objective  > "one-plus-$filename-jsep-$(ls -1 one-plus-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s muPlusLambda -t motifcore-no-motifgenes -e single-objective > "sapienz-single-objective-no-motifgene-$filename-jsep-$(ls -1 sapienz-single-objective-no-motifgene-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1

  $EVOLUTIZ_CMD --subject-path $subject -s muCommaLambda -t motifcore-no-motifgenes -e single-objective > "mu-comma-lambda-$filename-jsep-$(ls -1 mu-comma-lambda-$filename-jsep* 2>/dev/null | wc -l).out" 2>&1
done

# Delete logs for skipped runs in top folder
find . -maxdepth 1 -name "*.out" -exec bash -c "grep -q 'Exception: Skipping run' {} && echo {} && rm {}" \;

# Delete empty logs in top folder
find . -maxdepth 1 -type f -name "*.out" -size 0 -delete

notify -hn -m "run_experiments.sh" :