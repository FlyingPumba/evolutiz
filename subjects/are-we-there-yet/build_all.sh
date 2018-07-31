#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

for i in `ls -d */`; do
  echo "** BUILDING Project $i"
  cd $DIR/$i
  ant clean
  ant emma debug > build.log
done
