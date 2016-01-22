package io.hypertrack.meta.network;


import android.os.Build;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

public class HTMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final String mMimeType;
    private final byte[] mMultipartBody;
    private static String API_TOKEN;

    public static String getApiToken() {
        return API_TOKEN;
    }

    public static void setApiToken(String apiToken) {
        API_TOKEN = apiToken;
    }


    public HTMultipartRequest(String url, String mimeType, byte[] multipartBody, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(Method.PATCH, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mMimeType = mimeType;
        this.mMultipartBody = multipartBody;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        //Authorization: Token <token>
        if(API_TOKEN !=null) {
            params.put("Authorization", "Token " + API_TOKEN);
            //params.put("User-agent", "Hypertrack (Android " + Build.VERSION.RELEASE + ") SendETA/" + BuildConfig.VERSION_NAME);
        }
        return params;
    }

    @Override
    public String getBodyContentType() {
        return mMimeType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mMultipartBody;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}
