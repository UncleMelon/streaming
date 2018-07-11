package com.niuwa.streaming.dispatcher

import java.util.{Map => JMap, List => JList}

trait Compositor[T] {
  def initialize(typeFilters:JList[String],configParams:JList[JMap[Any,Any]])
  def result(alg:JList[Processor[T]],ref:JList[Strategy[T]],middleResult:JList[T],params:JMap[Any,Any]):JList[T]
  def stop = {}
}
