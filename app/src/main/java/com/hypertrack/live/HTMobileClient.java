package com.hypertrack.live;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.Tokens;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hypertrack.sdk.BuildConfig;
import com.hypertrack.sdk.logger.HTLogger;
import com.hypertrack.sdk.utils.StaticUtilsAdapter;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class HTMobileClient {
    private static final String TAG = "HTMobileClient";

    private final Context mContext;
    private RequestQueue mRequestQueue;
    private final Map<String, String> headers = new HashMap<>();

    public static HTMobileClient getInstance(Context context) {
        return new HTMobileClient(context);
    }

    private HTMobileClient(Context context) {
        mContext = context.getApplicationContext() == null ? context : context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());

        headers.put("User-Agent", "HyperTrack " + "Android" + " " +
                BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + ")");
        headers.put("Device-Time", StaticUtilsAdapter.sInstance.getCurrentTime());
        headers.put("App-ID", mContext.getPackageName());
        headers.put("timezone", TimeZone.getDefault().getID());
    }

    public boolean isAuthorized() {
        return AWSMobileClient.getInstance().isSignedIn();
    }

    public void initialize(@NonNull final Callback callback) {
        AWSMobileClient.getInstance().initialize(mContext, new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                callback.onSuccess(HTMobileClient.this);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void signIn(String email, final String password, @NonNull final Callback callback) {
        AWSMobileClient.getInstance().signIn(email, password, null, new com.amazonaws.mobile.client.Callback<SignInResult>() {
            @Override
            public void onResult(SignInResult result) {
                updatePublishableKey(callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        });

    }

    public void signUp(String email, final String password, Map<String, String> attributes, @NonNull final Callback callback) {
        AWSMobileClient.getInstance().signUp(email, password, attributes, null, new com.amazonaws.mobile.client.Callback<SignUpResult>() {
            @Override
            public void onResult(SignUpResult result) {
                callback.onSuccess(HTMobileClient.this);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void updatePublishableKey(@NonNull final Callback callback) {
        if (!isAuthorized()) {
            return;
        }

        Request request = new Request("https://live-api.htprod.hypertrack.com/api-key", new Response.Listener<JsonObject>() {
            @Override
            public void onResponse(JsonObject response) {
                Log.d(TAG, "getPublishableKey onResponse: " + response);
                String hyperTrackPublicKey = response.get("key").getAsString();
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.app_name), Context.MODE_PRIVATE);
                sharedPreferences.edit()
                        .putString("pub_key", hyperTrackPublicKey)
                        .putBoolean("is_tracking", true)
                        .apply();

                callback.onSuccess(HTMobileClient.this);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error.getMessage());
            }
        });

        request.setShouldCache(false);

        request(request);
    }

    public void request(@NonNull final Request request) {
        AWSMobileClient.getInstance().getTokens(new com.amazonaws.mobile.client.Callback<Tokens>() {
            @Override
            public void onResult(Tokens result) {
                headers.put("Authorization", result.getIdToken().getTokenString());
                request.headers.putAll(headers);
                mRequestQueue.add(request);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage() + "");
                if (request.getErrorListener() != null) {
                    request.getErrorListener().onErrorResponse(new VolleyError(e.getMessage()));
                }
            }
        });
    }

    public static class Request extends JsonRequest<JsonObject> {

        private final Gson gson;
        private Map<String, String> headers = new HashMap<>();

        Request(String url, Response.Listener<JsonObject> listener, Response.ErrorListener errorListener) {
            this(Method.GET, url, null, listener, errorListener);
        }

        Request(int requestMethod, String url, String requestBody, Response.Listener<JsonObject> listener,
                Response.ErrorListener errorListener) {
            super(requestMethod, url, requestBody, listener, errorListener);
            gson = new Gson();
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(
                        response.data, HttpHeaderParser.parseCharset(response.headers));

                return Response.success(
                        gson.fromJson(json, JsonObject.class), HttpHeaderParser.parseCacheHeaders(response));

            } catch (UnsupportedEncodingException | JsonSyntaxException e) {
                HTLogger.e(TAG, "parseNetworkResponse: ", e);
                ParseError parseError = new ParseError(response);
                return Response.error(parseError);
            }
        }

        @Override
        public String getBodyContentType() {
            return "application/json; charset=utf-8";
        }

    }

    public interface Callback {
        void onSuccess(HTMobileClient mobileClient);

        void onError(String message);
    }
}
