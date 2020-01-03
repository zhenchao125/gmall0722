package com.atguigu.gmallpublisher.service;

import java.io.IOException;
import java.util.Map;

public interface PublisherService {
    // Dau
    long getDau(String date);

    Map<String, Long> getHourDau(String date);


    // 销售额
    Double getAmountTotal(String date);

    Map<String, Double> getHourAmount(String date);


    // 直接从es读取数据
    // 返回3个:  saleDetail的总数   聚合结果  明细
    // Map[total-> 值, "agg"-> Map[10->100, 20->200...], "detail"-> ]
    Map<String, Object> getSaleDetailAndAggregationByField(String date,
                                                           String keyWord,
                                                           String field,
                                                           int size,  // 聚合后的最大长度
                                                           int startPage,
                                                           int pageSize) throws IOException;
}
