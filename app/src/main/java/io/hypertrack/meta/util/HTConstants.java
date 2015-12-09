package io.hypertrack.meta.util;

import android.util.Log;

import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.network.HTCustomPostRequest;


/**
 * Created by suhas on 12/11/15.
 */
public class HTConstants {
    public static final String TAG = HTConstants.class.getSimpleName();

    public static final String USER_ID = "user_id";
    public static final String HYPERTRACK_COURIER_ID = "hypertrack_courier_id";
    public static final String USER_AUTH_TOKEN = "user_auth_token";
    public static final String TRIP_ID = "trip_id";
    public static final String TRIP_URI = "trip_uri";
    public static final String TRIP_ETA = "trip_eta";
    public static final String TRIP_STATUS = "trip_live";
    public static final String SHARED_PREFERENCES_NAME = "io.hypertrack.meta";

    public static void setPublishableApiKey(String publishableKey) {

        try {

            if (publishableKey == null || publishableKey.length() == 0)
                throw new RuntimeException("Invalid Publishable Key: You must use a valid publishable key.");

            HTCustomPostRequest.setApiToken(publishableKey);
            HTCustomGetRequest.setApiToken(publishableKey);

        } catch (Exception e) {
            Log.wtf(TAG, "Please verify your publishable key.", e);
            e.printStackTrace();
        }
    }
}
