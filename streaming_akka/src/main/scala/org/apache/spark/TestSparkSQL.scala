package org.apache.spark

import org.apache.spark.sql.SparkSession

/**
  *  windows7远程访问hive metadata thrift服务，需要设置本地Hadoop_HOME
  * 或者设置System.setProperty("hadoop.home.dir", "D:\\hadoop-common-2.6.0-bin")
  */
object TestSparkSQL {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("spark sql test").setMaster("local")
    val spark = SparkSession.builder().enableHiveSupport().config(sparkConf).getOrCreate()
    spark.sql("show databases").collect().foreach(println)
    spark.sql("select * from rds.jxl_primary_info").collect().foreach(println)
  }
}
