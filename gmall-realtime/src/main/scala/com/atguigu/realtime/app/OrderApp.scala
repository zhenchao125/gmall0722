package com.atguigu.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.gmall.common.util.Constant
import com.atguigu.realtime.bean.OrderInfo
import com.atguigu.realtime.util.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Author atguigu
  * Date 2019/12/30 8:50
  */
object OrderApp {
    def main(args: Array[String]): Unit = {
        // 1. 从kafka消费数据
        val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName("OrderApp")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
        val sourceDStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Constant.TOPIC_ORDER)
        
        // 2. 做处理: a: 封装数据 b:把一些敏感数据做脱敏处理
        val orderInfoDStream: DStream[OrderInfo] = sourceDStream.map {
            case (_, jsonString) =>
                val orderInfo: OrderInfo = JSON.parseObject(jsonString, classOf[OrderInfo])
                // 13152505519 脱敏
                orderInfo.consignee_tel = orderInfo.consignee_tel.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1****$3")
                orderInfo.consignee = orderInfo.consignee.substring(0, 1) + "**"
                orderInfo
        }
        
        import org.apache.phoenix.spark._
        // 3. 写入到的hbase(phoenix)
        orderInfoDStream.foreachRDD(rdd =>
            rdd.saveToPhoenix(
                "GMALL_ORDER_INFO",
                Seq("ID", "PROVINCE_ID", "CONSIGNEE", "ORDER_COMMENT", "CONSIGNEE_TEL", "ORDER_STATUS", "PAYMENT_WAY", "USER_ID", "IMG_URL", "TOTAL_AMOUNT", "EXPIRE_TIME", "DELIVERY_ADDRESS", "CREATE_TIME", "OPERATE_TIME", "TRACKING_NO", "PARENT_ORDER_ID", "OUT_TRADE_NO", "TRADE_BODY", "CREATE_DATE", "CREATE_HOUR"),
                zkUrl = Some("hadoop102,hadoop103,hadoop104:2181"))
        
        )
        
        ssc.start()
        ssc.awaitTermination()
    }
}
