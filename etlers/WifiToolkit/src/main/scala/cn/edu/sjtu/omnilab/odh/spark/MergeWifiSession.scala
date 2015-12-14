package cn.edu.sjtu.omnilab.odh.spark

import cn.edu.sjtu.omnilab.odh.rawfilter.{APToBuilding, WIFICode}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._ // to use join etc.

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

    // Load clean Wifi syslog
    val inputRDD = spark.textFile(input)

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

        }}}.filter(m => m != null)

    // Filter IP sessions from input
    val ipSession = inputRDD
      .filter( m => m.code == WIFICode.IPAllocation || m.code == WIFICode.IPRecycle)
      .groupBy(_.MAC)
      .flatMap { case (key, logs) => { collectIPSession(logs) }}
      .keyBy(_.MAC)
      .cache

    // Filter Auth info. from input
    // If an account is detected only once for a MAC address, this account is
    // associate to that MAC for the whole day.
    // Otherwise, a session-based association is performed after the join action.
    val authRDD = inputRDD.filter(m => m.code == WIFICode.UserAuth)
      .map( m => CleanWIFILog(MAC=m.MAC, time=m.time, code=m.code, payload=m.payload))
      .distinct.groupBy(_.MAC)
      .mapValues{ case (authinfos) => {
        var accounts: List[String] = List[String]()
        authinfos.foreach(accounts ::= _.payload)

        if (accounts.distinct.length == 1) {
          authinfos.toList.take(1)
        } else {
          authinfos.toList
        }}}

    val sessionRDD = inputRDD
      // extract mobility sessions
      .filter(m => validSessionCodes.contains(m.code)).groupBy(_.MAC)
      .flatMap { case (key, logs) => { extractSessions(logs) }}

      // append IP info. to mobility sessions
      .keyBy(_.MAC)
      .leftOuterJoin(ipSession)
      .map { case (key, (wsession, ipsession)) => (wsession, ipsession.getOrElse(null))}
      .groupBy(_._1)
      .map{ case (wsession, ipsessions) => {

        // Determine IP by comparing the length of overlap for Wifi session and IP session.
        // The maximum temporal overlap corresponds to the right IP address.
        var result: IPSession = null
        var timeSpan: Long = 0

        ipsessions.foreach{ case (wsession, ipsession) => {
          var tspan: Long = 0

          if (ipsession != null) {
            val stime: Long = Math.max(ipsession.stime, wsession.stime)
            // We ARTIFICALLY add 15 mins more to tolerate data loss
            val etime: Long = Math.min(ipsession.etime + 15 * 3600 * 1000, wsession.etime)
            tspan = etime - stime
          }

          if ( tspan > timeSpan ) {
            timeSpan = tspan
            result = ipsession
          }
        }}

        (wsession, result) }}

      // append account info. to mobility sessions
      .keyBy(_._1.MAC)
      .leftOuterJoin(authRDD)
      .map { case (key, ((wsession, ipsession), authinfo)) => {
        val authinfos = authinfo.getOrElse(null)

        if ( authinfos != null ) {
          if (authinfos.size == 1) {
            (wsession, ipsession, authinfos(0))
          } else {
            // A session-based association is performed
            var result: CleanWIFILog = null
            authinfos.foreach( auth => {
              if ( auth.time >= wsession.stime && auth.time <= wsession.etime ) {
                result = auth
              }
            })
            (wsession, ipsession, result)
          }
        } else {
          (wsession, ipsession, null)
        } }}

      .map { case (wsession, ipsession, authinfo) =>
        val buildInfo = apToBuild.parse(wsession.AP)

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

        var ipaddr: String = null
        if ( ipsession != null ) {
          ipaddr = ipsession.IP
        }

        var account: String = null
        if ( authinfo != null ) {
          account = authinfo.payload
        }

        "%s,%d,%d,%s,%s,%s,%s,%s,%s,%s,%s"
          .format(wsession.MAC, wsession.stime, wsession.etime, wsession.AP,
            bname, btype, bschool, blat, blon, ipaddr, account)
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


  /**
   * Construct IP sessions identified by IPALLOC and IPRECYCLE messages
   * @param logs
   * @return
   */
  def collectIPSession(logs: Iterable[CleanWIFILog]): Iterable[IPSession] = {
    var sessions = new Array[IPSession](0)
    var IPMap = new HashMap[String, List[Long]]
    var mac: String = null

    logs.foreach( log => {
      mac = log.MAC
      val time = log.time
      val code = log.code
      val ip = log.payload

      if ( code == WIFICode.IPAllocation ) {

        if ( ! IPMap.contains(ip) )
          IPMap.put(ip, List(time, time))
        else {
          val value = IPMap.get(ip).get
          if ( time >= value(1) ) {
            // update session ending time
            IPMap.put(ip, List(value(0), time))
          }
        }

      } else if (code == WIFICode.IPRecycle) {

        if ( IPMap.contains(ip)) {
          val value = IPMap.get(ip).get
          // recycle certain IP
          sessions = sessions :+ IPSession(mac, value(0), time, ip)
          IPMap.remove(ip)
        } else {
          // omit recycling messages without allocation first
        }

      }
    })

    if ( mac != null ){
      IPMap.foreach { case (key, value) => {
        sessions = sessions :+ IPSession(mac, value(0), value(1), key)
      }}
    }

    // adjust session timestamps
    var preSession: IPSession = null
    var ajustedSessions = new Array[IPSession](0)
    sessions.sortBy(m => m.stime).foreach { m => {

      if ( preSession != null ) {
        val tdiff = m.stime - m.etime
        if (tdiff < 0)
          preSession = preSession.copy(etime = m.stime)

        // merge adjacent sessions with the same IP
        if ( preSession.IP == m.IP && tdiff < mergeSessionThreshold )
          preSession = preSession.copy(etime = m.etime)
        else {
          ajustedSessions = ajustedSessions :+ preSession
          preSession = m
        }
      } else {
        preSession = m
      }

    }}

    if (preSession != null)
      ajustedSessions = ajustedSessions :+ preSession

    ajustedSessions.toIterable
  }
}
