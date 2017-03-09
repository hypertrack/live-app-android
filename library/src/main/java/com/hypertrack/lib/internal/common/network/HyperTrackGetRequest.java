package com.hypertrack.lib.internal.common.network;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by piyush on 01/12/16.
 */
public class HyperTrackGetRequest<T> extends HyperTrackNetworkRequest<T> {
    private final MQTTMessageArrivedCallback messageArrivedCallback;
    private final MQTTSubscriptionSuccessCallback subscriptionSuccessCallback;
    private final String lastWillUserID;

    public HyperTrackGetRequest(@NonNull String TAG, Context context, @NonNull String url,
                                @NonNull HTNetworkClient networkClient, @NonNull Class<T> responseType,
                                HTNetworkResponse.Listener<T> mListener, HTNetworkResponse.ErrorListener mErrorListener) {
        this(TAG, context, url, networkClient, responseType, mListener, mErrorListener, null, null, null);
    }

    public HyperTrackGetRequest(@NonNull String TAG, Context context, @NonNull String url,
                                @NonNull HTNetworkClient networkClient, @NonNull Class<T> responseType,
                                HTNetworkResponse.Listener<T> mListener, HTNetworkResponse.ErrorListener mErrorListener,
                                MQTTMessageArrivedCallback messageArrivedCallback) {
        this(TAG, context, url, networkClient, responseType, mListener, mErrorListener,
                messageArrivedCallback, null, null);
    }

    public HyperTrackGetRequest(@NonNull String TAG, Context context, @NonNull String url,
                                @NonNull HTNetworkClient networkClient, @NonNull Class<T> responseType,
                                HTNetworkResponse.Listener<T> mListener, HTNetworkResponse.ErrorListener mErrorListener,
                                MQTTMessageArrivedCallback messageArrivedCallback, MQTTSubscriptionSuccessCallback subscriptionSuccessCallback) {
        this(TAG, context, url, networkClient, responseType, mListener, mErrorListener,
                messageArrivedCallback, subscriptionSuccessCallback, null);
    }

    public HyperTrackGetRequest(@NonNull String TAG, Context context, @NonNull String url,
                                @NonNull HTNetworkClient networkClient, @NonNull Class<T> responseType,
                                HTNetworkResponse.Listener<T> mListener, HTNetworkResponse.ErrorListener mErrorListener,
                                MQTTMessageArrivedCallback messageArrivedCallback, MQTTSubscriptionSuccessCallback subscriptionSuccessCallback,
                                String lastWillUserID) {
        super(TAG, context, HTNetworkRequestType.GET, url, networkClient, responseType,
                mListener, mErrorListener);
        this.messageArrivedCallback = messageArrivedCallback;
        this.subscriptionSuccessCallback = subscriptionSuccessCallback;
        this.lastWillUserID = lastWillUserID;
    }

    public MQTTMessageArrivedCallback getMessageArrivedCallback() {
        return messageArrivedCallback;
    }

    public MQTTSubscriptionSuccessCallback getSubscriptionSuccessCallback() {
        return subscriptionSuccessCallback;
    }

    public String getLastWillUserID() {
        return lastWillUserID;
    }
}
