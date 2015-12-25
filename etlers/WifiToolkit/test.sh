#!/bin/bash
set -e

APP_NAME="cn.edu.sjtu.omnilab.odh.spark.MergeWifiSession"
BINJAR="target/scala-2.10/WifiToolkit-assembly-1.0.jar"

if [ $# -lt 2 ]; then
 echo "Usage: $0 <in> <out>"
 exit -1
fi

input=$1
output=$2
echo "Output file:" $output

rm -rf $output

spark-submit --master local --class $APP_NAME $BINJAR $input $output

if [ -d $output ]; then
    echo "Output: "
    head -n 20 $output/part-00000
    echo "..."
fi
