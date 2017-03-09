package com.hypertrack.lib.internal.consumer.models;


/**
 * Created by suhas on 25/11/15.
 */
public interface HTActionCallBack {
    void onSuccess(HTAction action);

    void onError(Exception exception);
}
