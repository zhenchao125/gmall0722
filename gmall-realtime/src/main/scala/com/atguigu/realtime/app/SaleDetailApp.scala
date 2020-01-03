package com.atguigu.realtime.app

import java.util.Properties

import com.alibaba.fastjson.JSON
import com.atguigu.gmall.common.util.{Constant, ESUtil}
import com.atguigu.realtime.bean.{OrderDetail, OrderInfo, SaleDetail, UserInfo}
import com.atguigu.realtime.util.{MyKafkaUtil, RedisUtil}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import redis.clients.jedis.Jedis

/**
  * Author atguigu
  * Date 2020/1/3 9:12
  */
object SaleDetailApp {
    def main(args: Array[String]): Unit = {
        // 1. 从kafka消费数据
        val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName("OrderApp")
        val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
        
        // 1.
        val orderInfoStream: DStream[(String, OrderInfo)] = MyKafkaUtil
            .getKafkaStream(ssc, Constant.TOPIC_ORDER)
            .map {
                case (_, jsonString) =>
                    val orderInfo: OrderInfo = JSON.parseObject(jsonString, classOf[OrderInfo])
                    (orderInfo.id, orderInfo)
            }
        val orderDetailStream: DStream[(String, OrderDetail)] = MyKafkaUtil
            .getKafkaStream(ssc, Constant.TOPIC_DETAIL)
            .map {
                case (_, jsonString) =>
                    val orderDetail: OrderDetail = JSON.parseObject(jsonString, classOf[OrderDetail])
                    (orderDetail.order_id, orderDetail)
            }
        // 2. join的时候, 数据必须是k-v
        val fulljoinStrem: DStream[SaleDetail] = orderInfoStream.fullOuterJoin(orderDetailStream).mapPartitions(it => {
            // 一个分区一个连接
            val client: Jedis = RedisUtil.getJedisClient
            // 获取连接到redis的客户端
            val result = it.flatMap {
                case (orderId, (Some(orderInfo), Some(orderDetail))) =>
                    println("some some")
                    // 1. orderInfo缓存到redis
                    cacheOrderInfo(client, orderInfo)
                    // 2. 组合一个所谓的宽表: 新的样例类(订单信息, 订单详情, 用户的信息)
                    List(SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail))
                case (orderId, (Some(orderInfo), None)) =>
                    println("some none")
                    // 1. 缓存到redis
                    cacheOrderInfo(client, orderInfo)
                    // 2. 去orderDetail 缓存中查找数据  一个orderInfo对应多个orderDetail
                    // 3. 查到就组成宽表
                    import scala.collection.JavaConversions._
                    client.keys(s"order_detail_${orderInfo.id}_*").toList.map(key => {
                        println(key)
                        val orderDetail: OrderDetail = JSON.parseObject(client.get(key), classOf[OrderDetail])
                        SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail)
                    })
                case (orderId, (None, Some(orderDetail))) =>
                    println("none some")
                    // 1. 去orderInfo缓存中去查找数据  key在redis中不存在, 则返回null
                    val orderInfoString: String = client.get(s"order_info_${orderDetail.order_id}")
                    
                    if (orderInfoString != null && orderInfoString.startsWith("{")) {
                        // 2. 找到了, 就组合成宽表数据
                        val orderInfo: OrderInfo = JSON.parseObject(orderInfoString, classOf[OrderInfo])
                        List(SaleDetail().mergeOrderInfo(orderInfo).mergeOrderDetail(orderDetail))
                    } else {
                        // 3. 找不到把数据缓存到redis
                        cacheOrderDetail(client, orderDetail)
                        List[SaleDetail]()
                    }
            }
            client.close()
            result
        })
        
        // 3. 补充用户信息  从 mysql 去读取用户信息
        val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
        import spark.implicits._
        val jdbcUrl = "jdbc:mysql://hadoop102:3306/gmall0722"
        val props: Properties = new Properties()
        props.setProperty("user", "root")
        props.setProperty("password", "aaaaaa")
        
        val finalStream: DStream[SaleDetail] = fulljoinStrem.transform(rdd => {
            val userInfoRdd: RDD[(String, UserInfo)] = spark.read.jdbc(jdbcUrl, "user_info", props)
                .as[UserInfo]
                .rdd
                .map(userInfo => (userInfo.id, userInfo))
            val saleDetailRdd: RDD[(String, SaleDetail)] = rdd.map(saleDetail => (saleDetail.user_id, saleDetail))
            saleDetailRdd.join(userInfoRdd).map {
                case (_, (saleDetail, useInfo)) => saleDetail.mergeUserInfo(useInfo)
            }
            
        })
        // 4. 数据写入到es
        finalStream.foreachRDD(rdd => {
            ESUtil.insertBulk(Constant.INDEX_SALE_DETAIL, rdd.collect().toIterator)
        })
        
        
        ssc.start()
        ssc.awaitTermination()
    }
    
    /**
      * 缓存orderInfo
      *
      * @param client
      * @param content
      */
    def cacheOrderInfo(client: Jedis, orderInfo: OrderInfo): Unit = {
        val key: String = s"order_info_${orderInfo.id}"
        cache2Redis(client, key, orderInfo)
    }
    
    /**
      * 缓存orderDetail
      *
      * @param client
      * @param content
      */
    def cacheOrderDetail(client: Jedis, orderDetail: OrderDetail): Unit = {
        val key = s"order_detail_${orderDetail.order_id}_${orderDetail.id}"
        cache2Redis(client, key, orderDetail)
    }
    
    /**
      * 缓存数据到redis
      *
      * @param client
      * @param content
      */
    def cache2Redis(client: Jedis, key: String, content: AnyRef): Unit = {
        val value: String = Serialization.write(content)(DefaultFormats)
        client.setex(key, 1000 * 60 * 30, value) // 给每个key添加了过期时间
    }
}

/*
1. 从kafka消费数据, 得到两个流
    a: 数据封装
    

2. join两个流
    需要缓存

3. 在对join后的流增加补充user的信息
    使用spark-sql来查询mysql的user_info
    
4. 写数据到es



redis数据格式:
    hash  不合适

    key             value
    "orderInfo"     field       value
                    id          OrderInfo json格式数据

    orderInfo
        String
            key                        value
            "oder_info_" + id          OrderInfo json格式数据
    
    orderDetail
        string
            key                                    value
            "oder_detail_" + orderId  + id         OrderDetail json格式数据
 */