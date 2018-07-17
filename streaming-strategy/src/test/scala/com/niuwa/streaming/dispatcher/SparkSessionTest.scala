package com.niuwa.streaming.dispatcher

import org.apache.spark.sql.SparkSession

object SparkSessionTest {
  def main(args: Array[String]): Unit = {

     val spark  = SparkSession
       .builder()
       .appName("sparkTest")
       .master("local[2]")
       .getOrCreate()

    val tt = spark.read.format("json").load("D://example.json")
    tt.show()


  }

}
