package com.niuwa.streaming.dispatcher

object MapTest {
  def main(args: Array[String]): Unit = {
    val map = scala.collection.immutable.Map("one" -> 1)
    var map1 = map
    map1 += ("two" -> 2)
    val map11 = Map("one" -> 1)
    println("immutable" + map)
    println("immutable" + map1)
    println((map.hashCode(), map1.hashCode(), map == map1, map == map11))

    val map2 = scala.collection.mutable.Map("one" -> 1)
    val map3 = map2
    map3 += ("three" -> 3)
    println("mutable" + map2)
    println("mutable" + map3)
    println((map2.hashCode(), map3.hashCode(), map2 == map3))
  }
}
