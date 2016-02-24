package io.hypertrack.meta.view;

import android.view.View;

public interface MvpView {
    void attachView(View view);
    void detachView();
}
