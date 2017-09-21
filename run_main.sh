#!/bin/sh
LOG="output.$(date +%Y-%m-%d_%H-%M).log"
touch $LOG
echo "Starting time: $(date +%Y-%m-%d_%H-%M)" >> $LOG
echo "Settings used: \n" >> $LOG
cat settings.py >> $LOG
echo "\n" >> $LOG

python main.py subjects/a2dp.Vol_93_src >> $LOG 2>$LOG.err

echo "Finishing time: $(date +%Y-%m-%d_%H-%M)" >> $LOG
