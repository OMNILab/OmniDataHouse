package cn.edu.sjtu.omnilab.odh.spark

import cn.edu.sjtu.omnilab.odh.rawfilter.{WIFILogFilter}
import cn.edu.sjtu.omnilab.odh.utils.Utils
import org.apache.spark.{SparkConf, SparkContext}


/**
 * Cleanse raw WIFI syslog into movement data.
 */
object CleanseWifiLogs {

  final val mergeSessionThreshold: Long = 10 * 1000

  def main(args: Array[String]): Unit = {

    if ( args.length < 2) {
      println("Usage: CleanseWifiLogs <in> <out>")
      sys.exit(-1)
    }

    val input = args(0)
    val output = args(1)

    val conf = new SparkConf()
    conf.setAppName("Cleanse WIFI syslog into movement data")
    val spark = new SparkContext(conf)

    val inRDD = spark.textFile(input)
      .map { m => {

        val filtered = WIFILogFilter.filterData(m)
        var cleanLog: CleanWIFILog = null

        if (filtered != null) {
          val parts = filtered.split(',')

          cleanLog = CleanWIFILog(MAC = parts(0),
            time = Utils.ISOToUnix(parts(1)),
            code = parts(2).toInt,
            payload = parts(3))

        }

        cleanLog

      }}
      .filter(_ != null)

      .map(m => "%s,%d,%d,%s".format(m.MAC, m.time, m.code, m.payload))

      .saveAsTextFile(output)

    spark.stop()

  }

}
