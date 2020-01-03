package com.atguigu.gmallpublisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmallpublisher.bean.Option;
import com.atguigu.gmallpublisher.bean.SaleInfo;
import com.atguigu.gmallpublisher.bean.Stat;
import com.atguigu.gmallpublisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

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

    //  	http://localhost:8070/sale_detail?date=2019-05-20&&startpage=1&&size=5&&keyword=手机小米
    @GetMapping("/sale_detail")
    public String getSaleDetail(@RequestParam("date") String date,
                                @RequestParam("startpage") int startpage,
                                @RequestParam("size") int size,
                                @RequestParam("keyword") String keyword) throws IOException {
        // 1. 获取聚合结果
        Map<String, Object> resultAge = service.getSaleDetailAndAggregationByField(date, keyword, "user_age", size, startpage, 100);
        Map<String, Object> resultGender = service.getSaleDetailAndAggregationByField(date, keyword, "user_gender", size, startpage, 2);

        // 2. 最终的封装对象
        SaleInfo saleInfo = new SaleInfo();

        // 3. 明细赋值:
        List<Map<String, Object>> detail = (List<Map<String, Object>>) resultAge.get("detail");
        saleInfo.setDetail(detail);
        // 4. 总数赋值
        Integer total = (Integer) resultAge.get("total");
        saleInfo.setTotal(total);

        // 5. 两个饼图
        // 5.1 关于年龄的饼图
        Stat ageStat = new Stat();


        saleInfo.addStat(ageStat);
        // 5.2 关于性别的饼图
        Stat genderStat = new Stat();
        genderStat.setTitle("用户性别占比");
        HashMap<String, Long> genderMap = (HashMap<String, Long>) resultGender.get("aggMap");
        Set<Map.Entry<String, Long>> entries = genderMap.entrySet();
        for (Map.Entry<String, Long> entry : entries) {
            Option option = new Option();
            option.setName(entry.getKey().replace("F", "女").replace("M", "男"));
            option.setValue(entry.getValue());
            genderStat.addOption(option);
        }
        saleInfo.addStat(genderStat);


        return JSON.toJSONString(saleInfo);
    }

    private String getYesterday(String date) {

        return LocalDate.parse(date).minusDays(1).toString();

    }


}
