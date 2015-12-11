# WifiSyslog

This repo contains cleansed data for Aruba Wifi Syslog, at SJTU.


## Data path

    hdfs://user/omnilab/warehouse/WifiSyslog


## Data format

Four fields are contained in the cleansed data:

* `User ID`: string, recorded as device MAC address.

* `Timestamp`: long, recorded as the time (in milliseconds) when the logging action is triggered by Aruba system.

* `Action code number`: int, the formated code for different actions triggered by Aruba system.

    - 0: AuthRequest
    - 1: Deauth
    - 2: AssocRequest
    - 3: Disassoc
    - 4: UserAuth
    - 5: IPAllocation
    - 6: IPRecycle

* `Payload`: string, contains three types of payloads for different actions:

    - `Associated AP name`:

    Action 0-3, when the logging action is triggered. AP name takes "BuildName-BuildNum-Floor-APNum" format, where the BuildNum may be absent for small buildings.

    - `User account name`:

    Action 4, the identify of JAccount.

    - `Allocated IP address`:

    Action 5-6, the allocated IP address for current user. IP addresses are reused by Aruba systems. The same IP address MIGHT be reallocated to another user when that address is unused for about 30 mins to a hour.


## Data sample

    90187c511111,1366715483000,3,TSG-1-3F-03
    701a04b22222,1366715483000,5,111.186.58.26
    ccf9e8a33333,1366715119000,0,XXY-1F-06
    ccf9e8a44444,1366715119000,2,XXY-1F-06
    ccf9e8a55555,1366715119000,2,XXY-1F-06
