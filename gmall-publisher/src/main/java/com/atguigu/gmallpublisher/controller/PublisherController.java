package com.atguigu.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String getDau(@RequestParam("date") String date){

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

        return JSON.toJSONString(result);
    }
}
