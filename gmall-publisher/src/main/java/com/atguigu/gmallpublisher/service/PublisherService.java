package com.atguigu.gmallpublisher.service;

import java.util.Map;

public interface PublisherService {
    // Dau
    long getDau(String date);

    Map<String, Long> getHourDau(String date);


    // 销售额
    Double getAmountTotal(String date);

    Map<String, Double> getHourAmount(String date);
}
