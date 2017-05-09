package io.hypertrack.sendeta.presenter;

public interface Presenter<V> {
    void attachView(V view);
    void detachView();
}
