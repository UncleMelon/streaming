package com.niuwa.streaming.runtime

import com.niuwa.streaming.dispatcher.Strategy

import scala.collection.mutable


trait StreamingRuntime {

  def startRuntime: StreamingRuntime

  def destroyRuntime(stopGraceful: Boolean, stopContext: Boolean = false): Boolean

  def streamingRuntimeInfo: StreamingRuntimeInfo

  def resetRuntimeOperator(runtimeOperator: RuntimeOperator)

  def configureStreamingRuntimeInfo(streamingRuntimeInfo: StreamingRuntimeInfo)

  def awaitTermination

  def startThriftServer

  def startHttpServer

  def params: Map[Any, Any]

}

trait StreamingRuntimeInfo

trait Event

case class JobFlowGenerate(jobName: String, index: Int, strategy: Strategy[Any]) extends Event

trait PlatformManagerListener {
  def processEvent(event: Event)
}
