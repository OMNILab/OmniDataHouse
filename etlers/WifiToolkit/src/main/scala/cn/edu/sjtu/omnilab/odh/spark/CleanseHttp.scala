package cn.edu.sjtu.omnilab.odh.spark


import cn.edu.sjtu.omnilab.odh.utils.{HttpSchema}
import org.apache.spark.{SparkContext, SparkConf}


object CleanseHttp {

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      println("Usage: CleanseHttp <in> <out>")
      sys.exit(-1)
    }

    val input = args(0)
    val output = args(1)

    val conf = new SparkConf()
    conf.setAppName("Cleanse HTTP logs")
    val spark = new SparkContext(conf)

    spark.textFile(input)

      .map( m => {
        val fields = splitLog(m)

        if ( fields == null ) {
          null
        } else {
          // Select required fields
          "%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s".format(

            // connection info
            fields(HttpSchema.source_ip),
            formatInt(fields(HttpSchema.source_port)),
            fields(HttpSchema.dest_ip),
            formatInt(fields(HttpSchema.dest_port)),
            fields(HttpSchema.conn),

            // connection timing metrics
            formatDouble(fields(HttpSchema.conn_ts)),
            formatDouble(fields(HttpSchema.close_ts)),
            formatDouble(fields(HttpSchema.conn_dur)),
            formatDouble(fields(HttpSchema.idle_time0)),
            formatDouble(fields(HttpSchema.request_ts)),
            formatDouble(fields(HttpSchema.request_dur)),
            formatDouble(fields(HttpSchema.response_ts)),
            formatDouble(fields(HttpSchema.response_dur_b)),
            formatDouble(fields(HttpSchema.response_dur_e)),
            formatDouble(fields(HttpSchema.idle_time1)),

            // request/response sizes
            formatInt(fields(HttpSchema.request_size)),
            formatInt(fields(HttpSchema.response_size)),

            // request keywords
            fields(HttpSchema.request_method),
              formatString(fields(HttpSchema.request_url)),
            formatString(fields(HttpSchema.request_host)),
            formatString(fields(HttpSchema.request_user_agent)),
            formatString(fields(HttpSchema.request_referrer)),

            // response keywords
            formatInt(fields(HttpSchema.response_code)),
            formatString(fields(HttpSchema.response_server)),
            formatString(fields(HttpSchema.response_ctype))
          )
        }
    })

      .filter(m => m != null)

    .saveAsTextFile(output)

  }

  /**
   * Split each http log entry into fields.
   * Null fields will be empty string.
   *
   * @param line a single entry of HTTP log
   * @return an array of separated parts
   */
  def splitLog(line: String): Array[String] = {
    // get HTTP header fields
    val chops = line.split("""\"\s\"""");
    if ( chops.length != 21 )
      return null

    // get timestamps
    val timestamps = chops(0).split(" ");
    if (timestamps.length != 18 )
      return null

    val results = timestamps ++ chops.slice(1, 21)

    // remove N/A values and extrat quote
    results.transform( field => {
      var new_field = field.replaceAll("\"", "")
      if (new_field == "N/A")
        new_field = ""
      new_field
    })

    results
  }


  def formatDouble(field: String): String = {
    try {
      "%.3f".format(field.toDouble)
    } catch {
      case e: Exception => ""
    }
  }


  def formatInt(field: String): String = {
    try {
      "%d".format(field.toInt)
    } catch {
      case e: Exception => ""
    }
  }


  def formatString(field: String): String = {
    try {
      field.replaceAll("|", "")
    } catch {
      case e: Exception => field
    }
  }

}
