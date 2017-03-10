package com.hypertrack.lib.internal.common.network;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by piyush on 01/12/16.
 */
public class HyperTrackPostRequest<T> extends HyperTrackNetworkRequest<T> {
    private final Object requestBody;
    private final boolean isRetained;

    public HyperTrackPostRequest(@NonNull String TAG, Context context, @NonNull String url,
                                 @NonNull HTNetworkClient networkClient, Object requestBody,
                                 @NonNull Class<T> responseType, HTNetworkResponse.Listener<T> mListener,
                                 HTNetworkResponse.ErrorListener mErrorListener) {
        this(TAG, context, url, networkClient, requestBody, responseType, mListener, mErrorListener, false);
    }

    public HyperTrackPostRequest(@NonNull String TAG, Context context, @NonNull String url,
                                 @NonNull HTNetworkClient networkClient, Object requestBody,
                                 @NonNull Class<T> responseType, HTNetworkResponse.Listener<T> mListener,
                                 HTNetworkResponse.ErrorListener mErrorListener, boolean isRetained) {
        super(TAG, context, HTNetworkRequestType.POST, url, networkClient, responseType,
                mListener, mErrorListener);
        this.requestBody = requestBody;
        this.isRetained = isRetained;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public boolean isRetained() {
        return isRetained;
    }
}
