package com.niuwa.streaming.dispatcher


object Dispatcher {
  def dispatcher(contextParams: Map[Any, Any]): StrategyDispatcher[Any] = {
    val defaultShortNameMapping = new DefaultShortNameMapping()

    if (contextParams != null && contextParams.contains("streaming.job.file.path")) {

      val jobFilePath = contextParams.getOrElse("streaming.job.file.path", "").asInstanceOf[String]

      var jobConfigStr = "{}"

      if (jobFilePath.startsWith("classpath://")) {
        val cleanJobFilePath = jobFilePath.substring("classpath://".length)
        jobConfigStr = scala.io.Source.fromInputStream(
          Dispatcher.getClass.getResourceAsStream(cleanJobFilePath)).getLines().
          mkString("\n")
      } else {
//        jobConfigStr = HDFSOperator.readFile(jobFilePath)
      }

      if (jobConfigStr == null || jobConfigStr.isEmpty)
        jobConfigStr = "{}"

      StrategyDispatcher.getOrCreate(jobConfigStr, defaultShortNameMapping)
    } else {
      StrategyDispatcher.getOrCreate(null, defaultShortNameMapping)
    }

  }
}
