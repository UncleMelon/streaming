package com.niuwa.streaming.core.compositor.spark.output

import org.apache.spark.sql.SQLContext

/**
  * Created by allwefantasy on 15/7/2017.
  */
trait OutputWriter {
  def write(sQLContext: SQLContext, contextParams: Map[Any, Any], config: Map[String, String]): Unit
}
