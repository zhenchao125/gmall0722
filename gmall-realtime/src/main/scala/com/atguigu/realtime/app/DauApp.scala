package com.atguigu.realtime.app

import com.atguigu.gmall.common.util.Constant
import com.atguigu.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Author atguigu
  * Date 2019/12/25 15:53
  */
object DauApp {
    def main(args: Array[String]): Unit = {
        // 1. 从kafka读数据
        val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName("RealtimeApp")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
        val sourceDStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Constant.TOPIC_STARTUP)
        sourceDStream.print(1000)
        // 2. 计算DAU
        
        
        // 3. 写入hbase(phoenix)
        
        
        ssc.start()
        ssc.awaitTermination()
    }
}
