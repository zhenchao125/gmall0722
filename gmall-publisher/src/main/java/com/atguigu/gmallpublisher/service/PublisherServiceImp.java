package com.atguigu.gmallpublisher.service;

import com.atguigu.gmallpublisher.mapper.DauMapper;
import com.atguigu.gmallpublisher.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lzc
 * @Date 2019/12/28 9:24
 */
@Service
public class PublisherServiceImp implements PublisherService {

    @Autowired
    public DauMapper dauMapper;
    @Autowired
    public OrderMapper orderMapper;



    /**
     * 获取指定日志的日活总数
     *
     * @param date
     * @return
     */
    @Override
    public long getDau(String date) {
        return dauMapper.getDau(date);
    }

    /**
     * 获取指定日期的小时日活的数
     *
     * @param date
     * @return
     */
    @Override
    public Map<String, Long> getHourDau(String date) {
        /*
        List<Map<"logHour"->"10", "count"->100>>

        List[Map["logHour"->"10", "count"->100], ...]

        目标:
        Map[10->100, 11->200] => {10->100, 11->200}
         */
        List<Map> mapList = dauMapper.getHourDau(date);

        HashMap<String, Long> resultMap = new HashMap<>();
        for (Map map : mapList) {
            String key = (String) map.get("LOGHOUR");
            Long value = (Long) map.get("COUNT");
            resultMap.put(key, value);
        }

        return resultMap;
    }

    /**
     * 销售总额
     * @param date
     * @return
     */
    @Override
    public Double getAmountTotal(String date) {
        return orderMapper.getAmountTotal(date);
    }

    /**
     * 小时明细
     * @param date
     * @return
     */
    @Override
    public Map<String, Double> getHourAmount(String date) {


        List<Map<String, Double>> mapList = orderMapper.getHourAmount(date);

        Map<String, Double> resultMap = new HashMap<>();
        for (Map map : mapList) {
            String key = (String) map.get("CREATE_HOUR");
            Double value = ((BigDecimal) map.get("SUM")).doubleValue();
            resultMap.put(key, value);
        }
        return resultMap;
    }
}
