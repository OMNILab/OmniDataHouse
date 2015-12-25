package cn.edu.sjtu.omnilab.odh.spark

import cn.edu.sjtu.omnilab.odh.rawfilter.{WIFILogFilter}
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
      .map { m => WIFILogFilter.filterData(m)}
      .filter(_ != null)
      .saveAsTextFile(output)

    spark.stop()

  }

}
