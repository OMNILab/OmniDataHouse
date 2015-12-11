# HangZhou Data Description

This data set contains user web-browsing logs in 19 days, across two months,
in August and October, 2012. The network topology covers the main areas of
Hangzhou City and Wenzhou City, Zhejiang Province.


## Data path

    hdfs://user/omnilab/warehouse/HzMobile/hzclean


## Data Columns

This folder maintains the set after data cleansing and formatting. Each set
contains 27 independent columns separated by '\t' to describe user web-browsing activities.

* ttime (double): timestamp issuing a web request, in seconds
* dtime (double): timestamp ending a request or dumping this log, in seconds
* BS (long): signature of individual base stations (LAC*10^6 + CI)
* IMSI (string): user IMSI signature
* mobile_type (string): signature of mobile client type
* dest_ip (long): destination IP address
* dest_port (int): destination TCP port
* success (long): indicating if the web request succeeded
* failure_cause (string): reason of web request failure
* response_time (long): time delay from request to the first byte of response
* host (string): host name of web request
* content_length (long): content-length field of HTTP header
* retransfer_count (long): the number of retransmission
* packets (long): the number of network packets
* status_code (int): HTTP status code
* web_volume (long): byte number of transfered web request
* content_type (string): content-type field of HTTP header
* user_agent (string): MD5 value of user-agent field of HTTP header
* is_mobile (int): if the client is mobile device
* e_gprs (int): E_GPRS mode indicator
* umts_tdd (int): UMTS/TDD mode indicator
* ICP (long): classification of Internet Content Providers, e.g., Netease
* SC (string): service classification, e.g., video, music.
* URI (string): Uniform resource identifier
* OS (string): operating system type
* LON (double): latitude of base station location
* LAT (double): longitude of base station location


## Data Stat

* Total logs: 852314304
* Total unique users:
* Total base stations:


## Data sample

    1345084549.229  1345085752.000  22696030330 460022688112277     1862344734  80  1       2000    storage7.cdn.kugou.com  17221   0   12  206 16384   application/octet-stream            12  0   13  酷狗音乐网   /201208161032/602b72233338bcf732ed1a0d1ab9de0e/M01/11/DC/OtfxyE_xEgf9hd16AB-VzJZmo9g824.m4a     119.06104   29.615866
    1345084666.528  1345085752.000  22696030330 460007472554744 com.sina.weibo  1862344789  80  1       1880    tp4.sinaimg.cn  3079    0   3   200 2550    image/jpeg  zMu/gx+2nvFedIg8HudOww==    1   10  0   32  新浪微博    /2044794631/50/5613889201/0 IOS 119.06104   29.615866
