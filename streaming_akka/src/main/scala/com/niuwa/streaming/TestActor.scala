package com.niuwa.streaming

import akka.actor.{Actor, ActorLogging}
import com.niuwa.streaming.TestActor.{Bean, CreateView}
import com.niuwa.streaming.spark.utils.UDFUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.util.Random

class TestActor(sparkConf: SparkConf) extends Actor with ActorLogging {
  var sparkSession: SparkSession = _
  override def preStart(): Unit = {
    sparkSession = SparkSession.builder().enableHiveSupport().config(sparkConf).getOrCreate()
    UDFUtils.registerUDF(sparkSession)
    /*UDFUtils.registerSparkUDF("com.niuwa.streaming.spark.udf.Functions", sparkSession)*/
  }

  override def postStop(): Unit = sparkSession.stop()

  override def receive: Receive = {
    case CreateView(limit) =>
      log.info(s"exec createview")
      sparkSession.createDataFrame(getBeans(limit)).createOrReplaceTempView("test1")
    case s:String =>
      log.info(s"exec $s")
      sparkSession.sql(s).show(1000)
    case e: Any => println(e)
  }

  val ids = 1 to 100
  val names = ('a' to 'z').map(_.toString)
  val beans = for {
    id <- ids
    name <- names
  } yield Bean(id, name)

  def getBeans(limit: Int) = {
    Random.shuffle(beans).take(limit)
  }
}

object TestActor {
  sealed trait DeployMessage extends Serializable
  case class CreateView(limit: Int) extends DeployMessage
  case class Bean(id: Int, name: String)
}
