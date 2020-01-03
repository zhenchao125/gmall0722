package com.atguigu.gmallpublisher.service

/**
  * Author atguigu
  * Date 2020/1/3 14:31
  */
object DSLUtil {
    def getDSL(date: String,
               keyWord: String,
               field: String,
               size: Int,
               startPage: Int,
               pageSize: Int): String = {
        s"""
           |{
           |  "query": {
           |    "bool": {
           |      "filter": {
           |        "term": {
           |          "dt": "$date"
           |        }
           |      },
           |      "must": [
           |        {"match": {
           |          "sku_name": {
           |            "query": "$keyWord",
           |            "operator": "and"
           |          }
           |        }}
           |      ]
           |    }
           |  },
           |  "aggs": {
           |    "groupby_$field":{
           |      "terms": {
           |        "field": "$field",
           |        "size": $size
           |      }
           |    }
           |  },
           |  "from": ${(startPage - 1) * pageSize},
           |  "size": $pageSize
           |}
        """.stripMargin
        
    }
}

/*
page

0

(1 - 1) * 5
(2 - 1) * 5
(3 - 1) * 5

 */
