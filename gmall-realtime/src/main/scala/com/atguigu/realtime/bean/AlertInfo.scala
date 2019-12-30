package com.atguigu.realtime.bean

import scala.collection.mutable

case class AlertInfo(mid: String,
                     uids: mutable.HashSet[String],
                     itemIds: mutable.HashSet[String],
                     events: mutable.ListBuffer[String],
                     ts: Long)
