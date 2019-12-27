package com.atguigu.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.atguigu.gmall.common.util.Constant
import com.atguigu.realtime.bean.StartupLog
import com.atguigu.realtime.util.{MyKafkaUtil, RedisUtil}
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

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
        
        // 2. 计算DAU(DAU明细)
        // 2.1 封装数据
        val startupLogDStream: DStream[StartupLog] = sourceDStream.map {
            case (_, value) =>
                val startupLog: StartupLog = JSON.parseObject(value, classOf[StartupLog])
                startupLog
        }
        
        
        // 2.2 去重   使用redis(set)去重  把启动过的设备mid存储到redis中, 然后用这个set去过滤流
        val filteredStartupLogDStream: DStream[StartupLog] = startupLogDStream.transform(rdd => {
            
            // 保留没有启动过的(表示是第一次启动)
            val client: Jedis = RedisUtil.getJedisClient
            val midSet: util.Set[String] = client.smembers(Constant.TOPIC_STARTUP + ":" + new SimpleDateFormat("yyyy-MM-dd").format(new Date))
            
            client.close()
            // 集合做广播
            val bdSet: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
            rdd.filter(log => {
                !bdSet.value.contains(log.mid)
            })
            
        })
        
        // 2.3 把第一次启动的设备写入到redis
        filteredStartupLogDStream.foreachRDD(rdd => {
            // 把每个mid都写入到redis中,
            rdd.foreachPartition(logIt => {
                // 1.获取连接
                val client: Jedis = RedisUtil.getJedisClient
                // 2. 写入
                logIt.foreach(log => {
                    client.sadd(Constant.TOPIC_STARTUP + ":" + log.logDate, log.mid)
                })
            
                client.close()
            })
        })
        
        // 3. 写入hbase(phoenix)
        filteredStartupLogDStream.foreachRDD(rdd => {
            //
        })
        
        ssc.start()
        ssc.awaitTermination()
    }
}
