package com.atguigu.gmallpublisher.service;

import com.atguigu.gmallpublisher.mapper.DauMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author lzc
 * @Date 2019/12/28 9:24
 */
@Service
public class PublisherServiceImp implements PublisherService {

    @Autowired
    public DauMapper dauMapper;
    @Override
    public long getDau(String date) {
        return dauMapper.getDau(date);
    }
}
