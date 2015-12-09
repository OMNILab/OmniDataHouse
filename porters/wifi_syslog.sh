#!/bin/bash

function clean_trash () {
  hadoop fs -rm -r .Trash/Current > /dev/null
}

function die () {
    echo "${@}"
    exit 1
}

# Global vars
BASEDIR=$(dirname $0)/..
source $BASEDIR/global_config.sh

# Cleansing tools
BINJAR=$BASEDIR/etlers/ArubaSyslog/target/scala-2.10/ArubaSyslog-assembly-1.0.jar
CLSNAME="cn.edu.sjtu.omnilab.odh.spark.CleanseWifiLogs"

TEMPWP=$HDFS_WIFI_SYSLOG/_temp
hadoop fs -rm -r $TEMPWP
hadoop fs -mkdir -p $TEMPWP

# Process yesterday's file
for rawfile in `ls $WIFI_SYSLOG_PATH`; do

    rfname=${rawfile%.*}

    year=`echo $rfname | cut -d "-" -f1 | sed -e 's/wifilog\([0-9]*\)/\1/g'`
    month=`echo $rfname | cut -d "-" -f2`
    day=`echo $rfname | cut -d "-" -f3`

    if [ $year$month$day == $(date -d "yesterday" '+%Y%m%d') ]; then

        if ! hadoop fs -test -e $HDFS_WIFI_SYSLOG/$rfname/_SUCCESS; then

            # Decompress file
            if ! hadoop fs -test -e $TEMPWP/$rfname; then
                gunzip -c $WIFI_SYSLOG_PATH/$rawfile | hadoop fs -put - $TEMPWP/$rfname
            fi

            # Cleanse wifilog
            hadoop fs -rm -r $HDFS_WIFI_SYSLOG/$rfname
            spark-submit2 --class $CLSNAME $BINJAR $TEMPWP/$rfname $HDFS_WIFI_SYSLOG/$rfname

        fi
    fi

done

hadoop fs -rm -r $TEMPWP

clean_trash

exit 0;
