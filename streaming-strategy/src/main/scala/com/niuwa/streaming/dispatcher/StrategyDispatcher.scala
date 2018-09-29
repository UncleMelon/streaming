package com.niuwa.streaming.dispatcher

import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.{UUID, List => JList, Map => JMap}

import com.alibaba.fastjson.JSON
import com.google.inject.Singleton
import com.niuwa.streaming.core.strategy.SparkStreamingStrategy
import org.apache.logging.log4j.scala.Logger

import scala.collection.JavaConversions._

@Singleton
class StrategyDispatcher[T] {
  self =>
  private val _strategies = new ConcurrentHashMap[String, Strategy[T]]()
  private val logger = Logger(classOf[StrategyDispatcher[T]])

  private var _config: JMap[String, JMap[_, _]] = new java.util.HashMap[String, JMap[_, _]]()

  def strategies = _strategies

  def dispatch(_params: Map[Any, Any]): JList[T] = {
    val clientType: String = (if (_params.contains("_client_")) _params.getOrElse("_client_", "app")).asInstanceOf[String]

    var params = _params
    params += ("_cache_" ->  new util.HashMap[Any, Any]())
    params += ("_token_" -> (if (params.contains("_token_")) params.getOrElse("_token_", UUID.randomUUID().getMostSignificantBits() + "")))

    findStrategies(clientType) match {
      case Some(strategies) =>

        val result = new util.ArrayList[T]()

        try {
          result.addAll(strategies.flatMap {
            f =>
              val time = System.currentTimeMillis()
              val res = f.result(params)
              logger.info( s"""${params.get("_token_")} ${f.name} ${System.currentTimeMillis() - time}""")
              res
          })
        } catch {
          case e: Exception => logger.error("调用链路异常", e)
        }
        result

      case None => List()

    }
  }

  def findStrategies(key: String): Option[List[Strategy[T]]] = {
    Option(List(_strategies.get(key)))
  }

  def reload(configStr: String) = {
    synchronized {
      _strategies.foreach(_._2.stop)
      loadConfig(configStr)
    }
  }

  private var shortNameMapping: ShortNameMapping = new ShortNameMapping {
    override def forName(shortName: String): String = shortName
  }

  def configShortNameMapping(mapping: ShortNameMapping) = {
    shortNameMapping = mapping
  }

  def loadConfig(configStr: String) = {
    if (configStr != null) {
      _config = JSON.parseObject(configStr).asInstanceOf[JMap[String, JMap[_, _]]]
    } else {
//      _config = JSON.parseObject(new Environment(settings).resolveConfigAndLoadToString(settings.get("application.strategy.config.file", "strategy.v2.json"))).asInstanceOf[JMap[String, JMap[_, _]]]
    }
    load
  }

  private def load = {
    _config.foreach {
      f =>
        createStrategy(f._1, f._2)
    }
  }

  def createStrategy(name: String, desc: JMap[_, _]): Option[Strategy[T]] = {
    if (_strategies.contains(name)) return None;

    require(desc.containsKey("strategy"), s"""$name 必须包含 strategy 字段。该字段定义策略实现类""")

    val strategy = Class.forName(shortNameMapping.forName(desc.get("strategy").asInstanceOf[String])).newInstance().asInstanceOf[SparkStreamingStrategy[T]]
    val configParams: JMap[Any, Any] = if (desc.containsKey("configParams")) desc.get("configParams").asInstanceOf[JMap[Any, Any]] else new java.util.HashMap()
    strategy.initialize(name, createAlgorithms(desc), createRefs(desc), createCompositors(desc), configParams)
    _strategies.put(name, strategy)
    Option(strategy)

  }

  /*
    创建算法。一个策略由0个或者多个算法提供结果
   */
  private def createAlgorithms(desc: JMap[_, _]): JList[Processor[T]] = {
    if (!desc.containsKey("algorithm") && !desc.containsKey("processor")) return List()
    val rs = if (desc.containsKey("algorithm")) desc.get("algorithm") else desc.get("processor")
    rs.asInstanceOf[JList[JMap[_, _]]].map {
      alg =>
        val name = alg.get("name").asInstanceOf[String]
        val temp = Class.forName(shortNameMapping.forName(name)).newInstance().asInstanceOf[Processor[T]]
        val configParams: JList[JMap[Any, Any]] = if (alg.containsKey("params")) alg.get("params").asInstanceOf[JList[JMap[Any, Any]]] else new java.util.ArrayList[JMap[Any, Any]]()
        temp.initialize(name, configParams)
        temp
    }
  }

  /*
    创建策略。一个策略允许混合包括算法，其他策略提供的结果。
   */
  private def createRefs(desc: JMap[_, _]): JList[Strategy[T]] = {
    val result = new java.util.ArrayList[Strategy[T]]()
    if (!desc.containsKey("ref")) return result
    desc.get("ref").asInstanceOf[JList[String]].foreach {
      ref =>
        if (_strategies.contains(_config.get(ref))) {
          result.add(_strategies.get(_config.get(ref)))
        } else {
          createStrategy(ref, _config.get(ref)) match {
            case Some(i) => result.add(i)
            case None =>
          }
        }
    }
    result
  }

  /*
    创建组合器，可以多个，按顺序调用。有点类似过滤器链。第一个过滤器会接受算法或者策略的结果。后续的组合器就只能
    处理上一阶段的组合器吐出的结果
   */
  private def createCompositors(desc: JMap[_, _]): JList[Compositor[T]] = {
    if (!desc.containsKey("compositor")) return List()
    val temp = desc.get("compositor").asInstanceOf[JList[JMap[_, _]]]
    temp.map {
      f =>
        val compositor = Class.forName(shortNameMapping.forName(f.get("name").asInstanceOf[String])).newInstance().asInstanceOf[Compositor[T]]
        val configParams: JList[JMap[Any, Any]] = if (f.containsKey("params")) f.get("params").asInstanceOf[JList[JMap[Any, Any]]] else new java.util.ArrayList[JMap[Any, Any]]()
        compositor.initialize(f.get("typeFilter").asInstanceOf[JList[String]], configParams)
        compositor
    }
  }

}

trait ShortNameMapping {
  def forName(shortName: String): String
}


object StrategyDispatcher {
  private val INSTANTIATION_LOCK = new Object()


  @transient private val lastInstantiatedContext = new AtomicReference[StrategyDispatcher[Any]]()


/*  def getOrCreate(configFile: String, settings: Settings,shortNameMapping: ShortNameMapping): StrategyDispatcher[Any] = {
    INSTANTIATION_LOCK.synchronized {
      if (lastInstantiatedContext.get() == null) {
        val temp = new StrategyDispatcher[Any]()
        if(shortNameMapping!=null){
          temp.configShortNameMapping(shortNameMapping)
        }
        temp.loadConfig(configFile)
        setLastInstantiatedContext(temp)
      }
    }
    lastInstantiatedContext.get()
  }

  def getOrCreate(configFile: String, settings: Settings): StrategyDispatcher[Any] = {
    INSTANTIATION_LOCK.synchronized {
      if (lastInstantiatedContext.get() == null) {
        val temp = new StrategyDispatcher[Any]()
        temp.loadConfig(configFile)
        setLastInstantiatedContext(temp)
      }
    }
    lastInstantiatedContext.get()
  }*/

  def getOrCreate(configFile: String,shortNameMapping: ShortNameMapping): StrategyDispatcher[Any] = {
    INSTANTIATION_LOCK.synchronized {
      if (lastInstantiatedContext.get() == null) {
        val temp = new StrategyDispatcher[Any]()
        if(shortNameMapping!=null){
          temp.configShortNameMapping(shortNameMapping)
        }
        temp.loadConfig(configFile)
        setLastInstantiatedContext(temp)
      }
    }
    lastInstantiatedContext.get()
  }

  def getOrCreate(configFile: String): StrategyDispatcher[Any] = {
    INSTANTIATION_LOCK.synchronized {
      if (lastInstantiatedContext.get() == null) {
        val temp = new StrategyDispatcher[Any]()
        temp.loadConfig(configFile)
        setLastInstantiatedContext(temp)
      }
    }
    lastInstantiatedContext.get()
  }

  def clear = {
    lastInstantiatedContext.set(null)
  }

  private def setLastInstantiatedContext(strategyDispatcher: StrategyDispatcher[Any]): Unit = {
    INSTANTIATION_LOCK.synchronized {
      lastInstantiatedContext.set(strategyDispatcher)
    }
  }
}
