package com.hypertrack.live;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hypertrack.live.debug.DebugHelper;
import com.hypertrack.sdk.HyperTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiHelper {

    private final RequestQueue queue;
    private final String hyperTrackPublicKey;
    private final Map<String, String> baseHeaders = new HashMap<>();
    private final Map<String, String> tripsHeaders;
    private final String apiDomain;

    public ApiHelper(Context context, String hyperTrackPublicKey) {
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(context);
        this.hyperTrackPublicKey = hyperTrackPublicKey;
        baseHeaders.put("Content-Type", "application/json; charset=utf-8");

        apiDomain = DebugHelper.getApiDomain(context);
        String accountid = DebugHelper.getSharedPreferences(context).getString(DebugHelper.DEV_ACCOUNTID_KEY, "");
        String secretkey = DebugHelper.getSharedPreferences(context).getString(DebugHelper.DEV_SECRETKEY_KEY, "");
        tripsHeaders = new HashMap<>(baseHeaders);
        String authHeader = "Basic " + Base64.encodeToString(
                String.format("%s:%s", accountid, secretkey).getBytes(StandardCharsets.UTF_8),
                Base64.NO_WRAP);
        tripsHeaders.put("Authorization", authHeader);
    }

    public void createTrip(JSONObject jsonObject, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,
                "https://" + apiDomain + "/trips/",
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("response", response.toString());
                        if (listener != null) {
                            listener.onResponse(response);
                        }
                    }
                }, new BaseErrorListener(errorListener)) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return new HashMap<>(tripsHeaders);
            }
        };

        queue.add(jsonRequest);
    }

    public void completeTrip(String tripId, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,
                String.format("https://" + apiDomain + "/trips/%s/complete", tripId), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("response", response.toString());
                        if (listener != null) {
                            listener.onResponse("");
                        }
                    }
                }, new BaseErrorListener(errorListener)) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return new HashMap<>(tripsHeaders);
            }
        };

        queue.add(jsonRequest);
    }

    public void getTrackingId(final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
            String url = "https://7kcobbjpavdyhcxfvxrnktobjm.appsync-api.us-west-2.amazonaws.com/graphql";

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject("{\n" +
                        "  \"query\": \"query getPublicTrackingIdQuery($publishableKey: String!, $deviceId: String!){\\\\\n  getPublicTrackingId(publishable_key: $publishableKey, device_id: $deviceId){\\\\\n    tracking_id\\\\\n  }\\\\\n}\"," +
                        "  \"variables\": {" +
                        "    \"publishableKey\": \"" + hyperTrackPublicKey + "\",\n" +
                        "    \"deviceId\": \"" + HyperTrack.getDeviceId() + "\"" +
                        "  }," +
                        "  \"operationName\": \"getPublicTrackingIdQuery\"" +
                        "}");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Request a json response from the provided URL.
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Display the first 500 characters of the response string.
                            try {
                                String trackingId = response.getJSONObject("data")
                                        .getJSONObject("getPublicTrackingId")
                                        .getString("tracking_id");
                                if (listener != null) {
                                    listener.onResponse(trackingId);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new BaseErrorListener(errorListener)) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>(baseHeaders);
                    headers.put("X-Api-Key", "da2-nt5vwlflmngjfbe6cbsone4emm");
                    return headers;
                }
            };


            // Add the request to the RequestQueue.
            queue.add(jsonRequest);
    }

    public static class BaseErrorListener implements Response.ErrorListener {

        private final Response.ErrorListener innerErrorListener;

        public BaseErrorListener() {
            this(null);
        }

        public BaseErrorListener(Response.ErrorListener errorListener) {
            innerErrorListener = errorListener;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (innerErrorListener != null) {
                innerErrorListener.onErrorResponse(error);
            }
            error.printStackTrace();
        }
    }
}
