package io.hypertrack.meta.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import io.hypertrack.meta.model.Place;

/**
 * Created by suhas on 25/02/16.
 */
public class SharedPreferenceManager {

    public static final String pref_name = Constants.SHARED_PREFERENCES_NAME;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    Context ctx;

    public SharedPreferenceManager(Context ctx) {
        sharedpreferences = ctx.getSharedPreferences(pref_name,
                Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        this.ctx = ctx;
    }

    public void setUserAuthToken(String userAuthToken) {
        editor.putString(Constants.USER_AUTH_TOKEN, userAuthToken);
        editor.apply();
    }

    public String getUserAuthToken() {
        return sharedpreferences.getString(Constants.USER_AUTH_TOKEN, Constants.DEFAULT_STRING_VALUE);
    }
}
