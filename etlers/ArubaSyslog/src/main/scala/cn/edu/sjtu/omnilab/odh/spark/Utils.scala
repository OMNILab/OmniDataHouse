package cn.edu.sjtu.omnilab.odh.spark

import com.google.common.net._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.collection.immutable.HashMap


object Utils {

  /**
   * Parse integer value from a string
   * @param s a string
   * @return an Some(Int) or None
   */
  def parseLong(s: String, default: Long): Long = {
    try {
      s.toLong
    } catch {
      case e: Exception => default
    }
  }

  /**
   * Parse double value from a string
   * @param s a string
   * @return an Some(Double) or None
   */
  def parseDouble(s: String, default: Double): Double = {
    try {
      s.toDouble
    } catch {
      case e: Exception => default
    }
  }

  /**
   * Remove params of a URL string
   * @param s a URL string
   * @return a clean URL without params
   */
  def stripURLParams(s: String): String = {
    if (s == null) return null

    val pattern = """^(?:\w+://)?([^\?&]+)\??""".r
    val matched = pattern.findFirstMatchIn(s)
    matched match {
      case Some(m) => m.group(1)
      case None => s
    }
  }

  /**
   * Remove protocol prefix of a URL string
   * @param s a URL string
   * @return a new string without prefix
   */
  def stripURLSchema(s: String): String = {
    if (s == null) return null

    val pattern = """^(?:\w+:?//)?(.*)$""".r
    val matched = pattern.findFirstMatchIn(s)
    matched match {
      case Some(m) => m.group(1)
      case None => s
    }
  }

  /**
   * Check if a given URL takes schema "http://"
   * @param url
   * @return
   */
  def hasURLSchema(url: String): Boolean = {
    if (url == null) return false
    val pattern = """^(\w+:?//).*""".r
    pattern.findFirstIn(url).nonEmpty
  }

  /**
   * Remove protocol prefix and params of a URL string
   * @param s
   * @return
   */
  def stripURL(s: String): String = {
    if (s == null) return null
    stripURLSchema(stripURLParams(s))
  }

  /**
   * Concatenate Host and URI fileds in HTTP header into a URL address.
   * @param host
   * @param uri
   */
  def combineURL(host: String, uri: String): String = {
    var new_url = uri
    if (!hasURLSchema(uri))
      new_url = host + uri
    new_url
  }

  /**
   * Get host name from a given Host or URL string
   * @param url a Host or URL string, e.g. http://baidu.com/index.html
   * @return the host name, e.g. baidu.com
   */
  def getHost(url: String): String = {
    if (url == null) return null

    val pattern = """^(?:\w+:?//)?([^:\/\?&]+)""".r
    val matched = pattern.findFirstMatchIn(url)
    matched match {
      case Some(m) => m.group(1)
      case None => null
    }
  }

  /**
   * Extract parameters from given URL string
   * @param url
   * @return
   */
  def getURLParams(url: String): Map[String, String] = {
    if (url == null) return null

    val mainUrlPattern = """^(?:\w+://)?(?:[^\?&]+)(\?.*)?$""".r
    val urlParamPattern = """([^?=&]+)=([^?&=]+)""".r

    // do param extraction
    def _extractParams(s: String): Map[String, String] = {
      val result = new HashMap[String, String]
      urlParamPattern.findAllMatchIn(s).foreach{
        case m => result.updated(m.group(1), m.group(2))
      }
      result
    }

    val mainUrlMatched = mainUrlPattern.findFirstMatchIn(url)
    mainUrlMatched match {
      case Some(m) => _extractParams(m.group(1))
      case None => null
    }
  }

  /**
   * Get the top-level domain from URL.
   * For example, given url "www.baidu.com", this UDF returns "baidu.com" as results.
   *
   * @param url
   * @return
   */
  def getTopPrivateDomain(url: String): String = {
    if (url == null) return null

    val host = getHost(url)
    try {
      val name = InternetDomainName.from(host).topPrivateDomain.toString
      // NB: there is a compiling error with `name()` method
      val pattern = """name=([^=\\{\\}]+)""".r
      pattern.findFirstMatchIn(name).get.group(1)
    } catch {
      case e: Exception => host
    }
  }

  /**
   *  Detect the mobile OS name given the User Agent string.
   *  "unknown" is returned if not identified.
   *
   * @param user_agent
   * @return
   */
  def getMobileName(user_agent: String): String = {
    if (user_agent == null) return "unknown"

    val pattern = """android|(bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|docomo|dolfin|dolphin|elaine|fennec|hiptop|iemobile|(hpw|web)os|htc( touch)?|ip(hone|od|ad)|iris|j2me|kindle( fire)?|lge |maemo|midp|minimo|mmp|netfront|nokia|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|playstation|pocket|portalmmm|psp|series(4|6)0|symbian|silk-accelerated|skyfire|sonyericsson|treo|tablet|touch(pad)?|up\\.(browser|link)|vodafone|wap|webos|windows (ce|phone)|wireless|xda|xiino|zune""".r
    val matched = pattern.findFirstMatchIn(user_agent)
    matched match {
      case Some(m) => m.group(0)
      case None => "unknown"
    }
  }

  val timeFormatString = ""

  /**
   * Converts ISO8601 datetime strings to Unix Time Longs (milliseconds)
   * @param isotime
   * @return
   */
  def ISOToUnix(isotime: String): Long = {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    fmt.parseDateTime(isotime).getMillis
  }

  /**
   * Converts Unix Time Longs (milliseconds) to ISO8601 datetime strings
   * @param unixtime
   * @return
   */
  def UnixToISO(unixtime: Long): String = {
    new DateTime(unixtime).formatted("yyyy-MM-dd HH:mm:ss")
  }

}
