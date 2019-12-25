package com.atguigu.gmalllogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author lzc
 * @Date 2019/12/25 9:18
 */
/*@Controller
@ResponseBody*/
@RestController   // == @Controller + @ResponseBody
public class LoggerController {
    // http://localhost:8080/log
    @PostMapping("/log")
    public String doLog(@RequestParam("log") String log) {
//        System.out.println(log);
        // 1. 给每条日志添加一个时间戳
        JSONObject jsonObj = addTS(log);
        // 2. 把日志落盘, 将来可以用flume进行采集
        saveLog2File(jsonObj);
        // 3. 直接把数据写入到kafka
        saveLog2Kafka(jsonObj);

        return "abc";  //
    }

    @Autowired
    KafkaTemplate<String, String> kafka;

    /**
     * 把数据写入到kafka
     *
     * @param jsonObj
     */
    private void saveLog2Kafka(JSONObject jsonObj) {
        String type = jsonObj.getString("logType");
        if ("event".equals(type)) {
            kafka.send(Constant.TOPIC_EVENT, jsonObj.toJSONString());
        } else {
            kafka.send(Constant.TOPIC_STARTUP, jsonObj.toJSONString());
        }
    }


    Logger logger = LoggerFactory.getLogger(LoggerController.class);

    /**
     * 保存日志到文件中
     *
     * @param jsonObj
     */
    private void saveLog2File(JSONObject jsonObj) {
        logger.info(jsonObj.toJSONString());
    }

    /**
     * 给日志添加时间戳
     *
     * @param log
     * @return
     */
    private JSONObject addTS(String log) {
        JSONObject jsonObj = JSON.parseObject(log);
        jsonObj.put("ts", System.currentTimeMillis());
        return jsonObj;
    }
}
