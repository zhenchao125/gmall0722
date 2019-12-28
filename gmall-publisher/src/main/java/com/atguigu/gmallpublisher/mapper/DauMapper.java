package com.atguigu.gmallpublisher.mapper;

import java.util.List;
import java.util.Map;

public interface DauMapper {
    Long getDau(String date);

    List<Map> getHourDau(String date);
}
