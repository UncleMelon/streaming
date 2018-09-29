package com.niuwa.streaming

import java.util.concurrent.atomic.AtomicReference

import com.niuwa.streaming.dispatcher.{Dispatcher, StrategyDispatcher}
import com.niuwa.streaming.runtime.{SparkRuntime, StreamingRuntime}

import scala.collection.JavaConversions._
import scala.collection.mutable

class SparkJobLauncher(_params: mutable.HashMap[String, String]) {

  self =>

  var config = Map[Any, Any]()

  paramsInit(_params)

  SparkJobLauncher.setInstaintiatedContext(self)

  def run(): Unit = {

    val dispatcher = findDispatcher

    val jobs: Array[String] = dispatcher.strategies.keys.toArray

    jobs.foreach( jobName => {
      //执行具体的job
      addContextValue("_client_" , jobName)
      dispatcher.dispatch(config)
    })
  }

  def paramsInit(_params: mutable.HashMap[String, String]): Unit = {
    _params.filter(f => f._1.startsWith("streaming.")).foreach { f =>  addContextValue(f._1, f._2) }
  }

  def findDispatcher: StrategyDispatcher[Any] = {
    addContextValue("_runtime_" , getRuntime)
    Dispatcher.dispatcher(config)
  }

  def getRuntime: StreamingRuntime = {
    val runtime = SparkRuntime.getOrCreate(config)
    config = runtime.params
    runtime
  }

  def addContextValue(value: (Any, Any)) : Map[Any, Any] = {
    config += value
    config
  }

}

object SparkJobLauncher {

  class Builder {

    val options = new mutable.HashMap[String, String]()

    def config(map: Map[String, String]): Builder = {
      options ++= map
      this
    }

    def getOrCreate: SparkJobLauncher = {
      INSTAINTIATION_LOCK.synchronized {
        if (lastInstaintiatedContext.get() == null) {
          new SparkJobLauncher(options)
        }
      }
      lastInstaintiatedContext.get()
    }
  }

  def builder(): Builder = new Builder

  /**
    * singleton lock
    */
  private val INSTAINTIATION_LOCK = new Object()

  /**
    * atomic reference for concurrent threads.
    */
  private val lastInstaintiatedContext = new AtomicReference[SparkJobLauncher]()


  private def setInstaintiatedContext(sparkJobLauncher: SparkJobLauncher) : Unit = {
    INSTAINTIATION_LOCK.synchronized {
      lastInstaintiatedContext.set(sparkJobLauncher)
    }
  }

}


