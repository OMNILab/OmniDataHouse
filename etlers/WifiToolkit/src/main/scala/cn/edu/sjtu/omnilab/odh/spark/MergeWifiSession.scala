package cn.edu.sjtu.omnilab.odh.spark

import cn.edu.sjtu.omnilab.odh.rawfilter.{APToBuilding, WIFICode}
import org.apache.commons.lang.StringUtils
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
    val mobilityCodes = List(
      WIFICode.AuthRequest,
      WIFICode.AssocRequest,
      WIFICode.Deauth,
      WIFICode.Disassoc,
      WIFICode.UserRoam,
      WIFICode.NewDev
    )

    val ipAllocCodes = List(
      WIFICode.UserAuth,
      WIFICode.IPAllocation,
      WIFICode.IPRecycle,
      WIFICode.UserRoam
    )

    // Load clean Wifi syslog
    val inputRDD = spark.textFile(input)

      .map{ filtered => {
        // NOTE: there are more than four fields for UserAuth and UserRoam
        val parts = StringUtils.split(filtered, ",", 4)

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
      .filter( m => ipAllocCodes.contains(m.code))
      .groupBy(_.MAC)
      .flatMap { case (key, logs) => { collectIPSession(logs) }}
      .keyBy(_.MAC)
      .cache

    // Filter Auth info. from input
    // If an account is detected only once for a MAC address, this account is
    // associate to that MAC for the whole day.
    // Otherwise, a session-based association is performed after the join action.
    val authRDD = inputRDD.filter(m => m.code == WIFICode.UserAuth)
      .map( m => {
        val accname = m.payload.split(',')(0)
        CleanWIFILog(MAC=m.MAC, time=m.time, code=m.code, payload=accname)
      })
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
      .filter(m => mobilityCodes.contains(m.code))
      .groupBy(_.MAC)
      .flatMap { case (key, logs) => { extractSessions_ex(logs) }}

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

        var bname: String = ""
        var btype: String = ""
        var bschool: String = ""
        var blat: String = ""
        var blon: String = ""
        if ( buildInfo != null ) {
          bname = buildInfo.get(0)
          btype = buildInfo.get(1)
          bschool = buildInfo.get(2)
          blat = buildInfo.get(3)
          blon = buildInfo.get(4)
        }

        var ipaddr: String = ""
        if ( ipsession != null ) {
          ipaddr = ipsession.IP
        }

        var account: String = ""
        if ( authinfo != null ) {
          account = authinfo.payload
        }

        "%s,%d,%d,%s,%s,%s,%s,%s,%s,%s,%s"
          .format(wsession.MAC, wsession.stime, wsession.etime, wsession.AP,
            bname, btype, bschool, blat, blon, ipaddr, account)
    }
      .sortBy(m => m)
      .saveAsTextFile(output)

  }

  /**
   * Extract WIFI association sessions from certain individual's logs
   */
  def extractSessions_ex(logs: Iterable[CleanWIFILog]): Iterable[WIFISession] = {


    /*
     * Based on previous algorithm used in SyslogCleanser project.
     */
    var sessions = new Array[WIFISession](0)
    var APMap = new HashMap[String, List[Long]]
    var preAP: String = null
    var mac: String = null

    logs.toSeq.sortBy(_.time)
      .foreach( log => {

        mac = log.MAC
        val time = log.time
        val code = log.code
        val ap = log.payload.split(',')(0)

        // create ip mapping
        if ( code == WIFICode.AuthRequest || code == WIFICode.AssocRequest ||
          code == WIFICode.UserRoam || code == WIFICode.NewDev) {
          // start a new mobility session with these msgs.
          if ( ! APMap.contains(ap) )
            APMap.put(ap, List(time, time))
          else {
            val value = APMap.get(ap).get
            if (preAP == ap) {
              // update ending time
              if ( time >= value(1) ) {
                APMap.put(ap, List(value(0), time))
              }
            } else {
              // create new session
              sessions = sessions :+ WIFISession(mac, value(0), value(1), ap)
              APMap.put(ap, List(time, time))
            }
          }
        } else if (code == WIFICode.Deauth || code == WIFICode.Disassoc) {
          if ( APMap.contains(ap)) {
            val value = APMap.get(ap).get
            sessions = sessions :+ WIFISession(mac, value(0), time, ap)
            APMap.remove(ap)
          }
        }

        preAP = ap

    })

    if ( mac != null ){
      APMap.foreach { case (key, value) => {
        sessions = sessions :+ WIFISession(mac, value(0), value(1), key)
      }}
    }

    // adjust session timestamps
    var preSession: WIFISession = null
    var ajustedSessions = new Array[WIFISession](0)

    sessions.sortBy(m => m.stime).foreach { m => {
      if ( preSession != null ) {
        val tdiff = m.stime - preSession.etime
        if (tdiff < 0)
          preSession = preSession.copy(etime = m.stime)

        // merge adjacent sessions with the same IP
        if ( preSession.AP == m.AP && tdiff < mergeSessionThreshold )
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

    ajustedSessions.sortBy(m => m.stime).toIterable

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
      // extract ip info
      mac = log.MAC
      val time = log.time
      val code = log.code
      val ip = code match {
        case WIFICode.UserAuth | WIFICode.UserRoam => {
          val parts = log.payload.split(',')
          val part_ip = parts(1)
          if ( ! List("null", "0.0.0.0", "255.255.255.255").contains(part_ip))
            part_ip
          else
            null
        }
        case WIFICode.IPAllocation | WIFICode.IPRecycle => log.payload
        case _ => null
      }

      // create ip mapping
      if (ip != null){
        if ( code == WIFICode.IPAllocation ||
              code == WIFICode.UserAuth ||
              code == WIFICode.UserRoam) {
          // start an IP allocation session with these msgs.
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
          // terminate an IP allocation session with recycle msg.
          if ( IPMap.contains(ip)) {
            val value = IPMap.get(ip).get
            sessions = sessions :+ IPSession(mac, value(0), time, ip)
            IPMap.remove(ip)
          }
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
        val tdiff = m.stime - preSession.etime
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

    ajustedSessions.sortBy(m => m.stime).toIterable
  }
}
