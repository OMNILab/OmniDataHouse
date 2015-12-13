#!/bin/bash
#
# Usage:
#  wifi_syslog_session.sh [2013-04-25]

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

TARGET=$(date -d "yesterday" '+%Y-%m-%d')
if [ $1 != "" ]; then
    TARGET=$1
fi

INPUT=$HDFS_WIFI_SYSLOG/wifilog$TARGET
OUTPUT=$HDFS_WIFI_SYSLOG_SESSION/wifilog$TARGET

if ! hadoop fs -test -e $OUTPUT/_SUCCESS; then
    hadoop fs -rm -r $OUTPUT
    spark-submit2 --class $CLSNAME $BINJAR $INPUT $OUTPUT
fi

clean_trash

exit 0;
