package com.atguigu.canal

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * Author atguigu
  * Date 2019/12/28 16:25
  */
object MyKafkaUtil {
    val props = new Properties()
    // Kafka服务端的主机名和端口号
    props.put("bootstrap.servers", "hadoop102:9092,hadoop103:9092,hadoop104:9092")
    // key序列化
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    // value序列化
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    
    private val producer = new KafkaProducer[String, String](props)
    def send(topic :String, content: String) = {
        // 创建生产者, 然后写入
        val record = new ProducerRecord[String, String](topic, content)
        producer.send(record)
    }
    
    
}
