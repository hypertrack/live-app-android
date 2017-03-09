package com.hypertrack.lib.internal.common.network;

import android.content.Context;
import android.os.Build;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.common.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class HTTPPostRequest<T> extends JsonRequest<T> {

    private static final String TAG = HTTPPostRequest.class.getSimpleName();
    private final Gson mGson;
    private final Class<T> mResponseType;
    private final Listener<T> mListener;

    private Context context;
    private String mToken;
    private byte[] requestBody;

    private final HashMap<String, String> additionalHeaders;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ENCODING = "Content-Encoding";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private boolean mGzipEnabled = false;

    HTTPPostRequest(String url, HashMap<String, String> additionalHeaders,
                    String requestBody, Context context, Class<T> responseType, Listener<T> listener,
                    ErrorListener errorListener) {
        super(Method.POST, url, requestBody, listener, errorListener);

        this.context = context;
        this.requestBody = getRequestBody(requestBody);
        this.mResponseType = responseType;
        this.mListener = listener;
        this.mGson = HTGson.gson();
        this.additionalHeaders = additionalHeaders;
        this.mToken = HyperTrack.getPublishableKey(context);
    }

    private byte[] getCompressed(String requestBody) {
        if (requestBody != null) {

            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(requestBody.length());
                GZIPOutputStream gzipOutputStream;
                gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream, 32);
                gzipOutputStream.write(requestBody.getBytes("UTF-8"));
                gzipOutputStream.close();
                byte[] compressed = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                mGzipEnabled = true;
                return compressed;

            } catch (Exception exception) {
                HTLog.e(TAG, "Exception occurred while getCompressed: " + exception);
                mGzipEnabled = false;
            } catch (OutOfMemoryError error) {
                HTLog.e(TAG, "OutOfMemory Error occurred while getCompressed: " + error);
                mGzipEnabled = false;
            }
        }

        return null;
    }

    private byte[] getRequestBody(String requestBody) {
        byte[] compressedRequestBody = getCompressed(requestBody);
        if (mGzipEnabled) {
            return compressedRequestBody;
        } else {
            try {
                return requestBody == null ? null : requestBody.getBytes(PROTOCOL_CHARSET);
            } catch (Exception exception) {
                HTLog.e(TAG, "Exception occurred while getRequestBody: " + exception);
            }
        }
        return null;
    }

    @Override
    public byte[] getBody() {
        return requestBody;
    }

    /**
     * Utility method to decompress gzip. To be used when we start sending gzip responses.
     */
    public static String getDecompressed(byte[] compressed) throws IOException {
        try {
            final int BUFFER_SIZE = 32;
            ByteArrayInputStream is = new ByteArrayInputStream(compressed);
            GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
            StringBuilder string = new StringBuilder();
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                string.append(new String(data, 0, bytesRead));
            }
            gis.close();
            is.close();
            return string.toString();
        } catch (Exception exception) {
            HTLog.e(TAG, "Exception occurred while getDecompressed: " + exception);
        }
        return null;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        Map<String, String> params = new HashMap<>();
        params.put(HEADER_AUTHORIZATION, "Token " + (mToken != null ? mToken : ""));
        params.put("User-Agent", "HyperTrack " + HyperTrack.getSDKPlatform() + " " +
                BuildConfig.SDK_VERSION_NAME + " (Android " + Build.VERSION.RELEASE + ")");
        params.put("Device-Time", DateTimeUtility.getCurrentTime());
        params.put("Device-ID", Utils.getDeviceId(context));

        if (mGzipEnabled) {
            params.put(HEADER_ENCODING, ENCODING_GZIP);
        }

        if (this.additionalHeaders != null) {
            Iterator<Map.Entry<String, String>> iterator = this.additionalHeaders.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> header = iterator.next();
                params.put(header.getKey(), header.getValue());
            }
        }
        return params;
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {

        if (volleyError == null || volleyError.networkResponse == null)
            return super.parseNetworkError(volleyError);

        try {
            String json = new String(
                    volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));

            HTLog.d(TAG, "Status Code: " + volleyError.networkResponse.statusCode +
                    " Data: " + json);

        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while HTTPPostRequest parseNetworkError: " + e, e);
        }

        return super.parseNetworkError(volleyError);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));

            return Response.success(
                    mGson.fromJson(json, mResponseType), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

}
