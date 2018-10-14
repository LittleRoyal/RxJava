package com.xll.rxdemo.network;

import com.xll.rxdemo.bean.Weather;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * retrofit
 */
public interface GetRequest_Interface {
    @GET("toolsapi/api/weather/now?location=guangzhou")
    Observable<Weather> getCall();
}