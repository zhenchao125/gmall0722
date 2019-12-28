package com.atguigu.canal

import java.net.InetSocketAddress
import java.util

import com.alibaba.otter.canal.client.{CanalConnector, CanalConnectors}
import com.alibaba.otter.canal.protocol.CanalEntry.{EntryType, RowChange}
import com.alibaba.otter.canal.protocol.{CanalEntry, Message}
import com.google.protobuf.ByteString

/**
  * Author atguigu
  * Date 2019/12/28 15:23
  */
object CanalClient {
    def main(args: Array[String]): Unit = {
        // 用于java集合到scala集合的转换
        import scala.collection.JavaConversions._
        // 1. 创建连接
        val addr = new InetSocketAddress("hadoop102", 11111)
        val connector: CanalConnector = CanalConnectors.newSingleConnector(addr, "example", "", "")
        connector.connect()
        connector.subscribe("gmall0722.*")
    
        while (true) {
            val msg: Message = connector.get(100)
            val entries: util.List[CanalEntry.Entry] = msg.getEntries
            if(entries != null && entries.nonEmpty){
                for(entry <- entries){
                    // 只处理ROWDATA这种类型的Entry
                    if(entry.getEntryType == EntryType.ROWDATA){
                        val storeValue: ByteString = entry.getStoreValue
                        val rowChange: RowChange = RowChange.parseFrom(storeValue)
                        
                        val rowDataList: util.List[CanalEntry.RowData] = rowChange.getRowDatasList
                        
                        CanalHandler.handle(entry.getHeader.getTableName, rowDataList, rowChange.getEventType)
                    }
                }
            }else{
                System.out.println("没有拉取到数据, 2s之后重新拉取");
                Thread.sleep(2000)
            }
        }
    }
}

/*
1. 从canal读数据
    1. 先创建一个客户端对象
    
    2. 订阅数据
    
    3. 对数据解析解析

message 一次可以拉一个message, 一个message可以看成是由多条sql执行导致的变化组成的数据的封装
Entry  实体 一个message封装多个Entry. 一个Entry表示一条sql执行的结果(多行变化)
    StoreValue 一个Entry封装一个StoreValue, 可以看成是序列化的数据
RowChange 表示一行变化大数据. 一个StoreValue会存储1个RowChange
    RowData 变化的数据 对应以一行数据  一个RowChange对应对个RowData
Column 列   列名和值

kafka: {"cn", "value", }



2. 把数据进行格式调整之后, 写入到kafka


 */