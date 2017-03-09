package com.hypertrack.lib.internal.common.network;

import android.content.Context;
import android.os.Build;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.exception.NoConnectionException;
import com.hypertrack.lib.internal.common.exception.NoResponseException;
import com.hypertrack.lib.internal.common.exception.ServerException;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.common.util.Utils;
import com.hypertrack.lib.internal.consumer.models.HTAction;
import com.hypertrack.lib.internal.transmitter.controls.SDKControls;
import com.hypertrack.lib.models.HyperTrackUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by piyush on 19/09/16.
 */
public class HTTPClient {

    private static final String TAG = HTTPClient.class.getSimpleName();
    private static final int TIMEOUT_MILLISECONDS = 30000;
    private static final int mMaxRetries = -1;

    private Context mContext;
    private RequestQueue mRequestQueue;
    private String mTag;

    public HTTPClient(Context context, String tag) {
        this.mContext = context;
        this.mTag = tag;
    }

    private HashMap<String, String> getRequestHeaders(HyperTrackNetworkRequest networkRequest) {

        final HashMap<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.putAll(Utils.getBatteryHeader(mContext));
        additionalHeaders.putAll(Utils.getDeviceHeader());

        HashMap<String, String> params = new HashMap<>();
        String token = HyperTrack.getPublishableKey(mContext);
        params.put("Authorization", "Token " + (token != null ? token : ""));
        params.put("User-Agent", "HyperTrack " + HyperTrack.getSDKPlatform() + " " +
                BuildConfig.SDK_VERSION_NAME + " (Android " + Build.VERSION.RELEASE + ")");
        params.put("Device-Time", DateTimeUtility.getCurrentTime());

        for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
            params.put(header.getKey(), header.getValue());
        }

        return params;
    }

    public void executeHttpPOST(final HyperTrackPostRequest httpPostRequest) {
        HTTPPostRequest request = getHTCustomPostRequest(httpPostRequest);
        if (request != null)
            addToRequestQueue(request, httpPostRequest.getTAG());
    }

    public void executeHttpGET(final HyperTrackGetRequest httpGetRequest) {
        HTTPGetRequest request = getHTCustomGetRequest(httpGetRequest);
        if (request != null)
            addToRequestQueue(request, httpGetRequest.getTAG());
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private HTTPPostRequest getHTCustomPostRequest(final HyperTrackPostRequest httpPostRequest) {
        try {
            final Class responseType = httpPostRequest.getResponseType();

            // Check requestType of NetworkRequest
            if (responseType == JSONArray.class) {
                return new HTTPPostRequest<>(httpPostRequest.getUrl(), getRequestHeaders(httpPostRequest),
                        httpPostRequest.getRequestBody().toString(), this.mContext, JSONArray.class,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (httpPostRequest.getListener() != null) {
                                    httpPostRequest.getListener().onResponse(response);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                processError(error, httpPostRequest.getErrorListener());
                            }
                        });
            } else if (responseType == JSONObject.class) {
                return new HTTPPostRequest<>(httpPostRequest.getUrl(), getRequestHeaders(httpPostRequest),
                        httpPostRequest.getRequestBody().toString(), this.mContext, JSONObject.class,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (httpPostRequest.getListener() != null) {
                                    httpPostRequest.getListener().onResponse(response);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                processError(error, httpPostRequest.getErrorListener());
                            }
                        });
            } else if (responseType == SDKControls.class) {
                return new HTTPPostRequest<>(httpPostRequest.getUrl(), getRequestHeaders(httpPostRequest),
                        httpPostRequest.getRequestBody().toString(), this.mContext, SDKControls.class,
                        new Response.Listener<SDKControls>() {
                            @Override
                            public void onResponse(SDKControls response) {
                                if (httpPostRequest.getListener() != null) {
                                    httpPostRequest.getListener().onResponse(response);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                processError(error, httpPostRequest.getErrorListener());
                            }
                        });
            } else if (responseType == HTAction.class) {
                return new HTTPPostRequest<>(httpPostRequest.getUrl(), getRequestHeaders(httpPostRequest),
                        httpPostRequest.getRequestBody().toString(), this.mContext, HTAction.class,
                        new Response.Listener<HTAction>() {
                            @Override
                            public void onResponse(HTAction response) {
                                if (httpPostRequest.getListener() != null) {
                                    httpPostRequest.getListener().onResponse(response);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                processError(error, httpPostRequest.getErrorListener());
                            }
                        });
            } else if (responseType == HyperTrackUser.class) {
                return new HTTPPostRequest<>(httpPostRequest.getUrl(), getRequestHeaders(httpPostRequest),
                        httpPostRequest.getRequestBody().toString(), this.mContext, HyperTrackUser.class,
                        new Response.Listener<HyperTrackUser>() {
                            @Override
                            public void onResponse(HyperTrackUser response) {
                                if (httpPostRequest.getListener() != null) {
                                    httpPostRequest.getListener().onResponse(response);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                processError(error, httpPostRequest.getErrorListener());
                            }
                        });
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getHTCustomPostRequest: " + e);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return null;
    }

    private HTTPGetRequest getHTCustomGetRequest(final HyperTrackGetRequest httpGetRequest) {

        final Class responseType = httpGetRequest.getResponseType();

        // Check requestType of NetworkRequest
        if (responseType == JSONArray.class) {
            return new HTTPGetRequest<>(httpGetRequest.getUrl(), getRequestHeaders(httpGetRequest),
                    null, this.mContext, JSONArray.class,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if (httpGetRequest.getListener() != null) {
                                httpGetRequest.getListener().onResponse(response);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            processError(error, httpGetRequest.getErrorListener());
                        }
                    });
        } else if (responseType == HTAction.class) {
            return new HTTPGetRequest<>(httpGetRequest.getUrl(), getRequestHeaders(httpGetRequest),
                    null, this.mContext, HTAction.class,
                    new Response.Listener<HTAction>() {
                        @Override
                        public void onResponse(HTAction response) {
                            if (httpGetRequest.getListener() != null) {
                                httpGetRequest.getListener().onResponse(response);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            processError(error, httpGetRequest.getErrorListener());
                        }
                    });
        } else if (responseType == SDKControls.class) {
            return new HTTPGetRequest<>(httpGetRequest.getUrl(), getRequestHeaders(httpGetRequest),
                    null, this.mContext, SDKControls.class,
                    new Response.Listener<SDKControls>() {
                        @Override
                        public void onResponse(SDKControls response) {
                            if (httpGetRequest.getListener() != null) {
                                httpGetRequest.getListener().onResponse(response);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            processError(error, httpGetRequest.getErrorListener());
                        }
                    });
        }

        return null;
    }

    private void processError(VolleyError error, HTNetworkResponse.ErrorListener errorListener) {
        if (errorListener == null) {
            return;
        }

        if (error == null) {
            errorListener.onErrorResponse(null, new NoResponseException("Something went wrong. Please try again."));
            return;
        }

        if (error instanceof NoConnectionError) {
            errorListener.onErrorResponse(error, new NoConnectionException("We had some trouble connecting. Please try again in sometime."));
            return;
        }

        if (error.networkResponse == null) {
            errorListener.onErrorResponse(error, new NoResponseException("There was no response from the server. Please try again in sometime."));
            return;
        }

        if (error.networkResponse.statusCode >= 500 && error.networkResponse.statusCode <= 599) {
            errorListener.onErrorResponse(error, new ServerException(NetworkErrorUtil.getMessage(error)));
            return;
        }

        Exception runtimeException = NetworkErrorUtil.getException(error);
        HTLog.e(this.mTag, runtimeException.getMessage(), runtimeException);

        errorListener.onErrorResponse(error, runtimeException);
    }

    private <T> void addToRequestQueue(Request<T> request, String tag) {

        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        request.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MILLISECONDS, mMaxRetries,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        request.setShouldCache(false);

        RequestQueue requestQueue = getRequestQueue();
        if (requestQueue != null) {
            requestQueue.add(request);
        }
    }

    private RequestQueue getRequestQueue() {
        try {
            if (mRequestQueue == null) {

                synchronized (HTTPClient.class) {
                    if (mRequestQueue == null) {
                        mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
                    }
                }
            }
        } catch (OutOfMemoryError | Exception e) {
            HTLog.e(TAG, "Error occurred while getRequestQueue: " + e);
        }

        return mRequestQueue;
    }
}
