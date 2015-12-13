package cn.edu.sjtu.omnilab.odh.spark

import cn.edu.sjtu.omnilab.odh.rawfilter.{APToBuilding, WIFICode}
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable.HashMap

/**
 * Merge individual's movement logs into sessions.
 *
 * Use the result of cn.edu.sjtu.omnilab.odh.spark.CleanseWifiLogs
 *
 * By Xiaming Chen
 */
object MergeWifiSession {

  // The largest to value to terminate a sesion, default 10 seconds.
  final val mergeSessionThreshold: Long = 10 * 1000

  val apToBuild = new APToBuilding()

  def main(args: Array[String]): Unit = {

    if ( args.length < 2) {
      println("Usage: MergeWifiSession <in> <out>")
      sys.exit(-1)
    }

    val input = args(0)
    val output = args(1)

    val conf = new SparkConf()
    conf.setAppName("Merge clean wifi logs into sesions")
    val spark = new SparkContext(conf)

    val inRDD = spark.textFile(input)

    // extract sessions
    val validSessionCodes = List(
      WIFICode.AuthRequest,
      WIFICode.AssocRequest,
      WIFICode.Deauth,
      WIFICode.Disassoc
    )

    spark.textFile(input)

      .map{ filtered => {
        val parts = filtered.split(',')

        if (parts.length < 4) {
          null
        } else {

          CleanWIFILog(
            MAC = parts(0),
            time = parts(1).toLong,
            code = parts(2).toInt,
            payload = parts(3)
          )

        }}}

      .filter(m => m != null && validSessionCodes.contains(m.code))

      .groupBy(_.MAC)

      .flatMap { case (key, logs) => { extractSessions(logs) }}

      .map { m =>
        val buildInfo = apToBuild.parse(m.AP)

        var bname: String = null
        var btype: String = null
        var bschool: String = null
        var blat: String = null
        var blon: String = null
        if ( buildInfo != null ) {
          bname = buildInfo.get(0)
          btype = buildInfo.get(1)
          bschool = buildInfo.get(2)
          blat = buildInfo.get(3)
          blon = buildInfo.get(4)
        }

        "%s,%d,%d,%s,%s,%s,%s,%s,%s"
          .format(m.MAC, m.stime, m.etime, m.AP, bname, btype, bschool, blat, blon)
    }

      .saveAsTextFile(output)

  }

  /**
   * Extract WIFI association sessions from certain individual's logs
   */
  def extractSessions(logs: Iterable[CleanWIFILog]): Iterable[WIFISession] = {

    var sessions = new Array[WIFISession](0)
    var APMap = new HashMap[String, Long]
    var preAP: String = null
    var preSession: WIFISession = null
    var curSession: WIFISession = null

    /*
     * Use algorithm iterated from previous GWJ's version of SyslogCleanser project.
     */
    logs.toSeq.sortBy(_.time)

      .foreach( log => {
        /*
         * construct network sessions roughly
         * currently, we end a session when AUTH and DEAUTH pair is detected
         */
        val mac = log.MAC
        val time = log.time
        val code = log.code
        val ap = log.payload

        if ( code == WIFICode.AuthRequest || code == WIFICode.AssocRequest) {

          if ( ! APMap.contains(ap) || (APMap.contains(ap) && ap != preAP))
            APMap.put(ap, time)

        } else if (code == WIFICode.Deauth || code == WIFICode.Disassoc) {

          if ( APMap.contains(ap) ) {
            // record this new session and remove it from APMap
            val stime = APMap.get(ap).get
            curSession = WIFISession(mac, stime, time, ap)
            APMap.remove(ap)

            // adjust session timestamps
            if ( preSession != null ) {
              val tdiff = curSession.stime - preSession.etime
              if (tdiff < 0)
                preSession = preSession.copy(etime = curSession.stime)

              // merge adjacent sessions under the same AP
              if ( preSession.AP == curSession.AP && tdiff < mergeSessionThreshold )
                preSession = preSession.copy(etime = curSession.etime)
              else {
                sessions = sessions :+ preSession
                preSession = curSession
              }

            } else {
              preSession = curSession
            }
          }
        }

        preAP = ap

      })

    if (preSession != null)
      sessions = sessions :+ preSession

    sessions.sortBy(_.stime).toIterable

  }
}
