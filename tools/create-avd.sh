#!/bin/bash
n=$1
delete=${2:-false}
for i in $(seq 0 $(($n- 1))); do
	if $delete; then
		avdmanager delete avd --name Nexus_4_API_19_$i
	fi
	printf '\n\n' | avdmanager --verbose create avd --name Nexus_4_API_19_$i --package "system-images;android-19;google_apis;armeabi-v7a" --abi google_apis/armeabi-v7a 
	cat avd.config > .android/avd/Nexus_4_API_19_$i.avd/config.ini
done
