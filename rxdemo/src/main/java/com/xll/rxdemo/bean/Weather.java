package com.xll.rxdemo.bean;

import android.util.Log;

/**
 * Created by LittleRoy on 2018/9/24.
 */
public class Weather {

    private int code;

    private content data;
    private static class content {
        private String ico_url;
        private String result;
        private String temperature;
        private String updateTime;
        private String wet;
        private String wind;
        private String url;
    }

    //定义 输出返回数据 的方法
    public void show() {
        Log.d("RxJava", data.result+data.temperature );
    }
}