package com.niuwa.streaming.dispatcher

import com.niuwa.streaming.CommitSparkJob

object StrategyDispatcherIT11 {
  def main(args: Array[String]): Unit = {


    val params : java.util.HashMap[String, String] = new java.util.HashMap[String, String]()

    params.put("streaming.master", "local[2]")
    params.put("streaming.name", "god")
    params.put("streaming.job.file.path","classpath://batch-script-df.json")

    CommitSparkJob.getOrCreate.run(params)
  }

}
