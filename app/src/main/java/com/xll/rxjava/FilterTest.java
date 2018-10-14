package com.xll.rxjava;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

/**
 * 过滤操作符
 * Created by LittleRoy on 2018/9/4.
 *
 * filter 过滤筛选不符合条件的操作符
 * take 限制发送的事件数量
 * first/last 当第一个/最后一个元素为空时，发送一个默认值
 * elementAt 指定发送第几个元素
 * ofType 选择发射指定类型数据
 * skip 跳过某些/某段时间的数据
 * ignoreElements 不关心发送的数据，只关心onComplete或onError
 * distinct 过滤重复数据
 * timeout 过滤超时数据
 * throttlexxx  某段时间内只有第一次或最后一次数据有效
 */
public class FilterTest {

    /**
     * filter过滤符
     * 内部有个筛选条件，过滤不符合条件的事件
     */
    public static void filterTest(){
        Flowable.just(1,2,3,4)
                .filter(integer -> integer>=2).subscribe(integer -> log("接受到的值："+integer));
    }

    /**
     * take过滤符
     * 只接收处理前面N个事件，然后执行onComplete()
     * takeLast
     * 只接受处理最后N个事件
     */
    public static void takeTest(){
        Flowable.just(1,1,2,2,3,4)
                .take(2).subscribe(integer -> log("接受到的值："+integer));
    }

    public static void takeTimeTest(){
        Flowable.interval(1, TimeUnit.SECONDS)
                .take(5, TimeUnit.SECONDS)//5秒之内的数据（这里输出0,1,2,3）
                .subscribe(integer -> log("接收到的值："+ String.valueOf(integer)));
    }

    /**
     * 在被观察者不为空的时候，firstElement与first的效果都是获取第一个元素
     * last与lastElement都是获取最后一个值
     */
    public static void lastTest(){
        Flowable.just(1,2,3,4)
                .firstElement()
                .subscribe(integer -> log("firstElement:"+integer));

        Flowable.just(1,2,3,4)
                .lastElement()
                .subscribe(integer -> log("lastElement:"+integer));

        Flowable.just(1,2,3,4)
                .first(6)
                .subscribe(integer -> log("first:"+integer));

        Flowable.just(1,2,3,4)
                .last(5)
                .subscribe(integer -> log("last:"+integer));
    }

    /**
     * 当被观察者为空时，firstElement与lastElement都不会发送事件,直接发送完成事件
     * first与last由于设置了默认值，此时都会发送一个默认值
     * firstOrError / lastOrError会在被观察者为空的时候发送error
     */
    public static void lastEmptyTest(){
        Flowable.empty()
                .firstElement()
                .subscribe(integer -> log("firstElement:"+integer),
                        onError->log("first onError"),
                        ()->log("firstonComplete"));

        Flowable.empty()
                .lastElement()
                .subscribe(integer -> log("last Element:"+integer),
                        onError->log("last  onError"),
                        ()->log("last onComplete"));

        Flowable.empty()
                .first(6)
                .subscribe(integer -> log("first:"+integer));

        Flowable.empty()
                .last(5)
                .subscribe(integer -> log("last:"+integer));
    }

    /**
     * elementAt 只发送第index位置的事件，未设置默认值时没有该位置的元素直接发送完成事件，否则发送默认值
     * elementAtOrError 没有该位置的元素时直接发送onError
     */
    public static void elementTest(){
        Flowable.just(1,2,3,4)
                .elementAt(5)
                .subscribe(integer -> log("get Element:"+integer),
                        onError->log("element onError"),
                        ()->log("element onComplete"));

        Flowable.just(1,2,3,4)
                .elementAt(5,7)
                .subscribe(integer -> log("get Element:"+integer),
                        onError->log("element onError"));

        Flowable.just(1,2,3,4)
                .elementAtOrError(5)
                .subscribe(integer -> log("get Element:"+integer),
                        onError->log("element onError"));
    }

    /**
     * ofType 筛选对应类型的数据发送，其他类型被抛弃
     */
    public static void ofTypeTest(){
        Flowable.just("hello",1,2,3,true,false,"world")
                .ofType(Boolean.class).subscribe(integer -> log("接受到的值："+integer));
    }

    /**
     * skip调过N个元素或者某段时间
     */
    public static void skipTest(){
        Flowable.just("a","b","c")
                .skip(1)
                .skipLast(1)
                .subscribe(ele -> log("跳过后最后的值："+String.valueOf(ele)));

        Flowable.intervalRange(0, 10, 1, 1, TimeUnit.SECONDS)
                .skip(3, TimeUnit.SECONDS)
                .skipLast(3, TimeUnit.SECONDS)
                .subscribe(ele -> log("跳过某段时间后的值："+String.valueOf(ele)));
    }

    /**
     * ignoreElements不关心发送的事件，只关心结束事件
     */
    public static void ignoreTest() {
        Flowable.just("a", "b", "c")
                .ignoreElements()
                .subscribe(()->log("element onComplete"),
                        onError->log("element onError"));
    }

    /**
     * distinct 去重
     *  distinctUtilChanged 只过滤连续重复的元素
     */
    public static void distinctTest() {
        Flowable.just("a","a", "b", "c", "b", "c")
                .distinct()
                .subscribe(str->log("distinct 接收到的值："+str),
                        onError->log("element onError"));

        Flowable.just("a","a", "b", "c", "b", "c")
                .distinctUntilChanged()
                .subscribe(str->log("distinctUtilChanged 接收到的值："+str),
                        onError->log("element onError"));
    }

    /**
     * timeout 过滤那些超时的操作
     */
    public static void timeoutTest(){
        Flowable.intervalRange(0, 10, 0, 2, TimeUnit.SECONDS)
                .timeout(1, TimeUnit.SECONDS)
                .subscribe(integer -> log("timeout 值:"+integer),
                        onError->log("timout onError"),
                        ()->log("timeout onComplete"));

        Flowable.intervalRange(0, 10, 0, 2, TimeUnit.SECONDS)
                .timeout(1, TimeUnit.SECONDS,  Flowable.just(-1L))
                .subscribe(integer -> log("timeout 有默认值:"+integer),
                        onError->log("timout 有默认值 onError"),
                        ()->log("timeout 有默认值 onComplete"));
    }

    /**
     * throttleFirst 一段时间内只处理第一个元素，后续的元素抛弃
     * throttleLast/sample  一段时间内只处理最后一个元素，前面的元素抛弃，最后触发onComplete
     * throttleWithTimeout/debounce 时间间隔内有收到新数据重新开始计时，每次新数据都会覆盖旧数据，直到超过时间结束
     */
    public static void throttleTest(){
//        Flowable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
//                .throttleFirst(1, TimeUnit.SECONDS)//每1秒中只处理第一个元素
//                .subscribe(integer -> log("throttleFirst:"+integer),
//                        onError->log("throttleFirst onError"),
//                        ()->log("throttleFirst onComplete"));

//        Flowable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
//                .throttleLast(2, TimeUnit.SECONDS)//每2秒中采集最后一个元素
//                .subscribe(integer -> log("throttleLast 有默认值:"+integer),
//                        onError->log("throttleLast 有默认值 onError"),
//                        ()->log("throttleLast 有默认值 onComplete"));
//
//        //等价于
//        Flowable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
//                .sample(2, TimeUnit.SECONDS)//每2秒中采集最后一个元素
//                .subscribe(integer -> log("sample 有默认值:"+integer),
//                        onError->log("sample 有默认值 onError"),
//                        ()->log("sample 有默认值 onComplete"));
//
        Flowable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
                .throttleWithTimeout(2, TimeUnit.SECONDS)//每2秒中采集最后一个元素,超时结束
                .subscribe(integer -> log("throttleWithTimeout 有默认值:"+integer),
                        onError->log("throttleWithTimeout 有默认值 onError"),
                        ()->log("throttleWithTimeout 有默认值 onComplete"));
//
//        Flowable.intervalRange(0, 10, 0, 1, TimeUnit.SECONDS)
//                .debounce(2, TimeUnit.SECONDS)//采集最后一个元素,超时结束
//                .subscribe(integer -> log("debounce 有默认值:"+integer),
//                        onError->log("debounce 有默认值 onError"),
//                        ()->log("debounce 有默认值 onComplete"));
    }
    private static void log(String msg){
        Log.e("Tag",msg);
    }
}
