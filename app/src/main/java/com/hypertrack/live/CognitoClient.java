package com.hypertrack.live;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.hypertrack.live.utils.SharedHelper;
import com.hypertrack.sdk.BuildConfig;
import com.hypertrack.sdk.logger.HTLogger;
import com.hypertrack.sdk.utils.StaticUtilsAdapter;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class CognitoClient {
    private static final String TAG = App.TAG + "HTMobileClient";

    private final Context mContext;
    private RequestQueue mRequestQueue;
    private SharedHelper sharedHelper;
    private final Map<String, String> headers = new HashMap<>();

    private String signUpEmail;
    private String signUpPassword;

    @SuppressLint("StaticFieldLeak")
    private static CognitoClient client;

    public synchronized static CognitoClient getInstance(Context context) {
        if (client == null) {
            client = new CognitoClient(context);
        }
        return client;
    }

    private CognitoClient(Context context) {
        mContext = context.getApplicationContext() == null ? context : context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        sharedHelper = SharedHelper.getInstance(context);

        headers.put("User-Agent", "HyperTrack " + "Android" + " " +
                BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + ")");
        headers.put("Device-Time", StaticUtilsAdapter.sInstance.getCurrentTime());
        headers.put("App-ID", mContext.getPackageName());
        headers.put("timezone", TimeZone.getDefault().getID());
    }

    boolean isAuthorized() {
        return AWSMobileClient.getInstance().isSignedIn();
    }

    void initialize(@NonNull final Callback callback) {
        AWSMobileClient.getInstance().initialize(mContext, new InnerCallback<UserStateDetails>(callback) {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                onSuccessHidden(CognitoClient.this);
            }
        });
    }

    public void signIn(final String email, final String password, @NonNull final Callback callback) {
        final AWSMobileClient awsMobileClient = AWSMobileClient.getInstance();
        awsMobileClient.signIn(email, password, null, new InnerCallback<SignInResult>(callback) {

            @Override
            public void onResult(SignInResult result) {
                sharedHelper.setAccountEmail(email);
                updatePublishableKey(callback);
            }

            @Override
            public void onError(Exception e) {
                awsMobileClient.signIn(email.toLowerCase(), password, null, new InnerCallback<SignInResult>(callback) {

                    @Override
                    public void onResult(SignInResult result) {
                        sharedHelper.setAccountEmail(email);
                        updatePublishableKey(callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        signUpEmail = email;
                        signUpPassword = password;
                    }
                });
            }
        });
    }

    public void signUp(final String email, final String password, Map<String, String> attributes, @NonNull final Callback callback) {
        AWSMobileClient.getInstance().signUp(email, password, attributes, null, new InnerCallback<SignUpResult>(callback) {
            @Override
            public void onResult(SignUpResult result) {
                sharedHelper.setAccountEmail(email);
                signUpEmail = email;
                signUpPassword = password;
                onSuccessHidden(CognitoClient.this);
            }
        });
    }

    public void resendSignUp(@NonNull final Callback callback) {
        if (!TextUtils.isEmpty(signUpEmail)) {
            AWSMobileClient.getInstance().resendSignUp(signUpEmail, new InnerCallback<SignUpResult>(callback) {
                @Override
                public void onResult(SignUpResult result) {
                    onSuccessHidden(CognitoClient.this);
                }
            });
        }
    }

    public void confirmSignUp(@NonNull final Callback callback) {
        AWSMobileClient.getInstance().signIn(signUpEmail, signUpPassword, null, new InnerCallback<SignInResult>(callback) {

            @Override
            public void onResult(SignInResult result) {
                updatePublishableKey(callback);
            }
        });

    }

    private void updatePublishableKey(@NonNull final Callback callback) {
        if (!isAuthorized()) {
            return;
        }

        Request request = new Request("https://live-api.htprod.hypertrack.com/api-key", new Response.Listener<JsonObject>() {
            @Override
            public void onResponse(JsonObject response) {
                Log.d(TAG, "getPublishableKey onResponse: " + response);
                String hyperTrackPublicKey = response.get("key").getAsString();
                sharedHelper.setHyperTrackPubKey(hyperTrackPublicKey);
                sharedHelper.setLoginType(SharedHelper.LOGIN_TYPE_COGNITO);
                AWSMobileClient.getInstance().signOut(); // We don't need to be signed in anymore
                callback.onSuccess(CognitoClient.this);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                callback.onError(error.getMessage(), null);
            }
        });

        request.setShouldCache(false);

        request(request);
    }

    private void request(@NonNull final Request request) {
        AWSMobileClient.getInstance().getTokens(new com.amazonaws.mobile.client.Callback<Tokens>() {
            @Override
            public void onResult(Tokens result) {
                Log.d(TAG, "IdToken: " + result.getIdToken().getTokenString());
                headers.put("Authorization", result.getIdToken().getTokenString());
                request.headers.putAll(headers);
                mRequestQueue.add(request);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "request onError: " + e.getMessage());
                if (request.getErrorListener() != null) {
                    request.getErrorListener().onErrorResponse(new VolleyError(e.getMessage()));
                }
            }
        });
    }

    public void logout() {

        try {
            AWSMobileClient.getInstance().signOut();
        } catch (Exception ignored) {
            // AWS doesn't provide way of getting user state when their client wasn't initialized
        }
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

    private static abstract class InnerCallback<T> implements com.amazonaws.mobile.client.Callback<T> {

        private Handler handler = new Handler(Looper.getMainLooper());
        private final Callback callback;

        InnerCallback(Callback callback) {
            this.callback = callback;
        }

        public abstract void onResult(T result);

        @Override
        public void onError(Exception e) {
            Log.e(TAG, "onError: " + e.getMessage());
            onErrorHidden(e.getLocalizedMessage(), e);
        }

        void onSuccessHidden(final CognitoClient mobileClient) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(mobileClient);
                }
            });
        }

        void onErrorHidden(final String message, final Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(message, e);
                }
            });
        }
    }

    public interface Callback {
        void onSuccess(CognitoClient mobileClient);

        void onError(String message, Exception e);
    }
}
