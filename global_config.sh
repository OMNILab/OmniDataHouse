#!/bin/bash

####################################
## Local data repos at NFS
####################################

DATA_FTP='/mnt/omnidata'

WIFI_TRAFFIC_PATH=$DATA_FTP/SJTU/wifi-archive

WIFI_SYSLOG_PATH=$DATA_FTP/SJTU/wifi-syslog

HZ_MOBILE_PATH=$DATA_FTP/NetworkTraffic/mobilelogs-hz2012/Original


####################################
## Data repos on HDFS
####################################

HDFS_WIFI_TRAFFIC='/user/omnilab/warehouse/WifiTraffic'

HDFS_WIFI_SYSLOG='/user/omnilab/warehouse/WifiSyslog'

HDFS_HZ_MOBILE='/user/omnilab/warehouse/HzMobile'

HDFS_D4D_SENEGAL='/user/omnilab/warehouse/Senegal'