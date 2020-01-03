package com.atguigu.realtime.app

/**
  * Author atguigu
  * Date 2020/1/3 10:53
  */
object Test {
    def main(args: Array[String]): Unit = {
        //        println(JSON.toJSONString(User(20, "abc")))
        //        println(JSON.toJSONString(User(20, "abc"), true))
        val user = User(20, "abc")
        import org.json4s.jackson.Serialization
        import org.json4s.DefaultFormats
        implicit val f = DefaultFormats
        println(Serialization.write(user))
    }
}

case class User(age: Int, name: String)