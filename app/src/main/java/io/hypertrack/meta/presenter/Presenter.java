package io.hypertrack.meta.presenter;

public interface Presenter<V> {
    void attachView(V view);
    void detachView();
}
