package com.niuwa.streaming.dispatcher

import com.niuwa.streaming.SparkJobLauncher

object StrategyDispatcherIT11 {

  def main(args: Array[String]): Unit = {

    var params = Map[String, String]()

    params += ("streaming.master" -> "local[2]")
    params += ("streaming.name" -> "god")
    params += ("streaming.job.file.path" -> "classpath://batch-script-df.json")

    SparkJobLauncher.builder().config(params).getOrCreate.run()
  }

}
