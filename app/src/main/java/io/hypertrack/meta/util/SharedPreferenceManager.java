package io.hypertrack.meta.util;

import android.content.Context;
import android.content.SharedPreferences;

import io.hypertrack.meta.MetaApplication;

/**
 * Created by suhas on 25/02/16.
 */
public class SharedPreferenceManager {

    private static final String PREF_NAME = Constants.SHARED_PREFERENCES_NAME;

    private static SharedPreferences getSharedPreferences() {
        Context context = MetaApplication.getInstance().getApplicationContext();
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

    public static void setUserAuthToken(String userAuthToken) {
        SharedPreferences.Editor editor = getEditor();

        editor.putString(Constants.USER_AUTH_TOKEN, userAuthToken);
        editor.apply();
    }

    public static String getUserAuthToken() {
        return getSharedPreferences().getString(Constants.USER_AUTH_TOKEN, Constants.DEFAULT_STRING_VALUE);
    }
}
