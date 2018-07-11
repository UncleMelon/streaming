package com.niuwa.streaming.dispatcher

import java.util.{Map => JMap, List => JList}

trait Processor[T] {
  def initialize(name:String,params:JList[JMap[Any,Any]])
  def result(params:JMap[Any,Any]):JList[T]
  def name():String
  def stop = {}
}
