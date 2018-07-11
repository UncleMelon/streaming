package com.niuwa.streaming.dispatcher

object StrategyDispatcherIT {
  def main(args: Array[String]): Unit = {
    val jobConfigStr = ""
    val defaultShortNameMapping = new DefaultShortNameMapping;
    StrategyDispatcher.getOrCreate(jobConfigStr, defaultShortNameMapping)
  }

}
