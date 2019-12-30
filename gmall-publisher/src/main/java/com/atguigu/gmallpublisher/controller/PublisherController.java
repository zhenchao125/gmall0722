package com.atguigu.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lzc
 * @Date 2019/12/28 9:27
 */
@RestController
public class PublisherController {

    @Autowired
    public PublisherService service;

    // http://localhost:8070/realtime-total?date=2019-09-20
    @GetMapping("/realtime-total")
    public String getDau(@RequestParam("date") String date) {

        /*
        [{"id":"dau","name":"新增日活","value":1200},
        {"id":"new_mid","name":"新增设备","value":233} ]

         */
        List<Map<String, Object>> result = new ArrayList<>();

        HashMap<String, Object> map1 = new HashMap<>();
        result.add(map1);
        map1.put("id", "dau");
        map1.put("name", "新增日活");
        map1.put("value", service.getDau(date));


        HashMap<String, Object> map2 = new HashMap<>();
        result.add(map2);
        map2.put("id", "new_mid");
        map2.put("name", "新增设备");
        map2.put("value", "233");


        HashMap<String, Object> map3 = new HashMap<>();
        result.add(map3);
        map3.put("id", "order_amount");
        map3.put("name", "新增交易额");
        map3.put("value", service.getAmountTotal(date));


        return JSON.toJSONString(result);
    }

    // http://localhost:8070/realtime-hour?id=dau&date=2019-09-20
    @GetMapping("/realtime-hour")
    public String getHourDau(@RequestParam("id") String id, @RequestParam("date") String date) {
        if ("dau".equals(id)) {
            /*
            {"yesterday":{"11":383,"12":123,"17":88,"19":200 },
              "today":{"12":38,"13":1233,"17":123,"19":688 }}

             */

            Map<String, Map<String, Long>> resultMap = new HashMap<>();

            // 今天
            resultMap.put("today", service.getHourDau(date));

            // 昨天
            resultMap.put("yesterday", service.getHourDau(getYesterday(date)));

            return JSON.toJSONString(resultMap);
        } else if ("order_amount".equals(id)) {
            /*
            {"yesterday":{"11":383,"12":123,"17":88,"19":200 },
                "today":{"12":38,"13":1233,"17":123,"19":688 }}

             */
            Map<String, Map<String, Double>> resultMap = new HashMap<>();
            // 今天
            resultMap.put("today", service.getHourAmount(date));

            // 昨天
            resultMap.put("yesterday", service.getHourAmount(getYesterday(date)));
            return JSON.toJSONString(resultMap);
        }

        return "ok";

    }

    private String getYesterday(String date) {

        return LocalDate.parse(date).minusDays(1).toString();

    }


}
