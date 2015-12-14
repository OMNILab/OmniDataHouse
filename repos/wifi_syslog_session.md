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

* `IP address`: string, the allocated IP address for current session.

* `Account`: string, the anonymized account name of user.


**Definition of a mobility session**:

A mobility session consists of a duration associating to the same Wifi access point.
Two small sessions with an idle gap of less than 10 seconds are merged into a larger one.



## Data sample

    60facd611111,1368532272000,1368532315000,XSY-3F-09,西上院,TeachBldg,Public,31.025927,121.437374,111.186.19.43,LLLLLLLL
    60facd611111,1368532510000,1368532511000,XSY-2F-09,西上院,TeachBldg,Public,31.025927,121.437374,111.186.19.43,LLLLLLLL
    60facd611111,1368525160000,1368525417000,BYGTSG-3F-03,包玉刚图书馆,LibBldg,Public,31.028224,121.436552,111.186.15.86,MMMMMMMM
