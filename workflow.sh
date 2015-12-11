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

## Run WifiSyslog cleansing
exec '$BASEDIR/porters/wifi_syslog.sh'

## Run WifiSyslogSession extraction
exec '$BASEDIR/porters/wifi_syslog_session.sh'
