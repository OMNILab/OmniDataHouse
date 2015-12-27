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
monthChr=`date -d "yesterday" "+%b"`
monthDig=`date -d "yesterday" "+%m"`
day=`date -d "yesterday" "+%d"`

monthnames=(invalid Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec)
if [ XXOO$1 != "XXOO" ]; then
    year=`echo $1 | cut -d "-" -f1`
    monthDig=`echo $1 | cut -d "-" -f2`
    monthChr=${monthnames[${monthDig}]}
    day=`echo $1 | cut -d "-" -f3`
fi

INPUT_PATH=$WIFI_TRAFFIC_PATH/${year}${monthDig}/tcp
OUTPUT_TCP=$HDFS_WIFI_TRAFFIC/TCP/$year$monthDig$day
OUTPUT_TCP_NOCOMPLETE=$HDFS_WIFI_TRAFFIC/TCP_NOCOMPLETE/$year$monthDig$day
OUTPUT_UDP=$HDFS_WIFI_TRAFFIC/UDP/$year$monthDig$day

# Decompress files WITHOUT further processing
for ((i = 0; i < 24; i++)); do
    hour=`printf "%02d" $i`
    file=$INPUT_PATH/${hour}_00_${day}_${monthChr}_${year}.out
    echo $file
    rfname=${file%.*}

    if ! hadoop fs -test -e ${OUTPUT_TCP}/$hour; then
         python wifi_traffic_tcp/unzip_tcp.py $file/log_tcp_complete.gz \
	     | hadoop fs -put - ${OUTPUT_TCP}/$hour
    fi

    if ! hadoop fs -test -e ${OUTPUT_TCP_NOCOMPLETE}/$hour; then
         python wifi_traffic_tcp/unzip_tcp.py $file/log_tcp_nocomplete.gz \
	     | hadoop fs -put - ${OUTPUT_TCP_NOCOMPLETE}/$hour
    fi

    if ! hadoop fs -test -e ${OUTPUT_UDP}/$hour; then
         python wifi_traffic_tcp/unzip_tcp.py $file/log_udp_complete.gz \
	     | hadoop fs -put - ${OUTPUT_UDP}/$hour
    fi

done

exit 0;
