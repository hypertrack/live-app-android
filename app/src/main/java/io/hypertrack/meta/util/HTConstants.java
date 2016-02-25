package io.hypertrack.meta.util;

import android.util.Log;

import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.network.HTMultipartRequest;


/**
 * Created by suhas on 12/11/15.
 */
public class HTConstants {
    public static final String TAG = HTConstants.class.getSimpleName();

    public static final String SHARED_PREFERENCES_NAME = "io.hypertrack.meta";
    public static final String USER_ID = "user_id";
    public static final String USER_FIRSTNAME = "user_firstname";
    public static final String USER_LASTNAME = "user_lastname";
    public static final String USER_PROFILE_PIC = "user_profile_pic";
    public static final String DEFAULT_STRING_VALUE = "none";

    public static final String USER_PROFILE_PIC_ENCODED = "user_profile_pic_encoded";
    public static final String HYPERTRACK_COURIER_ID = "hypertrack_courier_id";
    public static final String USER_AUTH_TOKEN = "user_auth_token";
    public static final String GCM_REGISTRATION_TOKEN = "gcm_registration_token";
    public static final String TRIP_ID = "trip_id";
    public static final String TRIP_DESTINATION = "trip_destination";
    public static final String TRIP_SHARE_URI = "trip_uri";
    public static final String TRIP_ETA = "trip_eta";
    public static final String TRIP_STATUS = "trip_live";
    public static final String API_ENDPOINT = BuildConfig.BASE_URL;
    public static final String PUBLISHABLE_KEY = BuildConfig.API_KEY;

    public static void setPublishableApiKey(String publishableKey) {

        try {

            if (publishableKey == null || publishableKey.length() == 0)
                throw new RuntimeException("Invalid Publishable Key: You must use a valid publishable key.");

            HTCustomPostRequest.setApiToken(publishableKey);
            HTCustomGetRequest.setApiToken(publishableKey);
            HTMultipartRequest.setApiToken(publishableKey);

        } catch (Exception e) {
            Log.wtf(TAG, "Please verify your publishable key.", e);
            e.printStackTrace();
        }
    }
}
