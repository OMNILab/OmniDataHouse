# OmniDataHouse

Tool set for OMNILab data warehouse.


## Architecture

OMNILab data warehouse is designed with three layers:

* Layer0: Original raw data from multiple sources (NFS).

* Layer1: Independent wide tables for each source after simple ETLing (HDFS).

* Layer2: A bunch of small tables (models) after combining multiple data at Layer1 to fit different applications (HDFS).

In most scenarios, data administrators hold the access right to Layer0 and Layer1, and data users have accesses to the
small tables at Layer2 to meet their requirements.

The data users can also contribute new data models to Layer2 when they develop a new type of table from application. In
this process, other data sources may be involved to generate the new model. At this time, the user should contact admin
to add new data sources to Layer0 or Layer1.


## Layer2 Repos

* [WifiSyslogSession](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/wifi_syslog_session.md)


## Layer1 Repos

* [HzMobile](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/hz_mobile.md)

* [SenegalMobile](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/senegal_mobile.md)

* [WifiSyslog](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/wifi_syslog.md)

* [WifiTrafficHTTP](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/wifi_traffic_http.md)

* [WifiTrafficTCP](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/wifi_traffic_tcp.md)

* [WifiTrafficUDP](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/wifi_traffic_udp.md)

* [WifiUsers](https://github.com/OMNILab/OmniDataHouse/blob/master/repos/wifi_users.md)


## Project structure

* `etlers`: source code of ETL tools.

* `deploy`: folder to dploy binary ETL tools referred by `porters`.

* `porters`: automatic scripts to port a new repo periodically with ETL tools.

* `repos`: documentation for each repo.

* `global_config.sh`: global settings used by porters.

* `workflow.sh`: global workflow to run periodically.


## Instructions to add a new repo.

1. Add a related ETL program or script to `etlers`. Each program deserves an independent folder.

2. Add a shell script in `porters` to call your ETL program automatically.

3. Append the shell script to right position in `workflow.sh`.

4. Add documentation of the new repo to `repos`.

5. Contact admin to redeploy this tool set.


## Contact

* Xiaming Chen, chen_xm@sjtu.edu.cn

* Haiyang Wang, oceanking111@163.com
