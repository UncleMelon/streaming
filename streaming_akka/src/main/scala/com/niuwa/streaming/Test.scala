package com.niuwa.streaming

import java.io.File

object Test {
  def main(args: Array[String]): Unit = {
    val filePath = getClass.getClassLoader.getResource("udf.properties").getFile
    Option(new File(filePath))
      .foreach(x => println(x.exists().toString))
    println("xxx")
  }
}
