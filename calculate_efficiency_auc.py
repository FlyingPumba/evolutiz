import sys

delta_time = 5 * 60 # 5 minutes
if sys.argv.length > 2:
  delta_time = sys.argv[1]

print "Using Delta time: " + str(delta_time) + "m"

# gather_data
coverage_data = []
crashes_data = []
length_data = []
timestamp_data = []

with open('./fitness-historic.log', 'r') as historic_file:
  lines = historic_file.read().splitlines()
  lines.pop(0) # discard header of file

  for line in lines:
      timestamp, coverage, crashes, length = line.split(',')
      timestamp_data.append(timestamp)
      coverage_data.append(coverage)
      crashes_data.append(crashes)
      length_data.append(length)

# process data
coverage_sum = 0
crashes_sum = 0
length_sum = 0

coverage_best = 0
crashes_best = 0
length_best = sys.maxint

for index, current_timestamp in enumerate(timestamp_data[:-1]):
  next_timestamp = timestamp_data[index + 1]
  delta_time = next_timestamp - current_timestamp

  # i-th data
  if coverage_data[index] > coverage_best:
    coverage_best = coverage_data[index]
  current_coverage = coverage_best

  if crashes_data[index] > crashes_best:
    crashes_best = crashes_data[index]
  current_crashes = crashes_best

  if length_data[index] > length_best:
    length_best = length_data[index]
  current_length = length_best

  #i+1-th data
  if coverage_data[index + 1] > coverage_best:
    coverage_best = coverage_data[index + 1]
  next_coverage = coverage_best

  if crashes_data[index + 1] > crashes_best:
    crashes_best = crashes_data[index + 1]
  next_crashes = crashes_best

  if length_data[index + 1] > length_best:
    length_best = length_data[index + 1]
  next_length = length_best

  coverage_sum += (current_coverage + next_coverage) * delta_time
  crashes_sum += (current_crashes + next_crashes) * delta_time
  length_sum += (current_length + next_length) * delta_time

# calcualte AUC
total_time = timestamp_data[-1] - timestamp_data[0]
coverage_auc = coverage_sum / float(2 * total_time)
crashes_auc = crashes_sum / float(2 * total_time)
length_auc = length_sum / float(2 * total_time)

print "Coverage AUC: " + str(coverage_auc)
print "Crashes AUC: " + str(crashes_auc)
print "Length AUC: " + str(length_auc)