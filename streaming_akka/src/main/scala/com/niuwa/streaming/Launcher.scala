package com.niuwa.streaming

import akka.actor.{ActorSystem, Props}
import org.apache.spark.SparkConf

object Launcher {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("testAkka")/*.setMaster("local[2]")*/
    val actorSystem = ActorSystem("testSystem")
    actorSystem.actorOf(Props(new TestActor(sparkConf)), "actor1")
    /*actorSystem.actorOf(Props(new TestActor(sparkConf)), "actor2")*/
  }
}
