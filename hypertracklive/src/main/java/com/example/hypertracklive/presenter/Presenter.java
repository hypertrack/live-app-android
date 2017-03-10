package com.example.hypertracklive.presenter;

public interface Presenter<V> {
    void attachView(V view);
    void detachView();
}
