package com.xll.rxdemo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.xll.rxdemo.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by LittleRoy on 2018/9/26.
 */
public class BackgroundActivity extends AppCompatActivity {
    private TextView currentTextView;
    private EditText searchText;
    private Disposable disposable;
    private int n=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentTextView = findViewById(R.id.text);
        searchText = findViewById(R.id.edit_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                startSearch(editable.toString());
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdateProgress();
            }
        });
    }

    private void startSearch(String s) {
        if (TextUtils.isEmpty(s)) {
            return;
        }
        //当上游发送onComplete之后repeateWhen开始执行
        Observable<Long> observable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                emitter.onNext(0L);
                emitter.onComplete();
            }
        }).repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Object o) throws Exception {
                        Log.d("TAG", "repeatWhen:" + n);
                        if (n != 3) {
                            n++;
                            return Observable.timer(1, TimeUnit.SECONDS);
                        } else {
                            return Observable.empty();
                        }
                    }
                });
            }
        });
        DisposableObserver<Long> disposableObserver = new DisposableObserver<Long>() {
            @Override
            public void onNext(Long aLong) {
                Log.d("TAG", "onNext:" + aLong);
            }

            @Override
            public void onError(Throwable e) {

                Log.d("TAG", "onError" + e.getLocalizedMessage());
            }

            @Override
            public void onComplete() {

                Log.d("TAG", "onComplete");
            }
        };
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);

    }

    private void startUpdateProgress() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
                .flatMap(new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Long aLong) throws Exception {
                        return Observable.just(aLong + 1);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        currentTextView.setText("");
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (aLong < 100) {
                            currentTextView.setText("当前进度：" + aLong);
                        } else if (disposable != null) {
                            disposable.dispose();
                            currentTextView.setText("更新完毕！");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
