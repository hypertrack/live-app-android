package io.hypertrack.meta.util;

import android.content.Context;
import android.content.SharedPreferences;

import io.hypertrack.meta.MetaApplication;

/**
 * Created by suhas on 25/02/16.
 */
public class SharedPreferenceManager {

    private static final String PREF_NAME = Constants.SHARED_PREFERENCES_NAME;
    private static final String USER_AUTH_TOKEN = "user_auth_token";
    private static final String PLACE_ID = "io.hypertrack.meta:PlaceID";
    private static final String TRIP_ID = "io.hypertrack.meta:TripID";

    private static SharedPreferences getSharedPreferences() {
        Context context = MetaApplication.getInstance().getApplicationContext();
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

    public static void setUserAuthToken(String userAuthToken) {
        SharedPreferences.Editor editor = getEditor();

        editor.putString(USER_AUTH_TOKEN, userAuthToken);
        editor.apply();
    }

    public static String getUserAuthToken() {
        return getSharedPreferences().getString(USER_AUTH_TOKEN, Constants.DEFAULT_STRING_VALUE);
    }

    public static int getPlaceID() {
        return getSharedPreferences().getInt(PLACE_ID, Constants.DEFAULT_INT_VALUE);
    }

    public static void setPlaceID(int placeID) {
        SharedPreferences.Editor editor = getEditor();

        editor.putInt(PLACE_ID, placeID);
        editor.apply();
    }
}
