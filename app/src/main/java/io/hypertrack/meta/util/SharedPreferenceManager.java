package io.hypertrack.meta.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by suhas on 25/02/16.
 */
public class SharedPreferenceManager {

    public static final String pref_name = HTConstants.SHARED_PREFERENCES_NAME;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    Context ctx;

    public SharedPreferenceManager(Context ctx) {
        sharedpreferences = ctx.getSharedPreferences(pref_name,
                Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        this.ctx = ctx;
    }

    public void setUserId(String id) {
        editor.putInt(HTConstants.USER_ID, Integer.valueOf(id));
        editor.apply();
    }

    public int getUserId() {
        return sharedpreferences.getInt(HTConstants.USER_ID, 0);
    }

    public void setFirstName(String firstName) {
        editor.putString(HTConstants.USER_FIRSTNAME, firstName);
        editor.apply();
    }

    public String getFirstName() {
        return sharedpreferences.getString(HTConstants.USER_FIRSTNAME, HTConstants.DEFAULT_STRING_VALUE);
    }

    public void setLastName(String lastName) {
        editor.putString(HTConstants.USER_LASTNAME, lastName);
        editor.apply();
    }

    public String getLastName() {
        return sharedpreferences.getString(HTConstants.USER_LASTNAME, HTConstants.DEFAULT_STRING_VALUE);
    }

    public void setUserPhoto(String userPhoto) {
        editor.putString(HTConstants.USER_PROFILE_PIC, userPhoto);
        editor.apply();
    }

    public String getUserPhoto() {
        return sharedpreferences.getString(HTConstants.USER_PROFILE_PIC, HTConstants.DEFAULT_STRING_VALUE);
    }

}
