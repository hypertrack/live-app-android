package io.hypertrack.meta.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.hypertrack.meta.BuildConfig;

public class HTCustomGetRequest<T> extends Request<T> {

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

    public HTCustomGetRequest(String url, Class<T> responseType, Listener<T> listener,
                              ErrorListener errorListener) {
        super(Method.GET, url, errorListener);

        this.responseType = responseType;
        this.listener = listener;
        this.gson = new Gson();

    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        Map<String, String> params = new HashMap<String, String>();
        if(API_TOKEN !=null) {
            //params.put("Authorization", "Token " + API_TOKEN);
            //params.put("User-agent", "META (Android " + Build.VERSION.RELEASE + ") TransmitterSDK/" + BuildConfig.VERSION_NAME);
        }
        return params;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {

            if (response != null)
                Log.d("Response", "Status Code: " + response.statusCode + " Headers:" + response.headers);

            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(gson.fromJson(json, responseType), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {

        if (volleyError != null) {

            if (volleyError.networkResponse != null) {

                Log.d("Error", " " + volleyError.networkResponse.statusCode);

                if (volleyError.networkResponse.data != null && volleyError.networkResponse.headers != null) {
                    try {
                        String json = new String(
                                volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                        Log.d("Error", " " + json);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        return super.parseNetworkError(volleyError);
    }

    private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        private final DateFormat dateFormat;

        private DateTypeAdapter() {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override public synchronized JsonElement serialize(Date date, Type type,
                                                            JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(dateFormat.format(date));
        }

        @Override public synchronized Date deserialize(JsonElement jsonElement, Type type,
                                                       JsonDeserializationContext jsonDeserializationContext) {
            try {
                return dateFormat.parse(jsonElement.getAsString());
            } catch (ParseException e) {
                throw new JsonParseException(e);
            }
        }
    }

}
