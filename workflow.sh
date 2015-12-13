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
BASEDIR=$(dirname $0)
source $BASEDIR/global_config.sh

## Run WifiSyslog cleansing
chmod +x $BASEDIR/porters/wifi_syslog.sh
$BASEDIR/porters/wifi_syslog.sh

## Run WifiSyslogSession extraction
chmod +x $BASEDIR/porters/wifi_syslog_session.sh
$BASEDIR/porters/wifi_syslog_session.sh

## Run WifiTrafficHttp cleansing
chmod +x $BASEDIR/porters/wifi_traffic_http.sh
$BASEDIR/porters/wifi_traffic_http.sh
