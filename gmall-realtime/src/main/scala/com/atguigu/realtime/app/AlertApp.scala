package com.atguigu.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.gmall.common.util.Constant
import com.atguigu.realtime.bean
import com.atguigu.realtime.bean.{AlertInfo, EventLog}
import com.atguigu.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

/**
  * Author atguigu
  * Date 2019/12/30 11:22
  */
object AlertApp {
    def main(args: Array[String]): Unit = {
        // 1. 从kafka消费数据
        val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName("OrderApp")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
        val sourceDStream: DStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Constant.TOPIC_EVENT).window(Minutes(5))
        
        // 2. 对数据进行处理
        val eventLogDStream: DStream[(String, EventLog)] = sourceDStream.map {
            case (_, jsonString) =>
                val eventLog: EventLog = JSON.parseObject(jsonString, classOf[EventLog])
                (eventLog.mid, eventLog)
        }
        val alertInfoDStream: DStream[(Boolean, AlertInfo)] = eventLogDStream
            .groupByKey
            .map {
                case (mid, eventLogIt) =>
                    // 保存领取过优惠券的用户
                    val uidSet: mutable.HashSet[String] = new mutable.HashSet[String]()
                    // 优惠券对应的商品id
                    val itemSet: mutable.HashSet[String] = new mutable.HashSet[String]()
                    // 保存用户的所有事件
                    val eventList: ListBuffer[String] = new mutable.ListBuffer[String]()
                
                
                    // 用来表示这5分钟内是否有点击商品
                    var isClicked = false
                    breakable {
                        eventLogIt.foreach(eventLog => {
                            eventList += eventLog.eventId
                            if (eventLog.eventId == "coupon") {
                                uidSet += eventLog.uid // 保存领优惠券的用户的id
                                itemSet += eventLog.itemId // 优惠券对应的商品id
                            } else if (eventLog.eventId == "clickItem") {
                                // 一旦出现点击商品, 就不可能再有预警, 所以, 直接退出这个遍历
                                isClicked = true
                                break
                            }
                        })
                    }
                    // (表示是否要预警,  预警的信息)
                    (!isClicked && uidSet.size >= 3, AlertInfo(mid, uidSet, itemSet, eventList, System.currentTimeMillis()))
            }
        // 写入到es
        alertInfoDStream.filter(_._1).map(_._2).foreachRDD(rdd =>{
            //
        } )
        
        
        ssc.start()
        ssc.awaitTermination()
        
    }
}

/*
同一设备，
5分钟内三次及以上用不同账号登录并领取优惠劵，
并且在登录到领劵过程中没有浏览商品。
同时达到以上要求则产生一条预警日志。 同一设备，每分钟只记录一次预警。

分析:
1. 同一设备 按设备(mid)分组
2. 5分钟 窗口长度5分钟
3. 三个不同的账号 (uid) 三个不同的用户
4. 只处理领取优惠券的
5. 没有浏览任何的商品

---
6. 每分钟只记录一次预警。 将来可以依靠es来完成   ????

 */