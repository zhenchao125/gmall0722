package com.atguigu.realtime.util

import com.atguigu.gmall.common.util.ESUtil
import com.atguigu.realtime.bean.AlertInfo
import org.apache.spark.rdd.RDD

/**
  * Author atguigu
  * Date 2019/12/31 16:20
  */
object ImplicitUtil {
    
    implicit class ESFunction[T <: AlertInfo](rdd: RDD[T]) {
        def saveToES(indexName: String) = {
            rdd.foreachPartition(it => {
                ESUtil.insertBulk(indexName, it.map(info => {
                    // 保证一个分钟内每个mid只会最多一条预警
                    (info.mid + "_" + info.ts / 1000 / 60, info)
                }))
            })
        }
    }
    
}
