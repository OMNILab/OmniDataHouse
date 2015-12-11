#!/bin/bash

function clean_trash () {
  hadoop fs -rm -r .Trash/Current > /dev/null
}

function die () {
    echo "${@}"
    exit 1
}

# Check permission
if [ `whoami` != 'omnilab' ]; then
    die "Need permission of OMNILAB to run. Try user omnilab."
fi

# Global vars
BASEDIR=$(dirname $0)/..
source $BASEDIR/global_config.sh

# Cleansing tools
BINJAR=$BASEDIR/deploy/WifiToolkit-assembly-1.0.jar
CLSNAME="cn.edu.sjtu.omnilab.odh.spark.MergeWifiSession"

# Check HDFS path for clean wifi logs
if ! hadoop fs -test -d $HDFS_WIFI_SYSLOG; then
    die "Cann't find WifiSyslog repo: $HDFS_WIFI_SYSLOG"
fi

# Check root path for this repo
if ! hadoop fs -test -d $HDFS_WIFI_SYSLOG_SESSION; then
    hadoop fs -mkdir -p $HDFS_WIFI_SYSLOG_SESSION
fi

year=`date -d "yesterday" '+%Y'`
month=`date -d "yesterday" '+%m'`
day=`date -d "yesterday" '+%d'`

INPUT=$HDFS_WIFI_SYSLOG/wifilog$year-$month-$day
OUTPUT=$HDFS_WIFI_SYSLOG_SESSION/wifilog$year-$month-$day

if ! hadoop fs -test -e $OUTPUT/_SUCCESS; then
    hadoop fs -rm -r $OUTPUT
    spark-submit2 --class $CLSNAME $BINJAR $INPUT $OUTPUT
fi

clean_trash

exit 0;
