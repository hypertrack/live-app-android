package com.hypertrack.lib.internal.consumer.models;

/**
 * Created by suhas on 25/11/15.
 */
public interface HTTaskCallBack {
    void onSuccess(HTTask task);
    void onError(Exception exception);
}
