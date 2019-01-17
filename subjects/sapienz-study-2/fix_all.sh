#!/bin/bash

for i in `ls -d */`; do
  echo "Updating project $i"
  cd $i;
  #cp ../a2dp.Vol_93_src/local.properties ./
  ../../../monkey/android-sdk-linux/tools/android update project -t android-10 -p . --subprojects
  cd ../
done

