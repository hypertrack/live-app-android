package com.hypertrack.lib.internal.consumer.models;

/**
 * Created by piyush on 25/07/16.
 */
public interface UpdateDestinationCallback {
    void onSuccess(HTTask task);
    void onError(Exception exception);
}
