# WifiSyslogTCP

This repo contains updated Wifi TCP traffic logs, which reports every TCP connection that has been tracked by [Tstat 2.3.1](http://tstat.polito.it/). A TCP connection is identified when the first SYN segment is observed, and is ended when either

* the FIN/ACK or RST segments are observer;
* no data packet has been observed (from both sides) for a default timeout of 10s after
the thress-way handshake or 5 min after the last data packet.

Tstat discards all the connections for which the three-way handshake is not properly seen. Then, in case a connection is correctly closed it is stored in log_tcp_complete, otherwise in log_tcp_nocomplete. For detailed description, please refer to [official documentation](https://web.archive.org/web/20130331032520/http://tstat.polito.it/measure.shtml).



## Data path

    hdfs://user/omnilab/warehouse/WifiTraffic/TCP
    hdfs://user/omnilab/warehouse/WifiTraffic/TCP_NOCOMPLETE


## Data format

There are 111 fields recorded and each line represents an individual TCP flow:

**Client info.**

* [1] Client IP addr
* [2] Client TCP port
* [3] Client packets
* [4] Client RST sent
* [5] Client ACK sent
* [6] Client PURE ACK sent
* [7] Client unique bytes
* [8] Client data packets
* [9] Client data bytes
* [10] Client rexmit packets
* [11] Client rexmit bytes
* [12] Client out sequence packets
* [13] Client SYN count
* [14] Client FIN count
* [15] Client RFC 1323 ws sent
* [16] Client RFC 1323 ts sent
* [17] Client window sacle factor
* [18] Client SACK option set
* [19] Client SACK sent
* [20] Client MSS declared
* [21] Client max segment size observed
* [22] Client min segment size observed
* [23] Client max receiver windows announced
* [24] Client min receiver windows announced
* [25] Client segements window zero
* [26] Client max cwin (in-flight-size)
* [27] Client min cwin (in-flight-size)
* [28] Client initial cwin (in-flight-size)
* [29] Client average RTT
* [30] Client min RTT
* [31] Client max RTT
* [32] Client standard deviation RTT
* [33] Client valid RTT count
* [34] Client min TTL
* [35] Client max TTL
* [36] Client rexmit segments RTO
* [37] Client rexmit segments FR
* [38] Client packet recording observed
* [39] Client network duplicated observed
* [40] Client unknown segments classified
* [41] Client rexmit segments flow control
* [42] Client unnece rexmit RTO
* [43] Client unnece rexmit FR
* [44] Client rexmit SYN different initial seqno

**Server info.**

* [45] Server IP addr
* [46] Server TCP port
* [47] Server packets
* [48] Server RST sent
* [49] Server ACK sent
* [50] Server PURE ACK sent
* [51] Server unique bytes
* [52] Server data packets
* [53] Server data bytes
* [54] Server rexmit packets
* [55] Server rexmit bytes
* [56] Server out sequence packets
* [57] Server SYN count
* [58] Server FIN count
* [59] Server RFC 1323 ws sent
* [60] Server RFC 1323 ts sent
* [61] Server window sacle factor
* [62] Server SACK option set
* [63] Server SACK sent
* [64] Server MSS declared
* [65] Server max segment size observed
* [66] Server min segment size observed
* [67] Server max receiver windows announced
* [68] Server min receiver windows announced
* [69] Server segements window zero
* [70] Server max cwin (in-flight-size)
* [71] Server min cwin (in-flight-size)
* [72] Server initial cwin (in-flight-size)
* [73] Server average RTT
* [74] Server min RTT
* [75] Server max RTT
* [76] Server standard deviation RTT
* [77] Server valid RTT count
* [78] Server min TTL
* [79] Server max TTL
* [80] Server rexmit segments RTO
* [81] Server rexmit segments FR
* [82] Server packet recording observed
* [83] Server network duplicated observed
* [84] Server unknown segments classified
* [85] Server rexmit segments flow control
* [86] Server unnece rexmit RTO
* [87] Server unnece rexmit FR
* [88] Server rexmit SYN different initial seqno

**Flow info.**

* [89] Flow duration
* [90] Flow first packet time offset
* [91] Flow last segment time offset
* [92] Client first payload time offset
* [93] Server first payload time offset
* [94] Client last payload time offset
* [95] Server last payload time offset
* [96] Client first PURE ACK time offset
* [97] Server first PURE ACK time offset
* [98] Flow first packet absolute time
* [99] Client has internal IP
* [100] Server has internal IP
* [101] Flow type bitmask
* [102] Flow P2P type
* [103] Flow P2P subtype
* [104] P2P ED2K data message number
* [105] P2P ED2K signaling message number
* [106] P2P ED2K C2S message number
* [107] P2P ED2K S2C message number
* [108] P2P ED2K chat message number
* [109] Flow HTTP type
* [110] Flow SSL client hello
* [111] Flow SSL server hello


## Data sample

    10.187.72.40 55917 14 0 13 11 447 1 447 0 0 0 1 1 1 1 6 1 0 1386 447 447 55744 28000 0 447 447 447 2.804573 2.784000 2.822000 0.019218 3 60 60 0 0 0 0 0 0 0 0 0 117.144.242.26 80 9 0 9 2 13093 5 13093 0 0 0 1 1 1 0 7 1 0 1440 2772 2005 6912 5760 0 13093 2772 13093 20.041998 17.816000 22.270000 0.000000 2 51 51 0 0 0 0 0 0 0 0 0 58.683000 60.636000 119.319000 25.514000 37.947000 25.514000 38.077000 25.078000 28.336000 1451059201213.258057 1 0 1 0 0 0 0 0 0 0 2 - -
    10.185.227.136 28245 6 1 5 2 1861 2 1861 0 0 0 1 0 1 0 2 1 0 1386 1386 475 66528 8192 0 1861 1386 1861 3.806873 3.780000 3.861000 0.046765 3 60 60 0 0 0 0 0 0 0 0 0 182.254.11.191 80 5 0 5 2 230 1 230 0 0 0 1 1 1 0 9 1 0 1440 230 230 11776 5760 0 230 230 230 3.024349 2.469000 3.580000 0.000000 2 51 51 0 0 0 0 0 0 0 0 0 16.345000 108.137000 124.482000 7.571000 12.025000 7.631000 12.025000 6.330000 11.351000 1451059201260.759033 1 0 1 0 0 0 0 0 0 0 1 - -
    10.187.140.194 28875 5 0 4 2 287 1 287 0 0 0 1 1 1 0 0 1 0 1386 287 287 65535 65535 0 287 287 287 29.699015 27.832000 31.569000 0.000000 2 124 124 0 0 0 0 0 0 0 0 0 119.75.220.50 80 5 1 4 1 474 1 474 0 0 0 1 1 0 0 0 1 0 1200 474 474 65535 15544 0 474 474 474 22.925854 8.052000 37.802000 0.000000 2 44 48 0 0 0 0 0 0 0 0 0 134.026000 37.012000 171.038000 36.132000 67.956000 36.132000 67.956000 35.884000 67.701000 1451059201189.634033 1 0 1 0 0 0 0 0 0 0 1 - -
