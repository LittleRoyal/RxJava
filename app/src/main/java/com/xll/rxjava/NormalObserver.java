package com.xll.rxjava;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by LittleRoy on 2018/8/13.
 */
public class NormalObserver {
    public static final String TAG_OBSERVABLE = "observable";
    public static final String TAG_OBSERVAER = "observer";
    private static Disposable disposable;

    public static void normalTest() {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
//                emitter.onComplete();
                emitter.onError(new NullPointerException());
                emitter.onError(new NullPointerException());
                emitter.onNext(2);
                Log.e("TAG", "onComplete后继续发送");
            }
        });

        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                Log.e("TAG", "onNext:" + integer);
                if (integer > 2) {
                    disposable.dispose();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG", "onError:" + e.getLocalizedMessage());
            }

            @Override
            public void onComplete() {
                Log.e("TAG", "onComplete");
            }
        };
        observable.subscribe(observer);//默认Observable与Observer在同一线程;

//        observable.subscribe(new Consumer<Integer>() {
//            @Override
//            public void accept(Integer integer) throws Exception {
//                //相当于onNext
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                //相当于onError
//            }
//        }, new Action() {
//            @Override
//            public void run() throws Exception {
//                //相当于onComplete,由于没有参数，此处使用的是Action
//
//            }
//        }, new Consumer<Disposable>() {
//            @Override
//            public void accept(Disposable disposable) throws Exception {
//                //相当于onSubscrib()
//            }
//        });
    }


    public static void SchedulerTest() {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e("tag", "send values1,current thread is" + Thread.currentThread().getName());
                Log.e(TAG_OBSERVABLE, "subscribe:" + 1);
                emitter.onNext(1);
                Log.e("tag", "send values2,current thread is" + Thread.currentThread().getName());
                Log.e(TAG_OBSERVABLE, "subscribe:" + 2);
                emitter.onNext(2);
            }
        });

        Observer<Integer> observer = new Observer<Integer>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                Log.e(TAG_OBSERVAER, "onNext,当前value:" + integer);
                Log.e("tag", "receive values,current thread is" + Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG_OBSERVAER, "出错:" + e);

            }

            @Override
            public void onComplete() {
                Log.e(TAG_OBSERVAER, "完成");

            }
        };
        //设置上游发送事件在新的子线程，下游接受事件在主线程
        observable.subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e("tag", "After observeOn(mainThread),current thread is" + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e("tag", "After observeOn(Schedulers.io()),current thread is" + Thread.currentThread().getName());
                    }
                })
                .subscribe(observer);
    }


    /**
     * map关键词主要是将发送事件通过Map转换成另一种下游所需要的目标类型
     */
    public static void MapTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e(TAG_OBSERVABLE, "ObservableEmitter:" + Thread.currentThread().getName());
                Log.e(TAG_OBSERVABLE, "被处理之前的值:1");
                emitter.onNext(1);
                Log.e(TAG_OBSERVABLE, "被处理之前的值:2");
                emitter.onNext(2);
                emitter.onComplete();
                Log.e(TAG_OBSERVABLE, "被处理之前的值:3");
                emitter.onNext(3);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        Log.e(TAG_OBSERVABLE, "apply:" + Thread.currentThread().getName());
                        return "我被转换成:" + integer;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e(TAG_OBSERVABLE, "accept:" + Thread.currentThread().getName());
                Log.e(TAG_OBSERVAER, "接收的内容:" + s);
            }
        });
    }

    /**
     * zip关键词主要是将两个事件合并成一个事件提供给下游处理
     */
    public static void zipTest() {
        Observable<Integer> integerObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e(TAG_OBSERVABLE, "被处理之前的值:1");
                emitter.onNext(1);
                Log.e(TAG_OBSERVABLE, "被处理之前的值:2");
                emitter.onNext(2);
                Log.e(TAG_OBSERVABLE, "被处理之前的值:3");
                emitter.onNext(3);
            }
        });
        Observable<String> stringObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("我叫:");
                emitter.onNext("我女朋友叫:");
            }
        });

        Observable.zip(integerObservable, stringObservable, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return s + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e(TAG_OBSERVAER, "接收的内容:" + s);
            }
        });
    }

    /**
     * concat关键词主要是将多个事件串行组合成一个发射器,先注册的优先发送,按照先后顺序发送,
     * 前面一个Observable必须发送完事件后,后面的才开始发送(onComplete),这里就要求发送器的类型要一致了
     */
    public static void concatTest() {
        Observable<Integer> integerObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e(TAG_OBSERVABLE, "ObservableEmitter1:" + Thread.currentThread().getName());
                Thread.sleep(1000);
                Log.e(TAG_OBSERVABLE, "发射器1被处理之前的值:1");
                emitter.onNext(1);
                Log.e(TAG_OBSERVABLE, "发射器1被处理之前的值:2");
                emitter.onNext(2);
                emitter.onComplete();
                Log.e(TAG_OBSERVABLE, "发射器1被处理之前的值:3");
                emitter.onNext(3);
            }
        }).subscribeOn(Schedulers.newThread());
        Observable<Integer> integerObservable2 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e(TAG_OBSERVABLE, "ObservableEmitter2:" + Thread.currentThread().getName());
                Log.e(TAG_OBSERVABLE, "发射器2被处理之前的值:5");
                emitter.onNext(5);
                Log.e(TAG_OBSERVABLE, "发射器2被处理之前的值:6");
                emitter.onNext(6);
                Log.e(TAG_OBSERVABLE, "发射器2被处理之前的值:7");
                emitter.onNext(7);
            }
        }).subscribeOn(Schedulers.io());

        Observable.concat(integerObservable, integerObservable2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG_OBSERVABLE, "accept:" + Thread.currentThread().getName());
                        Log.e(TAG_OBSERVAER, "接收到的值:" + integer);
                    }
                });
    }

    /**
     * flatMap主要是将单个发射器转换成多个发射器,事件数量增加.但是它是无序的;
     * concatMap则是有序的.
     */
    public static void flatTest() {
        Observable<Integer> integerObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e(TAG_OBSERVABLE, "ObservableEmitter:" + Thread.currentThread().getName());
                Log.e(TAG_OBSERVABLE, "发射器1被处理之前的值:1");
                emitter.onNext(1);
                Log.e(TAG_OBSERVABLE, "发射器1被处理之前的值:2");
                emitter.onNext(2);
                Log.e(TAG_OBSERVABLE, "发射器1被处理之前的值:3");
                emitter.onNext(3);
                emitter.onComplete();
            }
        });

        integerObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .flatMap(new Function<Integer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(Integer integer) throws Exception {
                        Log.e(TAG_OBSERVABLE, "apply:" + Thread.currentThread().getName());
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < 2; i++) {
                            list.add("我的值:" + integer);
                        }
                        int delayTime = (int) (1 + Math.random() * 10);
                        return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.e(TAG_OBSERVABLE, "accept:" + Thread.currentThread().getName());
                        Log.e(TAG_OBSERVAER, "接收到的:" + s);
                    }
                });
    }
}
