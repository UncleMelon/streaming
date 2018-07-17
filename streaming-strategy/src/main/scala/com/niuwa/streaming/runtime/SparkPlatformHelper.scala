package com.niuwa.streaming.runtime

import org.apache.spark.SparkContext

import scala.collection.JavaConversions._

/**
  * Created by allwefantasy on 20/3/2017.
  */
trait SparkPlatformHelper {
  def getRuntimeContext(runtime: StreamingRuntime): SparkContext = {

    runtime match {
      case s2: SparkRuntime => s2.sparkSession.sparkContext
      case _ =>
        Class.forName(runtime.getClass.getName).
          getMethod("sparkContext").
          invoke(runtime).asInstanceOf[SparkContext]
    }

  }

  def createSQLContextHolder(params: java.util.Map[Any, Any], runtime: StreamingRuntime) = {

    val sc = getRuntimeContext(runtime)

    if (params.containsKey("streaming.enableCarbonDataSupport")
      && params.get("streaming.enableCarbonDataSupport").toString.toBoolean
    ) {

      val hiveOption = Map(
        "className" -> "org.apache.spark.sql.CarbonContext",
        "store" -> params.getOrElse("streaming.carbondata.store", "").toString,
        "meta" -> params.getOrElse("streaming.carbondata.meta", "").toString

      )
      new SQLContextHolder(
        true, sc, Some(hiveOption))

    } else {

      new SQLContextHolder(
        params.containsKey("streaming.enableHiveSupport") &&
          params.get("streaming.enableHiveSupport").toString.toBoolean, sc, None)
    }

  }
}
