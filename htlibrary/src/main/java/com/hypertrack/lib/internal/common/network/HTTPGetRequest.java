package com.hypertrack.lib.internal.common.network;

import android.content.Context;
import android.os.Build;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.common.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class HTTPGetRequest<T> extends JsonRequest<T> {

    private final Gson gson;
    private final Class<T> responseType;
    private final Response.Listener<T> listener;

    private Context context;
    private String mToken;

    private final HashMap<String, String> additionalHeaders;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ENCODING = "Content-Encoding";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private boolean mGzipEnabled = false;

    HTTPGetRequest(String url, HashMap<String, String> additionalHeaders,
                   String requestBody, Context context, Class<T> responseType,
                   Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, requestBody, listener, errorListener);

        this.context = context;
        this.responseType = responseType;
        this.listener = listener;
        this.gson = HTGson.gson();
        this.additionalHeaders = additionalHeaders;
        this.mToken = HyperTrack.getPublishableKey(context);
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
            params.put(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
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

        return super.parseNetworkError(volleyError);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));

            return Response.success(
                    gson.fromJson(json, responseType), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    // The following code was an attempt to GZIP json before sending it to the server.
    // The server was unable to unzip it at its end so it has been pushed back for now.

    /*
    @Override
    public byte[] getBody() throws AuthFailureError {

        if (requestBody != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = null;
            try {

                HTLogUtils.LOGV(TAG, "Request Body: " + requestBody);
                gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
                gzipOutputStream.write(requestBody.getBytes("UTF-8"));
                gzipOutputStream.flush();
            } catch (IOException exception) {
                HTLogUtils.LOGV(TAG, "GZIP: " + exception.getMessage());
            } finally {
                if (gzipOutputStream != null) try {
                    gzipOutputStream.close();
                } catch (IOException exception) {
                    HTLogUtils.LOGV(TAG, "GZIP: " + exception.getMessage());
                }
            }

            testDecompress(byteArrayOutputStream.toByteArray());
            return byteArrayOutputStream.toByteArray();
        } else
            return super.getBody();

    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

    private void testDecompress(byte[] compressed) {

        final byte[] data;
        try {
            data = decompressResponse(compressed);
            String json = new String(data,"UTF-8");
            HTLogUtils.LOGV(TAG, "Decompressed: " + json);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected byte[] decompressResponse(byte[] compressed) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            int size;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
            GZIPInputStream gzip = new GZIPInputStream(byteArrayInputStream);
            final int buffSize = 8192;
            byte[] tempBuffer = new byte[buffSize];
            baos = new ByteArrayOutputStream();
            while ((size = gzip.read(tempBuffer, 0, buffSize)) != -1) {
                baos.write(tempBuffer, 0, size);
            }
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }
    */
}
