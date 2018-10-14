package com.xll.rxjava;

import android.util.Log;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.BiPredicate;
import io.reactivex.functions.Predicate;

/**
 * 条件变换操作符
 * Created by LittleRoy on 2018/9/6.
 * all 判断所有元素是否满足某个条件，返回boolean
 * ambArray 只接受最先发射的那个事件所在的被观察者
 * contains 是否包含某个元素
 * any 是否包含满足某个条件的元素
 * isEmpty用于校验是否发送过事件
 * defaultIfEmpty 当不发送数据的时候发送一个默认元素
 * switchIfEmpty 当不发送数据的时候设置一个默认被观察者
 * sequenceEqual 判断两个被观察者是否一致
 * takeUntil 触发某个条件即停止发送
 * skipUntil 触发某个条件才开始发送
 *
 */
public class ConditionTest {

    private static void log(String msg) {
        Log.e("Tag", msg);
    }

    /**
     * all 用于判断事件是否全部满足某个条件，如果是，返回true
     */
    public static void allTest() {
        Flowable.just(9, 2, 3, 4, 5, 6)
                .all(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        log("检测内容：" + integer);
                        return integer > 4;
                    }
                })
                .subscribe(ele -> log("事件是否全部都符合规则：" + ele));
    }

    /**
     * ambArray 只接受最先发射的那个事件所在的被观察者
     * 如下，只会接收1，2，3，4这组数据，因为第1组数据要延迟1s,后面一组注册比较晚
     */
    public static void ambArrayTest() {
        Flowable.ambArray(Flowable.timer(1, TimeUnit.SECONDS),
                Flowable.just(1, 2, 3, 4),
                Flowable.just(8, 8, 9))
                .subscribe(result -> log("ambArray输出：" + result));
    }

    /**
     * contains 是否包含某个元素
     */
    public static void containsTest() {
        Flowable.just(9, 2, 3, 4, 5, 6)
                .contains("hello")
                .subscribe(ele -> log("事件是否包含‘hello’：" + ele));
    }

    /**
     * any 是否包含满足某个条件的元素
     * 与all不同的是，all是判断所有元素是否都满足
     */
    public static void anyTest() {
        Flowable.just(9, 2, 3, 4, 5, 6)
                .any(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer % 2 == 0;
                    }
                })
                .subscribe(ele -> log("事件是否有满足条件的元素：" + ele));
    }

    /**
     * isEmpty用于校验是否发送过事件
     * defaultIfEmpty 当不发送数据的时候发送一个默认元素
     * switchIfEmpty 当不发送数据的时候设置一个默认被观察者
     */
    public static void isEmptyTest() {
        Flowable.just(1, 2, 3, 4)
                .isEmpty()
                .subscribe(ele -> log("是否没有事件发送：" + ele));
        Flowable.empty()
                .defaultIfEmpty("Hello")
                .subscribe(ele -> log("没有事件发送时，接收的默认值：" + ele));
    }

    /**
     * sequenceEqual 根据BiPredicate判断规则，判断两个发射队列的元素、元素发射顺序、和最终状态，而不关心他的时间
     */
    public static void sequenceTest() {
        Flowable.sequenceEqual(
                Flowable.just(0L, 1L, 2L),
                Flowable.intervalRange(0, 3, 0, 1, TimeUnit.SECONDS),
                new BiPredicate<Long, Long>() {
                    @Override
                    public boolean test(Long aLong, Long aLong2) throws Exception {
                        //这里可以控制比较规则
                        return aLong == aLong2;
                    }
                }
        )
                .subscribe(ele -> log("两个被观察者是否一致：：" + ele));
    }

    /**
     * takeUntil 直到满足某个条件才停止发送元素，包含当前符合条件的
     * 范例1结果：1，2，3，4
     * 范例2结果：0，1，2，3，4，5，6，7，8 当takeUntil内的被观察者发送完前面的观察者也停止发送
     * <p>
     * takeWhile 与takeUntil类似，takeWhile只接受Predicate，只有第一个发送事件满足predicate才会发送事件
     */
    public static void takeUntilTest() {
        Flowable.just(1,2,3,4,4,5)
                .takeUntil(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer==4;
                    }
                })
                .subscribe(ele -> log("符合条件的元素：" + ele));

        Flowable.just( 4, 4, 5)
                .takeWhile(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        log("处理事件："+integer);
                        return integer == 4;
                    }
                })
                .subscribe(ele -> log("takeWhile符合条件的元素：" + ele),
                        error -> log("onError"),
                        () -> log("onComplete"));

        Flowable.interval(100, TimeUnit.MILLISECONDS)
                .takeUntil(Flowable.timer(1, TimeUnit.SECONDS))
                .subscribe(ele -> log("takeUntil接收到的元素：" + ele));
    }

    /**
     * skipUntil 操作符接收一个被观察者，直到该被观察者发送事件之前，第一个被观察者所有发送的元素将被抛弃
     * 范例1 结果：3，4（由于skipUntil的延迟了3s才开始发送事件，所以，前面的0，1，2被抛弃）
     *
     * skipWhile 过滤在发射前满足条件的事件，一旦开始发送，后面的事件不再进行条件筛选
     * 范例2 结果 1，2，3，4 （由于第一个元素0符合筛选条件，所以0被抛弃，送去筛选的元素0，1两个）
     */
    public static void skipUntilTest(){
        Flowable.intervalRange(0,5,0,1,TimeUnit.SECONDS)
                .skipUntil(Flowable.timer(3,TimeUnit.SECONDS))
                .subscribe(ele -> log("跳过接收到的元素：" + ele));

        Flowable.intervalRange(0,5,0,1,TimeUnit.SECONDS)
                .skipWhile(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        log("筛选的元素："+aLong);
                        return aLong%2==0;
                    }
                })
                .subscribe(ele -> log("skipWhile跳过后的元素：" + ele));
    }

}
