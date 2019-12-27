package com.atguigu.realtime.util


import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils

/**
  * Author lzc
  * Date 2019/5/15 11:19 AM
  */
object MyKafkaUtil {
    
    val params: Map[String, String] = Map(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> PropertiesUtil.getProperty("config.properties", "kafka.broker.list"),
        ConsumerConfig.GROUP_ID_CONFIG -> PropertiesUtil.getProperty("config.properties", "kafka.group")
    )
    
    def getKafkaStream(ssc: StreamingContext, topic: String): InputDStream[(String, String)] = {
        KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, params, Set(topic))
    }
    
}
