package com.atguigu.realtime.util

import java.io.InputStream
import java.util.Properties

import scala.collection.mutable

/**
  * Author atguigu
  * Date 2019/12/25 15:57
  */
object PropertiesUtil {
    
    val map: mutable.Map[String, Properties] = mutable.Map[String, Properties]()
    
    def getProperty(configFile: String, propName: String): String = {
        
        map.getOrElseUpdate(configFile, {
            println("abc")
            val is: InputStream = ClassLoader.getSystemResourceAsStream(configFile)
            val ps: Properties = new Properties()
            ps.load(is)
            ps
        }).getProperty(propName)
    }
    
    def main(args: Array[String]): Unit = {
        println(getProperty("config.properties", "kafka.broker.list"))
    }
}
