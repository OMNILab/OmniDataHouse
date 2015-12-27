# WifiSyslogHTTP

This repo contains updated Wifi HTTP traffic logs.


## Data path

    hdfs://user/omnilab/warehouse/WifiTraffic/HTTP


## Data format

There are 25 fields recorded and each line represents a request-response (RR) pair.

**Connection info.**

* [0] source_ip: string, the source IP address
* [1] source_port: int, the source port number of a connection
* [2] dest_ip: string, the destination IP address
* [3] dest_port: int, the destination port number of a connection
* [4] connection: string, position indicator of an RR pair in a connection

**Connection timings**

* [5] connect_ts: double, the timestamp to start the TCP connection
* [6] close_ts: double, the timestamp to terminate the TCP connection
* [7] connection_dur: double, the duration in seconds of the whole TCP connection
* [8] idle_time0: double, the idle interval between connection success and first byte of request
* [9] request_ts: double, the timestamp to send the first byte of request
* [10] request_dur: double, the duration to sending out request data
* [11] response_ts: double, the timestsamp to receive the first byte of response
* [12] response_dur_b: double, the idle interval between last byte of request and first byte of response
* [13] response_dur_e: double, the idle interval between first byte of response and last byte of response
* [14] idle_time1: double, the idle interval between response end and next new request or TCP connection end.

**RR volumes**

* [15] request_size: int, the size of request (including request header length)
* [16] response_size: int, the size of response (including response header length)

**Request keywords**

* [17] request_method: string, request method, such as GET, POST.
* [18] request_url: string, requested URL
* [19] request_host: string, remote server name
* [20] request_user_agent: string, indicator of user agent
* [21] request_referrer: string, request referrer

**Response keywords**

* [22] response_code: int, response statues
* [23] response_server: string, the remote server type
* [24] response_ctype: string, the content type


## Data sample

    10.186.227.204|63851|114.66.198.57|80|start|1446307201.983||0.030|0.000|1446307202.129|0.000|1446307202.413|0.028|0.000|0.004|589|291|GET|/tools/FsPlatformAction?rprotocol=3*_*action=161.Foamii*_*actionresult=13888*_*actionobjectver=0*_*channelid=*_*mac=64D954A9C582*_*guid=83fced5c-ac31-4e78-880e-1a201ee9cff6*_*name=Aptupd*_*version=3.0.7.5*_*actiontime=|1|other*_*pullupname=FunWorks64*_*pullupversion=*_*cid=1799*_*aptid=1|3|stat.funshion.net|Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; Shuame; GWX:RESERVED) Funshion/1.0.0.1||200|nginx/1.2.2|text/plain
    10.186.227.204|63851|114.66.198.57|80|continue|||||1446307202.449|0.000|1446307202.729|0.028|0.000|0.006|589|291|GET|/tools/FsPlatformAction?rprotocol=3*_*action=161.Foamii*_*actionresult=13825*_*actionobjectver=0*_*channelid=*_*mac=64D954A9C582*_*guid=83fced5c-ac31-4e78-880e-1a201ee9cff6*_*name=Aptupd*_*version=3.0.7.5*_*actiontime=|1|other*_*pullupname=FunWorks64*_*pullupversion=*_*cid=1799*_*aptid=1|3|stat.funshion.net|Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; Shuame; GWX:RESERVED) Funshion/1.0.0.1||200|nginx/1.2.2|text/plain
    10.187.252.52|62241|121.195.187.54|80|unique|1446307202.270|1446307202.382|0.036|0.009|1446307202.315|0.000|1446307202.349|0.034|0.000|0.033|780|1349|POST|/sugg?ifc=4&em=4|s.wisdom.www.sogou.com|SogouPSI||200|nginx|
    10.186.227.204|63851|114.66.198.57|80|last||1446307202.400|||1446307202.787|0.000|1446307202.107|0.028|0.000|0.293|579|291|GET|/tools/FsPlatformAction?rprotocol=3*_*action=161.Foamii*_*actionresult=13829*_*actionobjectver=0*_*channelid=*_*mac=64D954A9C582*_*guid=83fced5c-ac31-4e78-880e-1a201ee9cff6*_*name=Aptupd*_*version=3.0.7.5*_*actiontime=*_*pullupname=FunWorks64*_*pullupversion=*_*cid=1799*_*aptid=3|stat.funshion.net|Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; Shuame; GWX:RESERVED) Funshion/1.0.0.1||200|nginx/1.2.2|text/plain
