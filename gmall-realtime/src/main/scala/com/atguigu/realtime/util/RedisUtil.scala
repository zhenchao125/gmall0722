package com.atguigu.realtime.util

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
  * Author atguigu
  * Date 2019/12/27 9:12
  */
object RedisUtil {
    
    val host = PropertiesUtil.getProperty("config.properties", "redis.host")
    val port = PropertiesUtil.getProperty("config.properties", "redis.port").toInt
    private val jedisPoolConfig = new JedisPoolConfig()
    jedisPoolConfig.setMaxTotal(100) //最大连接数
    jedisPoolConfig.setMaxIdle(40) //最大空闲
    jedisPoolConfig.setMinIdle(10) //最小空闲
    jedisPoolConfig.setBlockWhenExhausted(true) //忙碌时是否等待
    jedisPoolConfig.setMaxWaitMillis(1000 * 60 * 2) //忙碌时等待时长 毫秒
    jedisPoolConfig.setTestOnBorrow(true) //每次获得连接的进行测试
    jedisPoolConfig.setTestOnReturn(true)
    private val jedisPool: JedisPool = new JedisPool(jedisPoolConfig, host, port)
    
    // 直接得到一个 Redis 的连接
    def getJedisClient: Jedis = {
        jedisPool.getResource
    }
    
    
}
