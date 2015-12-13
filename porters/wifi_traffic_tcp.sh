#!/bin/bash

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

# Check root path for raw data
if [ ! -d $WIFI_TRAFFIC_PATH ]; then
    die "Cann't find path for archived traffic data: $WIFI_TRAFFIC_PATH"
fi

year=`date -d "yesterday" "+%Y"`
month=`date -d "yesterday" "+%b"`
month2=`date -d "yesterday" "+%m"`
day=`date -d "yesterday" "+%d"`

INPUT_PATH=$WIFI_TRAFFIC_PATH/$year$month/tcp/*_$day_$month_$year.out

OUTPUT_TCP=$HDFS_WIFI_TRAFFIC/TCP/$year$month2$day
OUTPUT_TCP_NOCOMPLETE=$HDFS_WIFI_TRAFFIC/TCP_NOCOMPLETE/$year$month2$day
OUTPUT_UDP=$HDFS_WIFI_TRAFFIC/UDP/$year$month2$day

# Decompress files WITHOUT further processing
for file in `ls $INPUT_PATH`; do
    echo $file
    rfname=${file%.*}

    if ! hadoop fs -test -e $INPUT_TEMP/`basename $rfname`; then
        gunzip -c $file | hadoop fs -put - $INPUT_TEMP/`basename $rfname`
    fi
done

clean_trash

exit 0;
