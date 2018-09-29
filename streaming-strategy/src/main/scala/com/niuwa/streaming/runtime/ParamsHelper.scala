package com.niuwa.streaming.runtime


/**
 * 5/14/16 WilliamZhu(allwefantasy@gmail.com)
 */
object ParamsHelper {
  implicit def mapToParams(_params: Map[Any, Any]):Params = {
    new Params(_params)
  }
}

class Params(_params: Map[Any, Any]) {
  def paramAsInt(key: String, defaultValue: Int = 0) = {
    if(_params.contains(key)){
      _params.get(key).toString.toInt
    }else
    defaultValue
  }

  def paramAsDouble(key: String, defaultValue: Double = 0) = {
    if(_params.contains(key)){
      _params.get(key).toString.toDouble
    }else
      defaultValue

  }

  def param(key: String, defaultValue: String = null) = {
    if(_params.contains(key)){
      _params.get(key).toString
    }else
      defaultValue

  }

  def paramAsBoolean(key: String, defaultValue: Boolean = false) = {
    if(_params.contains(key)){
      _params.get(key).toString.toBoolean
    }else
      defaultValue
  }

}

