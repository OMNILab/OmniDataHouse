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
CLSNAME="cn.edu.sjtu.omnilab.odh.spark.CleanseHttp"

# Check root path for raw data
if [ ! -d $WIFI_TRAFFIC_PATH ]; then
    die "Cann't find path for archived traffic data: $WIFI_TRAFFIC_PATH"
fi

year=`date -d "yesterday" "+%Y"`
month=`date -d "yesterday" "+%m"`
day=`date -d "yesterday" "+%d"`

INPUT=$WIFI_TRAFFIC_PATH/$year$month/http/$year$month$day-*.gz
INPUT_TEMP=$HDFS_WIFI_TRAFFIC/HTTP/_temp
OUTPUT=$HDFS_WIFI_TRAFFIC/HTTP/$year$month$day

# Decompress files
for rawfile in `ls $INPUT`; do
    echo $rawfile
    rfname=${rawfile%.*}

    if ! hadoop fs -test -e $INPUT_TEMP/`basename $rfname`; then
        gunzip -c $rawfile | hadoop fs -put - $INPUT_TEMP/`basename $rfname`
    fi
done

if ! hadoop fs -test -e $OUTPUT/_SUCCESS; then
    hadoop fs -rm -r $OUTPUT
    spark-submit2 --class $CLSNAME $BINJAR $INPUT_TEMP/$year$month$day* $OUTPUT
    hadoop fs -rm -r $INPUT_TEMP
fi

clean_trash

exit 0;
