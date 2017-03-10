package com.hypertrack.lib.internal.common.network;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by piyush on 19/09/16.
 */
public abstract class HyperTrackNetworkRequest<T> {

    private final Context context;
    private final String TAG;
    private final String url;
    private final HTNetworkClient networkClient;
    private final int networkRequestType;
    private final Class<T> responseType;
    private final HTNetworkResponse.Listener<T> mListener;
    private final HTNetworkResponse.ErrorListener mErrorListener;

    public enum HTNetworkClient {
        HT_NETWORK_CLIENT_HTTP("ht_network_client_http"),
        HT_NETWORK_CLIENT_MQTT("ht_network_client_mqtt");

        private String htNetworkClientType;

        HTNetworkClient(String htNetworkClientType) {
            this.htNetworkClientType = htNetworkClientType;
        }

        public String toString() {
            return this.htNetworkClientType;
        }
    }

    public HyperTrackNetworkRequest(@NonNull String TAG, Context context, int networkRequestType,
                                    @NonNull String url, @NonNull HTNetworkClient networkClient,
                                    @NonNull Class<T> responseType, HTNetworkResponse.Listener<T> mListener,
                                    HTNetworkResponse.ErrorListener mErrorListener) {
        this.TAG = TAG;
        this.context = context;
        this.networkRequestType = networkRequestType;
        this.url = url;
        this.networkClient = networkClient;
        this.responseType = responseType;
        this.mListener = mListener;
        this.mErrorListener = mErrorListener;
    }

    public Context getContext() {
        return context;
    }

    public String getTAG() {
        return TAG;
    }

    public String getUrl() {
        return url;
    }

    public HTNetworkClient getNetworkClient() {
        return networkClient;
    }

    public int getNetworkRequestType() {
        return networkRequestType;
    }

    public Class<T> getResponseType() {
        return responseType;
    }

    public HTNetworkResponse.Listener<T> getListener() {
        return mListener;
    }

    public HTNetworkResponse.ErrorListener getErrorListener() {
        return mErrorListener;
    }

    public interface HTNetworkRequestType {
        int INVALID = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }
}
