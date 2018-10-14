package com.xll.rxjava;

import android.util.Log;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

/**
 * Created by LittleRoy on 2018/9/3.
 */
public class OperateTest {
    /**
     * just操作符
     * 主要用于发送几个简单的数据,数据类型可以不同
     * 发送的数据不能超过10个数据
     * 当发送的数量超过2个时，其内部调用的是fromArray方法
     */
    public static void justOperate() {
        Flowable.just("Hello", "my", "girl")
                .subscribe(s -> Log.e("tag", s));
    }

    /**
     * fromArray操作符
     * 可以发送任意长度的数据数组
     * 可以直接发送数组，但是不要直接传递list,list会被认为是一个数据元素
     * fromArray(new int{1,2,3,4})
     */
    public static void fromArrayOperate() {
        Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
                .subscribe(s -> Log.e("tag", "当前值：" + s));
    }

    /**
     * fromIterable
     * 可以发送任意长度的数据集合
     * fromIterable(new list{1,2,3,4})
     */
    public static void fromIterableOperate() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add("我是：" + i);
        }
        Flowable.fromIterable(list)
                .subscribe(s -> Log.e("tag", "当前值：" + s));
    }

    /**
     * empty操作符
     * 不发送数据，直接调用的onComplete()
     */
    public static void emptyOperate() {
        Flowable.empty()
                .subscribe(
                        s -> Log.e("tag", "当前值：" + s),
                        e -> Log.e("tag", "error"),
                        () -> Log.e("tag", "oncomplete"));
    }

    /**
     * error操作符
     * 不发送数据，直接调用的onError()
     */
    public static void errorOperate() {
        Flowable.error(new RuntimeException("error"))
                .subscribe(
                        s -> Log.e("tag", "当前值：" + s),
                        e -> Log.e("tag", "error"),
                        () -> Log.e("tag", "oncomplete"));
    }

    /**
     * timer操作符
     * 延迟delay时长再发送数据
     * 不管上游设置的什么数据，timer都只会发送一个onNext(0L),然后onComplete
     */
    public static void timerOperate() {
        Flowable.just(1, 2, 3, 4)
                .timer(1, TimeUnit.SECONDS)
                .subscribe(
                        s -> Log.e("tag", "当前值：" + s),
                        e -> Log.e("tag", "error"),
                        () -> Log.e("tag", "oncomplete"));
    }

    /**
     * interval操作符
     * 第一个参数表示第一个数据延迟发送的时长，后面的延迟时长以第二个参数为准
     * interval也是从0L开始发送，不限发送个数，一直发下去
     */
    public static void intervalOperate() {
        Flowable.just(10)
                .interval(5, 1, TimeUnit.SECONDS)
                .subscribe(
                        s -> Log.e("tag", "当前值：" + s),
                        e -> Log.e("tag", "error"),
                        () -> Log.e("tag", "oncomplete"));
    }

    /**
     * intervalRange操作符
     * 可以指定从那个数据开始发送，发送的数量是多少个
     * 第一个参数表示从哪个数开始发送
     * 第二个参数表示总共发送多少个数据
     * 第三个参数表示第一个数据延迟发送的时长
     */
    public static void intervalRangeOperate() {
        Flowable.just(10)
                .intervalRange(3, 10, 5, 1, TimeUnit.SECONDS)
                .subscribe(
                        s -> Log.e("tag", "当前值：" + s),
                        e -> Log.e("tag", "error"),
                        () -> Log.e("tag", "oncomplete"));
    }

    /**
     * rang/rangLong
     * 可以指定从那个数据开始发送，发送的数量是多少个
     * 第一个参数表示从哪个数开始发送
     * 第二个参数表示总共发送多少个数据
     * 不会延迟发送数据
     */
    public static void rangeOperate() {
        Flowable.just(10)
                .range(3, 10)
                .subscribe(
                        s -> Log.e("tag", "当前值：" + s),
                        e -> Log.e("tag", "error"),
                        () -> Log.e("tag", "oncomplete"));
    }

    /**
     * defer
     * 顺序调用多个观察者对象，先订阅的接受完数据，onComplete()之后，后面的才创建接受
     */
    public static void deferOperate() {
        Flowable<String> flowable = Flowable.defer(new Callable<Publisher<? extends String>>() {
            @Override
            public Publisher<? extends String> call() throws Exception {
                return Flowable.just("hello", "world");
            }
        });
        flowable.subscribe(
                s -> Log.e("tag", "当前值1：" + s),
                e -> Log.e("tag", "error1"),
                () -> Log.e("tag", "oncomplete1"));
        flowable.subscribe(
                s -> Log.e("tag", "当前值2：" + s),
                e -> Log.e("tag", "error2"),
                () -> Log.e("tag", "oncomplete2"));
    }
}
