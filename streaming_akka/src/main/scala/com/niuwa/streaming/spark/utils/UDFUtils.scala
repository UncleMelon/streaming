package com.niuwa.streaming.spark.utils

import java.io.{File, FileReader}
import java.lang.reflect.Modifier
import java.util.Properties

import org.apache.hadoop.hive.ql.exec.{UDAF, UDF}
import org.apache.logging.log4j.scala.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

object UDFUtils {

  private val logger = Logger(this.getClass)

  def registerSparkUDF(clazz: String, sparkSession: SparkSession) = {
    logger.info("register functions.....")
    Class.forName(clazz).getMethods.foreach( f => {
      try{
        if (Modifier.isStatic(f.getModifiers)) {
          logger.info(f.getName)
          f.invoke(null, sparkSession)
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    })
  }

  val UDF_PROPERTIES = "/root/udf.properties"

  def registerUDF(sparkSession: SparkSession): Unit = {
    Option(new File(UDF_PROPERTIES))
      .filter(_.exists())
      .map{ input =>
        val properties = new Properties()
        properties.load(new FileReader(input))
        properties
      }.foreach(props => doRegister(props, sparkSession))
  }


  def doRegister(properties: Properties, sparkSession: SparkSession) = {
    properties.asScala.foreach {
      case (name, className) =>
        val clazz = Class.forName(className)
        if (classOf[UserDefinedAggregateFunction].isAssignableFrom(clazz)) {
          logger.info(s"register udaf $className#$name")
          sparkSession.udf.register(name, clazz.newInstance().asInstanceOf[UserDefinedAggregateFunction])
        } else if (classOf[UDAF].isAssignableFrom(clazz) || classOf[UDF].isAssignableFrom(clazz)) {
          registerHiveUDFandUDAF(className, name, sparkSession)
        } else {
          logger.info(s"register udf $className#$name")
          try {
//            sparkSession.udf.registerJava(name, className, null)
          } catch {
            case NonFatal(e) =>
              logger.error("in reister java udf, throw", e)
          }
        }
    }
  }

  def checkProps(properties: Properties): Unit = {
    properties.keys().asScala.map(_.toString).toList.groupBy(x => x).mapValues(_.size)
  }

  private def registerHiveUDFandUDAF(className: String, funcName: String, sparkSession: SparkSession) = {
    try {
      val sqlText = s"create temporary $funcName as '$className'"
      sparkSession.sql(sqlText)
    } catch  {
      case NonFatal(e) =>
        logger.error("in register hive udaf, throw", e)
    }
  }

}

