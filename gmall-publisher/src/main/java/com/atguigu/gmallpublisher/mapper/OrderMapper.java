package com.atguigu.gmallpublisher.mapper;

import java.util.List;
import java.util.Map;

/**
 * @Author lzc
 * @Date 2019/12/30 9:53
 */
public interface OrderMapper {
    Double getAmountTotal(String date);

    List<Map<String, Double>> getHourAmount(String date);
}
