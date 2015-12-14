#!/bin/bash
#
# Usage:
#  wifi_syslog.sh [2013-04-25]

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
CLSNAME="cn.edu.sjtu.omnilab.odh.spark.CleanseWifiLogs"

# Check root path for raw data
if [ ! -d $WIFI_SYSLOG_PATH ]; then
    die "Cann't find path for raw syslog: $WIFI_SYSLOG_PATH"
fi

# Check root path for this repo
if ! hadoop fs -test -d $HDFS_WIFI_SYSLOG; then
    hadoop fs -mkdir -p $HDFS_WIFI_SYSLOG
fi

# Clear temporary folder
TEMPWP=$HDFS_WIFI_SYSLOG/_temp
hadoop fs -rm -r $TEMPWP
hadoop fs -mkdir -p $TEMPWP

TARGET=$(date -d "yesterday" '+%Y-%m-%d')
if [ XXOO$1 != "XXOO" ]; then
    TARGET=$1
fi

# Process yesterday's file
for rawfile in `ls $WIFI_SYSLOG_PATH`; do

    rfname=${rawfile%.*}

    year=`echo $rfname | cut -d "-" -f1 | sed -e 's/wifilog\([0-9]*\)/\1/g'`
    month=`echo $rfname | cut -d "-" -f2`
    day=`echo $rfname | cut -d "-" -f3`

    if [ "$year-$month-$day" == $TARGET ]; then

    	if ! hadoop fs -test -e $HDFS_WIFI_SYSLOG/$rfname/_SUCCESS; then

    	    # Decompress file
    	    if ! hadoop fs -test -e $TEMPWP/$rfname; then
    		gunzip -c $WIFI_SYSLOG_PATH/$rawfile | hadoop fs -put - $TEMPWP/$rfname
    	    fi

    	    # Cleanse wifilog
    	    hadoop fs -rm -r $HDFS_WIFI_SYSLOG/$rfname
    	    spark-submit2 --class $CLSNAME $BINJAR $TEMPWP/$rfname $HDFS_WIFI_SYSLOG/$rfname
    	    hadoop fs -rm -r $TEMPWP/$rfname
    	fi

    fi

done

hadoop fs -rm -r $TEMPWP

clean_trash

exit 0;
