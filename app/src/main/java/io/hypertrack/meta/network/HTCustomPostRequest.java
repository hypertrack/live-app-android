package io.hypertrack.meta.network;

import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import io.hypertrack.meta.BuildConfig;

/**
 * Created by suhas on 27/08/15.
 */
public class HTCustomPostRequest<T> extends JsonRequest<T> {

    protected final Gson gson;
    protected final Class<T> responseType;
    private final Listener<T> listener;

    private static String API_TOKEN;

    public static String getApiToken() {
        return API_TOKEN;
    }

    public static void setApiToken(String apiToken) {
        API_TOKEN = apiToken;
    }

    public HTCustomPostRequest(int method, String url, String requestBody, Class<T> responseType, Listener<T> listener,
                               ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);

        this.responseType = responseType;
        this.listener = listener;
        this.gson = new Gson();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {

            Log.d("Response", "Status Code: " + response.statusCode + " Headers:" + response.headers);

            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));

            Log.d("Response", "RESPONSE: " + json);
            return Response.success(
                    gson.fromJson(json, responseType), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        Map<String, String> params = new HashMap<String, String>();
        //Authorization: Token <token>
        if(API_TOKEN !=null) {
            params.put("Authorization", "Token " + API_TOKEN);
            //params.put("User-agent", "Hypertrack (Android " + Build.VERSION.RELEASE + ") ConsumerSDK/" + BuildConfig.VERSION_NAME);
        }
        return params;
    }

}
