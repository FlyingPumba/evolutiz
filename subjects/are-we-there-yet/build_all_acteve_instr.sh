#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

for i in `ls -d */`; do
  echo "** Adding acteve build file to Project $i"
  cd $DIR/$i
  match='<import file="custom_rules.xml" optional="true" \/>'
  insert='\n\n<!-- Acteve Instrumentation Config -->\n <property environment="env" \/>\n <fail unless="env.A3T_DIR" message="Set environment variable A3T_DIR to absolute location of a3t."\/>\n <condition property="a3t.xml" value="${env.A3T_DIR}\/a3t-app.xml">\n  <isset property="a3t"\/>\n <\/condition>\n <import file="${a3t.xml}" optional="true"\/>\n\n'
  sed "s/$match/$match\n$insert/" build.xml > build_acteve.xml
  #echo "  Cleaning project"
  #ant -f build_acteve.xml clean
  echo "  Building project with Acteve Instrumentation"
  ant -f build_acteve.xml -Da3t= emma debug &> build_acteve.log
done
