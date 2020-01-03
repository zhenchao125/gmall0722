package com.atguigu.gmallpublisher.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lzc
 * @Date 2020/1/3 15:22
 */
public class Stat {
    private String title;
    private List<Option> options = new ArrayList<>();

    public void addOption(Option option) {
        options.add(option);
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
/*
{
    "options": [{
        "name": "20岁以下",
        "value": 0.0
    }, {
        "name": "20岁到30岁",
        "value": 25.8
    }, {
        "name": "30岁及30岁以上",
        "value": 74.2
    }],
    "title": "用户年龄占比"
}
 */