package cn.edu.sjtu.omnilab.odh.utils

/**
 * Data Schema of SJTU HTTP logs
 */
object HttpSchema {

  val source_ip = 0
  val source_port = 1
  val dest_ip = 2
  val dest_port = 3
  val conn = 4
  val conn_ts = 5
  val close_ts = 6
  val conn_dur = 7
  val idle_time0 = 8
  val request_ts = 9
  val request_dur = 10
  val response_ts = 11
  val response_dur_b = 12
  val response_dur_e = 13
  val idle_time1 = 14
  val request_size = 15
  val response_size = 16
  val request_method = 17
  val request_url = 18
  val request_protocol = 19
  val request_host = 20
  val request_user_agent = 21
  val request_referrer = 22
  val request_conn = 23
  val request_keep_alive = 24
  val response_protocol = 25
  val response_code = 26
  val response_server = 27
  val response_clen = 28
  val response_ctype = 29
  val response_cenc = 30
  val response_etag = 31
  val response_cache_ctl = 32
  val response_last_mod = 33
  val response_age = 34
  val response_expire = 35
  val response_connval  = 36
  val response_keep_alive = 37

}
