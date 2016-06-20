package io.hypertrack.meta.util;

import android.util.Log;

import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.network.HTMultipartRequest;


/**
 * Created by suhas on 12/11/15.
 */
public class Constants {
    public static final String TAG = Constants.class.getSimpleName();

    public static final String SHARED_PREFERENCES_NAME = "io.hypertrack.meta";
    public static final String DEFAULT_STRING_VALUE = "none";
    public static final int DEFAULT_INT_VALUE = -1;

    public static final String USER_PROFILE_PIC_ENCODED = "user_profile_pic_encoded";
    public static final String GCM_REGISTRATION_TOKEN = "gcm_registration_token";

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
