package org.apache.spark

import org.apache.spark.deploy.yarn.Client

class TestYarnCluster {
  def main(args: Array[String]): Unit = {
    val args = Array("--jar", "file:/usr/local/spark-2.2.1-bin-2.6.0-cdh5.9.0/examples/jars/spark-examples_2.11-2.2.1.jar",
    "--class", "org.apache.spark.examples.SparkPi")
//    Client(args)
  }
}
