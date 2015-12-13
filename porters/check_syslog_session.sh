#!/bin/bash
# Check the dates without successful processing

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

for rawfile in `ls $WIFI_SYSLOG_PATH`; do
    rfname=${rawfile%.*}
    year=`echo $rfname | cut -d "-" -f1 | sed -e 's/wifilog\([0-9]*\)/\1/g'`
    month=`echo $rfname | cut -d "-" -f2`
    day=`echo $rfname | cut -d "-" -f3`
    if ! hadoop fs -test -e $HDFS_WIFI_SYSLOG_SESSION/$rfname/_SUCCESS; then
        echo `printf "%4d-%02d-%02d" $year ${month#0} ${day#0}`
    fi
done

exit 0;
