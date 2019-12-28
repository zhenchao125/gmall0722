package com.atguigu.gmallpublisher.service;

import java.util.Map;

public interface PublisherService {
    long getDau(String date);

    Map<String, Long> getHourDau(String date);
}
