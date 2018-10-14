package com.xll.rxjava;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function3;

/**
 * 合并聚合操作符
 * startWith/startWithArray  在被观察者发送数据之前追加新的数据或者新的被观察者
 * concat/concatArray  连接多个操作符
 * merge/mergeArray   并行发送数据
 * mergeDelayError/concatDelayError
 * zip  多个被观察者压缩成单个的操作可以使用zip操作符，如果多个被观察者数量不同，则以少的为基准
 * combineLatest  类似zip操作符，但它合并时机和zip不一样，zip是一对一合并压缩，combineLatest则是在同一个时间线上，合并最后的元素
 * reduce 把一个被观察者的所有元素都聚合成单一的元素
 * count 统计被观察者发送的元素总数
 * collect
 * Created by LittleRoy on 2018/9/5.
 */
public class CombineTest {
    private static void log(String msg) {
        Log.e("Tag", msg);
    }

    /**
     * startWith 在被观察者发送数据之前追加新的数据或者新的被观察者
     * startWithArray 可以追加多个元素
     */
    public static void startWithTest() {
        Flowable.just("hello", "world", 7)
                .startWith(6)
                .startWith(Flowable.just(2, 3, 4, 5))
                .startWithArray("I", "love", "you")
                .subscribe(str -> log("startWith的輸出值：" + str));
    }

    /**
     * concat用于连接多个被观察者，最多是4个；
     * concatArray可以连接多个
     */
    public static void concatTest() {
        Flowable.concat(Flowable.just(1, 2, 3, 4),
                Flowable.just("Hello", "world"))
                .subscribe(str -> log("concat 输出：" + str));

        Flowable.concatArray(Flowable.just(7, 8, 9, 10),
                Flowable.just("I", "love", "you"))
                .subscribe(str -> log("concatArray 输出：" + str));
    }

    /**
     * merge主要用于合并被观察者，并行发送事件
     * mergeArray合并多个
     */
    public static void mergeTest() {
        Flowable.merge(Flowable.intervalRange(0, 2, 1, 1, TimeUnit.SECONDS),
                Flowable.intervalRange(2, 4, 2, 1, TimeUnit.SECONDS))
                .subscribe(str -> log("merge后结果" + str));
    }

    /**
     * mergeDelayError/concatDelayError
     * 在使用merge和concat的时候，如果遇到一个被观察者发送了onError事件，则会终止其他被观察者的事件
     * DelayError则可以延迟onError事件的发送，等其他的被观察者都结束事件发布后才触发
     */
    public static void delayError() {
        Flowable.mergeDelayError(
                Flowable.create(s -> s.onError(new NullPointerException()), BackpressureStrategy.ERROR),
                Flowable.intervalRange(3, 3, 1, 1, TimeUnit.SECONDS))
                .subscribe(ele -> log("mergeDelayError" + String.valueOf(ele)));
    }

    /**
     * zip将多个被观察者合并成一个，每个发送事件通过Function进行操作后发送，重写其apply方法
     * 1个被观察者 Function
     * 2个        BiFunction
     * 3-9个      Function3-Function9
     * 最多可以添加9个
     * 发送的数量以个数最少的那个被观察者发送的数量为准，一一对应的合并
     * 如下返回的是：1+4+6；2+5+7.由于第2个观察者的数量比较少，所以合并2次
     */
    public static void zipTest() {
        Flowable.zip(Flowable.just(1, 2, 3),
                Flowable.just(4, 5),
                Flowable.just(6, 7, 8, 9),
                new Function3<Integer, Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2, Integer integer3) throws Exception {
                        return integer + integer2 + integer3;
                    }
                }).subscribe(str -> log("zip输出：" + str));
    }

    /**
     * combineLatest只会合并最后一个被观察者的每个发送事件与之前的所有被观察者的最后一个事件
     * 如下返回的是3+5+6；3+5+7；3+5+8
     */
    public static void combineTest() {
        Flowable.combineLatest(Flowable.just(1, 2, 3),
                Flowable.just(4, 5),
                Flowable.just(6, 7, 8, 9),
                new Function3<Integer, Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2, Integer integer3) throws Exception {
                        return integer + integer2 + integer3;
                    }
                }).subscribe(str -> log("combine输出：" + str));
    }

    /**
     * reduce用于把元素前后两个元素进行相同的操作
     * 如下：3*4=12；12*5;
     */
    public static void reduceTest() {
        Flowable.just(3, 4, 5)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        return integer * integer2;
                    }
                })
                .subscribe(str -> log("reduce结果：" + str));
    }

    /**
     * count 发送事件数量
     */
    public static void countTest() {
        Flowable.just(1, 2, 3, 4, 6, 7, 8)
                .count()
                .subscribe(along -> log("count输出总数：" + along));
    }

    /**
     *  collect将事件收集到一个新的容器，不过容器需要自己定义
     */
    public static void collectTest() {
        Flowable.just(4, 5, 6)
                .collect(new Callable<ArrayList<Integer>>() {

                    @Override
                    public ArrayList<Integer> call() throws Exception {
                        return new ArrayList<>();
                    }
                }, new BiConsumer<ArrayList<Integer>, Integer>() {
                    @Override
                    public void accept(ArrayList<Integer> integers, Integer integer) throws Exception {
                        integers.add(integer);
                    }
                })
                .subscribe(along -> log("collect输出总数：" + along));
    }
}
