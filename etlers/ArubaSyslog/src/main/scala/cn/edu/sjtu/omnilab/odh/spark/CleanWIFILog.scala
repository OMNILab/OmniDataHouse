package cn.edu.sjtu.omnilab.odh.spark


case class CleanWIFILog(MAC: String, time: Long, code: Int, payload: String) // payload as AP or IP
