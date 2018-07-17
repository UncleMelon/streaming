package com.niuwa.streaming

import java.util.concurrent.atomic.AtomicReference
import java.util.{Map => JMap}

import com.niuwa.streaming.dispatcher.{Dispatcher, StrategyDispatcher}
import com.niuwa.streaming.runtime.{SparkRuntime, StreamingRuntime}

import scala.collection.JavaConversions._

class CommitSparkJob {

  self =>

  val config = new AtomicReference[JMap[String, String]]()

  def findDispatcher: StrategyDispatcher[Any] = {
    Dispatcher.dispatcher(Dispatcher.contextParams(""))
  }

  def run(_params: JMap[String, String] ): Unit = {
    config.set(_params)

    val params = config.get()

    val tempParams = new java.util.HashMap[Any, Any]()
    params.filter(f => f._1.startsWith("streaming.")).foreach { f => tempParams.put(f._1, f._2) }

    val dispatcher = findDispatcher

    var jobs: Array[String] = dispatcher.strategies/*.filter(f => f._2.isInstanceOf[JobStrategy])*/.keys.toArray

    jobs.foreach( jobName =>
      //执行具体的job
      dispatcher.dispatch(Dispatcher.contextParams(jobName))
    )
  }


  CommitSparkJob.setInstaintiatedContext(self)

}

object CommitSparkJob {

  /**
    * singleton lock
    */
  private val INSTAINTIATION_LOCK = new Object()

  /**
    *
    */
  private val lastInstaintiatedContext = new AtomicReference[CommitSparkJob]()

  def getOrCreate: CommitSparkJob = {
    INSTAINTIATION_LOCK.synchronized {
      if (lastInstaintiatedContext.get() == null) {
        new CommitSparkJob()
      }
    }
    lastInstaintiatedContext.get()
  }



  private def setInstaintiatedContext(commitSparkJob: CommitSparkJob) : Unit = {
    INSTAINTIATION_LOCK.synchronized {
      lastInstaintiatedContext.set(commitSparkJob)
    }
  }


  def getRuntime: StreamingRuntime = {
    val params: JMap[String, String] = getOrCreate.config.get()
    val tempParams: JMap[Any, Any] = params.map(f => (f._1.asInstanceOf[Any], f._2.asInstanceOf[Any]))
    val runtime = SparkRuntime.getOrCreate(tempParams)
    runtime
  }

}


