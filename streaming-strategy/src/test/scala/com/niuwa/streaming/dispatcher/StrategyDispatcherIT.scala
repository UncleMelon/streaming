package com.niuwa.streaming.dispatcher

import scala.collection.JavaConversions._

object StrategyDispatcherIT {
  def main(args: Array[String]): Unit = {

    val defaultShortNameMapping = new DefaultShortNameMapping

    val jobFilePath = "classpath://batch-script-df.json"
    val cleanJobFilePath = jobFilePath.substring("classpath://".length)
    val jobConfigStr = scala.io.Source.fromInputStream(StrategyDispatcherIT.getClass.getResourceAsStream(cleanJobFilePath)).getLines().
      mkString("\n")
    println(jobConfigStr)

    val params  = Map[Any, Any]()

    params.put("_client_", "batch-console")

    val dispatcher = StrategyDispatcher.getOrCreate(jobConfigStr, defaultShortNameMapping)


    var jobs: Array[String] = dispatcher.strategies.keys.toArray


    params.put("-streaming.master", "local[2]")
    params.put("-streaming.name", "god")

    jobs.foreach {
      jobName =>

        dispatcher.dispatch(params)
    }




  }

}
