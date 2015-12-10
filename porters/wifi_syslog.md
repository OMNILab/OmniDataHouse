# WifiSyslog

This repo contains cleansed data for Aruba Wifi Syslog, at SJTU.

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

    - `Associated AP name`: action 0-3, when the logging action is triggered. AP name format: `BuildName`-`BuildNum`-`Floor`-`APNum`, where the BuildNum may be absent for small buildings.

    - `User account name`: action 4, the identify of JAccount.

    - `Allocated IP address`: action 5-6, the allocated IP address for current user. IP addresses are reused by Aruba systems. The same IP address MIGHT be reallocated to another user when that address is unused for about 30 mins to a hour.