package com.xll.rxjava;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

import static com.xll.rxjava.NormalObserver.TAG_OBSERVABLE;

/**
 * Created by LittleRoy on 2018/8/15.
 */
public class SinglesObserver {
    /**
     * Single表示只能接受单个事件,只有onSuccess或onError
     *
     *
     */
    public static void singleTest(){
        Single<Integer> integerObservable2=Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(SingleEmitter<Integer> emitter) throws Exception {
                Log.e(TAG_OBSERVABLE,"发射器2被处理之前的值:5");
                emitter.onSuccess(5);
                Log.e(TAG_OBSERVABLE,"发射器2被处理之前的值:6");
                emitter.onSuccess(6);
                Log.e(TAG_OBSERVABLE,"发射器2被处理之前的值:7");
                emitter.onSuccess(7);
            }
        });
        integerObservable2.subscribe(new io.reactivex.SingleObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG_OBSERVABLE,"singleObserve注册");

            }

            @Override
            public void onSuccess(Integer integer) {
                Log.e(TAG_OBSERVABLE,"接收的值:"+integer);
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    /**
     * debounce 去除发送频率过快的项
     */
    public static void debounceTest(){
        Observable<Integer> integerObservable=Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e(TAG_OBSERVABLE,"发射器1被处理之前的值:1");
                emitter.onNext(1);
                Thread.sleep(100);
                Log.e(TAG_OBSERVABLE,"发射器1被处理之前的值:2");
                emitter.onNext(2);
                Thread.sleep(200);
                Log.e(TAG_OBSERVABLE,"发射器1被处理之前的值:3");
                emitter.onNext(3);
                emitter.onComplete();
            }
        });

        integerObservable.debounce(150, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG_OBSERVABLE,"接收的值:"+integer);
                    }
                });
    }

    /**
     * reduce主要是将发送事件前后进行特定的方法处理,最后下游只接受到一个
     * scan作用于reduce一样,不过每次return的结果都会发送给下游
     */
    public static void reduceTest(){
        Observable.just(1,2,3,4,5,6)
                .scan(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        Log.e(TAG_OBSERVABLE,"reduce过程:"+(integer2-integer));
                        return integer2-integer;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG_OBSERVABLE,"结果:"+integer);
                    }
                });
    }

    /**
     * 观察者不关心onNext事件，直接onComplete/onError
     */
    public static void completableTest(){
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                emitter.onComplete();
                emitter.onError(new Throwable());
            }
        }).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    public static void MaybeObserveTest(){
        Maybe.create(new MaybeOnSubscribe<String>() {

            @Override
            public void subscribe(MaybeEmitter<String> emitter) throws Exception {
                //最多只能发送一次onSuccess,如果发送了事件，onComplete不再触发
                //onError不能跟其他事件同时发送
                emitter.onSuccess("大宋王朝");
                emitter.onComplete();
//                emitter.onError(new Throwable());
            }
        }).subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String s) {
                Log.e(TAG_OBSERVABLE,"接收到的结果:"+s);


            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG_OBSERVABLE,"抛出异常");

            }

            @Override
            public void onComplete() {
                Log.e(TAG_OBSERVABLE,"发送完成");

            }
        });
    }
}
