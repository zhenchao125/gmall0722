package com.atguigu.gmall.common.util

import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.{Bulk, Index}

/**
  * Author atguigu
  * Date 2019/12/31 14:31
  */
object ESUtil {
    // 1. es客户端
    val esUrl = "http://hadoop102:9200"
    val conf: HttpClientConfig = new HttpClientConfig.Builder(esUrl)
        .multiThreaded(true)
        .maxTotalConnection(100)
        .connTimeout(10000)
        .readTimeout(10000)
        .build()
    
    val factory: JestClientFactory = new JestClientFactory()
    factory.setHttpClientConfig(conf)
    
    // 获取客户端
    def getClient: JestClient = factory.getObject
    
    /**
      * 向es插入单个 document
      *
      * @param indexName
      * @param source
      */
    def insertSingle(indexName: String, source: Any): Unit = {
        val client: JestClient = getClient
        val action: Index = new Index.Builder(source)
            .index(indexName)
            .`type`("_doc")
            .build()
        client.execute(action)
        client.close()
    }
    
    /**
      * 批量插入
      *
      * inserBulk("user", [User(...)])
      * inserBulk("user", [(id, User(...))])
      *
      * @param indexName
      * @param sources  [User(...)]    [(id, User(...))]
      */
    def insertBulk(indexName: String, sources: Iterator[Any]) = {
        val client: JestClient = getClient
        val bulk: Bulk.Builder =
            new Bulk.Builder()
                .defaultIndex(indexName)
                .defaultType("_doc")
        
        sources.foreach {
            case (id: String, source) =>
                val action: Index = new Index.Builder(source).id(id).build()
                bulk.addAction(action)
            case source =>
                println(source)
                val action: Index = new Index.Builder(source).build()
                bulk.addAction(action)
        }
        
        client.execute(bulk.build())
        client.shutdownClient()
        
    }
    
    
    def main(args: Array[String]): Unit = {
        
        /*val source1 = User(300, "zhiling")
        val source2 = User(400, "zhiling")
        
        insertBulk("user", Iterator(("a", source1), source2))
        */
        
    }
}

//case class User(age: Int, name: String)

/*
两种source:
    1. json格式的字符串
    2. 样例类
 */