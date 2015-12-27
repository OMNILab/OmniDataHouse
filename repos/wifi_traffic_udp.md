# WifiSyslogUDP

This repo contains updated Wifi UDP traffic logs. An UDP flow pair is identified when the first UDP segment is observed for a UDP socket pair, and is ended when no packet has been observed (from both sides) for 10s after the first packet or 3min after the last data packet. For detailed description, please refer to [official documentation](https://web.archive.org/web/20130331032520/http://tstat.polito.it/measure.shtml).


## Data path

    hdfs://user/omnilab/warehouse/WifiTraffic/UDP


## Data format

There are 16 fields recorded and each line represents an individual UDP flow:

* [1] Client IP address
* [2] Client UDP port
* [3] Client first packet in absolute time (epoch)
* [4] Client time between the first and the last packet from the 'client'
* [5] Client number of bytes transmitted in the payload
* [6] Client total number of packets observed from the client/server
* [7] Client if IP address is internal
* [8] Client Protocol type
* [9] Server IP address
* [10] Server UDP port
* [11] Server first packet in absolute time (epoch)
* [12] Server time between the first and the last packet from the 'client'
* [13] Server number of bytes transmitted in the payload
* [14] Server total number of packets observed from the client/server
* [15] Server if IP address is internal
* [16] Server Protocol type

## Data sample

    10.186.218.86 13964 1451062801455.429932 0.000000 137 1 1 12 217.216.91.184 53411 0.000000 0.000000 0 0 0 0
    10.186.218.86 13964 1451062801455.577881 0.000000 106 1 1 12 14.162.3.135 1066 0.000000 0.000000 0 0 0 0
    87.241.155.52 49001 1451062801456.123047 0.000000 225 1 0 12 10.187.192.237 56525 0.000000 0.000000 0 0 1 0
