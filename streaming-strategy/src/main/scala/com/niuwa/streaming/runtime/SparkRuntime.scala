package com.niuwa.streaming.runtime

import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicReference
import java.util.{Map => JMap}

import com.niuwa.streaming.runtime.ParamsHelper._
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._

/**
  * 5/11/16 WilliamZhu(allwefantasy@gmail.com)
  */
class SparkRuntime(_params: JMap[Any, Any]) extends StreamingRuntime with SparkPlatformHelper with PlatformManagerListener {
  self =>


  def name = "SPARK"

  var sparkSession: SparkSession = createRuntime

  var sparkRuntimeInfo = new SparkRuntimeInfo()

  def createRuntime = {
    val conf = new SparkConf()
    params.filter(f => f._1.toString.startsWith("spark.")).foreach { f =>
      conf.set(f._1.toString, f._2.toString)
    }

    if (params.containsKey("streaming.master")) {
      conf.setMaster(params.get("streaming.master").toString)
    }

    conf.setAppName(params.get("streaming.name").toString)

    SparkSession.builder().config(conf).getOrCreate()
  }


  params.put("_session_", sparkSession)

//  registerUDF

  def registerUDF = {
    Class.forName("streaming.core.compositor.spark.udf.func.Functions").getMethods.foreach { f =>
      try {
        if (Modifier.isStatic(f.getModifiers)) {
          f.invoke(null, SQLContextHolder.sqlContextHolder.getOrCreate().udf)
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }

  }

  override def startRuntime: StreamingRuntime = {
    this
  }

  override def awaitTermination: Unit = {
    if (params.paramAsBoolean("streaming.spark.service", false)) {
      Thread.currentThread().join()
    }
  }

  override def streamingRuntimeInfo: StreamingRuntimeInfo = sparkRuntimeInfo

  override def destroyRuntime(stopGraceful: Boolean, stopContext: Boolean): Boolean = {
    sparkSession.stop()
    SparkRuntime.clearLastInstantiatedContext()
    true
  }


  override def configureStreamingRuntimeInfo(streamingRuntimeInfo: StreamingRuntimeInfo): Unit = {}

  override def resetRuntimeOperator(runtimeOperator: RuntimeOperator): Unit = {

  }

  override def params: JMap[Any, Any] = _params

  override def processEvent(event: Event): Unit = {}

  SparkRuntime.setLastInstantiatedContext(this)

  override def startThriftServer: Unit = {
//    HiveThriftServer3.run(SQLContextHolder.sqlContextHolder.hiveContextRef.get())
  }

  override def startHttpServer: Unit = {}
}

class SparkRuntimeInfo extends StreamingRuntimeInfo {

}


object SparkRuntime {


  private val INSTANTIATION_LOCK = new Object()

  /**
    * Reference to the last created SQLContext.
    */
  @transient private val lastInstantiatedContext = new AtomicReference[SparkRuntime]()

  /**
    * Get the singleton SQLContext if it exists or create a new one using the given SparkContext.
    * This function can be used to create a singleton SQLContext object that can be shared across
    * the JVM.
    */
  def getOrCreate(params: JMap[Any, Any]): SparkRuntime = {
    INSTANTIATION_LOCK.synchronized {
      if (lastInstantiatedContext.get() == null) {
        new SparkRuntime(params)
      }
    }
//    PlatformManager.getOrCreate.register(lastInstantiatedContext.get())
    lastInstantiatedContext.get()
  }

  private def clearLastInstantiatedContext(): Unit = {
    INSTANTIATION_LOCK.synchronized {
//      PlatformManager.getOrCreate.unRegister(lastInstantiatedContext.get())
      lastInstantiatedContext.set(null)
    }
  }

  private def setLastInstantiatedContext(sparkRuntime: SparkRuntime): Unit = {
    INSTANTIATION_LOCK.synchronized {
      lastInstantiatedContext.set(sparkRuntime)
    }
  }
}
