# WifiToolkit

Spark jobs to process Wifi traffic and syslog data.

## Build

    $ # build with Java 1.6 to be compatible with our hadoop cluster
    $ sbt -java-home $(/usr/libexec/java_home -v '1.6*') assembly