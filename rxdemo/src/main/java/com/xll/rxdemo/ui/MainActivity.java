package com.xll.rxdemo.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.xll.rxdemo.R;
import com.xll.rxdemo.bean.Weather;
import com.xll.rxdemo.network.GetRequest_Interface;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 无条件网络请求轮询
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "RxJava tag";
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noConditionQuery();

    }

    private void noConditionQuery(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://toolsapi.nfapp.southcn.com/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        final GetRequest_Interface reqeust = retrofit.create(GetRequest_Interface.class);

        Observable.intervalRange(0,5,0,3,TimeUnit.SECONDS)
                .flatMap(new Function<Long, ObservableSource<Weather>>() {
                    @Override
                    public ObservableSource<Weather> apply(Long aLong) throws Exception {
                        Log.d(TAG, "第" + aLong + "次请求轮询结果:");
                        return reqeust.getCall();
                    }
                })
                .subscribe(new Observer<Weather>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Weather weather) {
                if(weather!=null){
                    weather.show();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        });
    }

    public void conditionQuery(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://toolsapi.nfapp.southcn.com/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        GetRequest_Interface reqeust = retrofit.create(GetRequest_Interface.class);

        Observable<Weather> observable = reqeust.getCall();

        observable.repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {

                    @Override
                    public ObservableSource<?> apply(Object o) throws Exception {
                        return Observable.just(1).delay(2,TimeUnit.SECONDS);
                    }
                });
            }
        }).subscribe(new Observer<Weather>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Weather weather) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //切断流
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
