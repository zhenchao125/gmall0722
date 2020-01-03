package com.atguigu.canal

import java.util

import com.alibaba.fastjson.JSONObject
import com.alibaba.otter.canal.protocol.CanalEntry
import com.alibaba.otter.canal.protocol.CanalEntry.EventType
import com.atguigu.gmall.common.util.Constant

import scala.collection.JavaConversions._
import scala.util.Random

/**
  * Author atguigu
  * Date 2019/12/28 15:47
  */
object CanalHandler {
    def handle(tableName: String, rowDataList: util.List[CanalEntry.RowData], eventType: CanalEntry.EventType) = {
        if ("order_info" == tableName && eventType == EventType.INSERT && rowDataList != null && !rowDataList.isEmpty) {
            println("order_info")
            sendRowDataListToKafka(rowDataList, Constant.TOPIC_ORDER)
        } else if ("order_detail" == tableName && eventType == EventType.INSERT && rowDataList != null && !rowDataList.isEmpty) {
            println("order_detail")
            sendRowDataListToKafka(rowDataList, Constant.TOPIC_DETAIL)
            
        }
    }
    
    
    private def sendRowDataListToKafka(rowDataList: util.List[CanalEntry.RowData], topic: String): Unit = {
        
        new Thread(){
            override def run(): Unit = {
                for (rowData <- rowDataList) {
        
                    val jsonObj: JSONObject = new JSONObject()
                    // 变化后的列
                    val columnList: util.List[CanalEntry.Column] = rowData.getAfterColumnsList
                    for (column <- columnList) {
                        jsonObj.put(column.getName, column.getValue)
                    }
                    // 写入到Kafka
                    Thread.sleep(new Random().nextInt(1000 * 8))
                    MyKafkaUtil.send(topic, jsonObj.toJSONString)
                }
            }
        }.start()
        
    }
}
