package com.xll.rxjava;

import android.util.Log;

import org.reactivestreams.Publisher;

import java.util.Arrays;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * 变换操作符
 * Created by LittleRoy on 2018/9/12.
 * map 通过Function将原来发射的元素进行转换
 * flatMap 将每一个元素换成新的被观察者，注意是每一个元素哦
 * flatMapIterable 把每一个元素转换成一个Iterable
 * concatMap 同flatMap,顺序有保证
 * switchMap 转换的每一个数据都会取代前一个被观察者
 * cast 类型强转
 * scan 从第二个元素开始进行变换发送
 * buffer 将N个数据打包成一个数组进行发送
 * toList 把所有元素转换成一个list发送
 *
 */
public class MapTest {
    private static void log(String msg) {
        Log.e("Tag", msg);
    }

    /**
     * map 将发射的事件转换成新的元素进行发射
     */
    public static void MapTest(){
        Flowable.just(1,2,3,4)
                .map(integer -> "使用map转换后的结果："+integer)
                .subscribe(result->log(result));
    }

    /**
     * flatMap 将原来发射的事件结合flatMap的发射事件组合发射
     * 内部采用的merge组合，组合没有顺序
     */
    public static void flatMap(){
        Flowable.just(1,2,3,4)
                .flatMap(new Function<Integer, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Integer integer) throws Exception {
                        return Flowable.just("hello", integer);
                    }
                })
                .subscribe(integer -> log("输出flatMap结果："+integer));
    }

    /**
     * flatMapIterable 将原来的发射事件与新的发射事件组成一个List发布
     */
    public static void flatMapIterable(){
        Flowable.just(1,2,3,4)
                .flatMapIterable(new Function<Integer, Iterable<?>>() {
                    @Override
                    public Iterable<?> apply(Integer integer) throws Exception {
                        return Arrays.asList("hello",integer);
                    }
                })
                .subscribe(integer -> log("输出flatMapIterable结果："+integer));

    }

    /**
     * concatMap 将原来发射的事件结合新的发射事件组合发射
     * 内部采用的concat组合，组合严格有顺序
     */
    public static void concatMap(){
        Flowable.just(1,2,3,4)
                .concatMap(new Function<Integer, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Integer integer) throws Exception {
                        return Flowable.just("hello", integer);
                    }
                })
                .subscribe(integer -> log("输出concatMap结果："+integer));
    }

    /**
     * switchMap用法与flatMap类似，但是转换出来的每一个新的数据（被观察者）发射会取代掉前一个被观察者
     */
    public static void switchMap(){
        Flowable.just(1,2,3,4)
                .switchMap(new Function<Integer, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Integer integer) throws Exception {
                        return Flowable.just("hello",integer);
                    }
                })
                .subscribe(str->log(str.toString()));
    }

    /**
     * cast 类型强制转换
     */
    public static void castTest(){
        Flowable.just(1,2,3,4)
                .cast(String.class)
                .subscribe(result->log(result));
    }

    /**
     * scan 类型强制转换
     */
    public static void scanTest(){
        Flowable.just(1, 2, 3)
                .scan((last, item) -> {
                    Log.i("tag", "last:" + String.valueOf(last));
                    Log.i("tag", "item:" + String.valueOf(item));
                    return item+1;
                })
                .subscribe(ele -> Log.i("tag", String.valueOf(ele)));
    }

    /**
     * 将N个数据组合在一起形成一个list发送
     */
    public static void buffer(){
        Flowable.just(1, 2, 3, 4, 5)
                .buffer(3)//三个元素打包成一个元素
                .subscribe(intList -> Log.i("tag", intList.toString()));
    }

    /**
     * 将所有数据组合在一起形成一个list发送
     */
    public static void toList(){
        Flowable.just(1, 2, 3, 4, 5)
                .toList()
                .subscribe(intList -> Log.i("tag", intList.toString()));
    }

}
