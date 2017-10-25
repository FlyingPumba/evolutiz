#!/bin/bash

NAME=Arity
SRCS=src/calculator/Calculator.java
LIBS=`find libs -name "*.jar"`
KEYSTORE=/home/preda/cheie/and
KEYALIAS=and

SDK=/home/preda/sdk
PLATFORM=$SDK/platforms/android-8/
AAPT=$PLATFORM/tools/aapt
DX=$PLATFORM/tools/dx
AJAR=$PLATFORM/android.jar
PKRES=bin/resource.ap_
OUT=bin/$NAME-unalign.apk
ALIGNOUT=bin/$NAME.apk
set -e

rm -rf bin
mkdir -p bin/classes gen

echo aapt
$AAPT package -f -m -J gen -M AndroidManifest.xml -S res -A assets -I $AJAR -F $PKRES

echo javac
javac -d bin/classes -classpath bin/classes:$LIBS -sourcepath src:gen -bootclasspath $AJAR $SRCS

echo dx
$DX --dex --output=bin/classes.dex $LIBS bin/classes 

echo apkbuilder
apkbuilder $OUT -u -z $PKRES -f bin/classes.dex

echo jarsigner
jarsigner -keystore $KEYSTORE -storepass 000000 $OUT $KEYALIAS > /dev/null || jarsigner -keystore $KEYSTORE $OUT $KEYALIAS

echo zipalign
zipalign -f 4 $OUT $ALIGNOUT
