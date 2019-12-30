package com.atguigu.canal

import java.util

import com.alibaba.fastjson.JSONObject
import com.alibaba.otter.canal.protocol.CanalEntry
import com.alibaba.otter.canal.protocol.CanalEntry.EventType
import com.atguigu.gmall.common.util.Constant

/**
  * Author atguigu
  * Date 2019/12/28 15:47
  */
object CanalHandler {
    def handle(tableName: String, rowDataList: util.List[CanalEntry.RowData], eventType: CanalEntry.EventType) = {
        import scala.collection.JavaConversions._
        if ("order_info" == tableName && eventType == EventType.INSERT && rowDataList != null && !rowDataList.isEmpty) {
            
            for (rowData <- rowDataList) {
                
                val jsonObj = new JSONObject()
                // 变化后的列
                val columnList: util.List[CanalEntry.Column] = rowData.getAfterColumnsList
                for (column <- columnList) {
                    jsonObj.put(column.getName, column.getValue)
                }
                println(jsonObj.toJSONString)
                // 写入到Kafka
                MyKafkaUtil.send(Constant.TOPIC_ORDER, jsonObj.toJSONString)
            }
        }
        
    }
    
}
