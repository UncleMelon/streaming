package com.niuwa.streaming.spark.udf

import org.apache.spark.sql.SparkSession

object Functions {

  def columnLength(sparkSession: SparkSession): Unit = {
    sparkSession.udf.register("column_length", (column: String) => column.length)
  }
}
