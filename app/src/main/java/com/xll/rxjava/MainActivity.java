package com.xll.rxjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.request_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscription.request(100);
            }
        });
        NormalObserver.concatTest();
    }

    private void setObservable() {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e("observer", "subscribe:" + 1);
                emitter.onNext(1);
                Log.e("observer", "subscribe:" + 21);
                emitter.onNext(21);
                Log.e("observer", "subscribe:" + 23);
                emitter.onError(new NullPointerException());
                emitter.onNext(23);
                Log.e("observer", "subscribe:" + "onCompelete");
                emitter.onComplete();
                Log.e("observer", "subscribe:" + 25);
                emitter.onNext(25);

            }
        });

        Observer<Integer> observer = new Observer<Integer>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                Log.e("observer", "订阅" + Thread.currentThread().getName());
                disposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                Log.e("observer", "当前value" + integer);
                Log.e("observer", "onNext Thread:" + Thread.currentThread().getName());
//                disposable.dispose(); //停止下游的事件接受
            }

            @Override
            public void onError(Throwable e) {
                Log.e("observer", "出错");

            }

            @Override
            public void onComplete() {
                Log.e("observer", "完成");

            }
        };
//        observable.subscribe(observer);//Observable与Observer在同一线程;

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.e("observer", "consumer:" + Thread.currentThread().getName());
                Log.e("observer", "onNext值:" + integer);
            }
        };

//        observable.subscribeOn(Schedulers.newThread())//指定上游线程,上游线程都是以第一个指定的为准
//                .observeOn(Schedulers.newThread())//指定下游线程,每次注册都会切换一次
//                .observeOn(Schedulers.io())
//                .subscribe(observer);
        observable.subscribe(observer);
    }

    /**
     * zip操作符主要是用于将多个发布事件进行组合
     */
    public void setZipObserverable() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.e("Observable", "subscribe1:" + 1);
                emitter.onNext(1);
                Log.e("Observable", "subscribe1:" + 2);
                emitter.onNext(2);
                Log.e("Observable", "subscribe1:" + 3);
                emitter.onNext(3);
                Log.e("Observable", "subscribe1:" + 4);
                emitter.onNext(4);
                emitter.onComplete();
            }
        }).observeOn(Schedulers.newThread());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.e("Observable", "subscribe2:" + "one");
                emitter.onNext("One");
                Log.e("Observable", "subscribe2:" + "two");
                emitter.onNext("two");
                Log.e("Observable", "subscribe2:" + "three");
                emitter.onNext("three");
                emitter.onComplete();
            }
        }).observeOn(Schedulers.newThread());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.e("observer", "onNext:" + s);

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.e("observer", "onComplete:");
            }
        });

    }

    /**
     * 同步订阅中,上游的FlowableEmitter事件处理能力完全跟下游设置的request的数量一致,多次调用是进行累加操作的;
     * 异步订阅中,上游的FlowableEmitter事件处理能力每次会在下游处理完96个事件之后同步一次;
     * <p>
     * 同步订阅中,下游不设置Subscription.request(num),下游就不会接受处理事件,
     * 会直接报错:MissingBackpressureException: create: could not emit value due to lack of requests
     * 异步订阅中,下游不设置request数量,Flowable默认request=128个事件,超出的也会报上述错误,但是后续还是会继续发送,
     * 下游可以从Flowable中获取到前面128个事件,后面的直接被丢弃处理
     * <p>
     * BackpressureStrategy.BUFFER表示对于上游的存储事件能力没有128个事件数量的限制
     * BackpressureStrategy.DROP
     * 异步:暂时丢弃不能处理的事件,剩下的下次再处理Flowable中存储的事件
     * <p>
     * BackpressureStrategy.LATEST
     * 异步情况下:从Flowable中存储的事件取,第一次处理request数量的事件,以后每次触发只处理最后一个事件
     * 同步情况下:每次都是处理最后一个事件
     * <p>
     * BackpressureStrategy.ERROR  处理不完的直接报错
     * <p>
     * 以下的方式提供给SDK自带的Flowable设置事件处理策略
     * onBackpressureBuffer()
     * onBackpressureDrop()
     * onBackpressureLatest()
     */
    public void getFlowable() {
        Flowable<Integer> upstream = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Log.e("subscribe", "Subscription request = " + emitter.requested());
                for (int i = 0; ; i++) {
                    emitter.onNext(i);
                    Thread.sleep(100);
                    Log.e("subscribe", "emit " + i + " , requested = " + emitter.requested());
                }
            }
        }, BackpressureStrategy.BUFFER);

        final Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.e("onSubscribe", "onSubscribe");
                subscription = s;
//                s.request(8);
            }

            @Override
            public void onNext(Integer integer) {
                Log.e("subscriber", "onNext:" + integer);
            }

            @Override
            public void onError(Throwable t) {
                Log.e("onError", "出错了" + t);
            }

            @Override
            public void onComplete() {
                Log.e("onComplete", "onComplete完成咯");
            }
        };
        upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
//        upstream.subscribe(subscriber);
    }
}
