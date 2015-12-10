# WifiSyslogSession

This repo contains sessional data for individul's movement.


## Data path

    hdfs://user/omnilab/warehouse/WifiSyslogSession


## Data format

This repo contains several sessional mobility features, including:

* `MAC address`: string, the physical MAC address.

* `Start timestamp`: long, the starting timestamp for a mobility session in milliseconds.

* `End timestamp`: long, the ending timestamp for a mobility session in milliseconds.

* `Wifi access point name`: string, the name of a specific hotspot.

* `Building semantic name`: string, the semantic name of a buidling for a specific hotspot.

* `Building type`: string, the type of a building.

* `Building department`: string, the department occupying specific building.

* `Latitude`: float, the latitude coordinate of specific building.

* `Longitude`: float, the longitude coordinate of specific building.


**Definition of a mobility session**:

A mobility session consists of a duration associating to the same Wifi access point.
Two small sessions with an idle gap of less than 10 seconds are merged into a larger one.



## Data sample

    78e400111111,1366725353000,1366725354000,TSG-2-2F-02,图书馆二区,LibBldg,Public,31.032837,121.443493
    00c610222222,1366715995000,1366716052000,BYGTSG-2F-01,图书馆,LibBldg,Public,31.028224,121.436552
    28e02c633333,1366719817000,1366719817000,LXZL-1F-02,老行政楼,AdmBldg,Public,31.024936,121.437448
    50ead6c44444,1366723611000,1366715873000,TSG-1-2F-04,图书馆一区,LibBldg,Public,31.03262,121.442788