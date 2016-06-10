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

    public void setUserId(int id) {
        editor.putInt(Constants.USER_ID, id);
        editor.apply();
    }

    public int getUserId() {
        return sharedpreferences.getInt(Constants.USER_ID, -1);
    }

    public void setFirstName(String firstName) {
        editor.putString(Constants.USER_FIRSTNAME, firstName);
        editor.apply();
    }

    public String getFirstName() {
        return sharedpreferences.getString(Constants.USER_FIRSTNAME, Constants.DEFAULT_STRING_VALUE);
    }

    public void setLastName(String lastName) {
        editor.putString(Constants.USER_LASTNAME, lastName);
        editor.apply();
    }

    public String getLastName() {
        return sharedpreferences.getString(Constants.USER_LASTNAME, Constants.DEFAULT_STRING_VALUE);
    }

    public void setUserPhoto(String userPhoto) {
        editor.putString(Constants.USER_PROFILE_PIC, userPhoto);
        editor.apply();
    }

    public String getUserPhoto() {
        return sharedpreferences.getString(Constants.USER_PROFILE_PIC, Constants.DEFAULT_STRING_VALUE);
    }

    public void setUserAuthToken(String userAuthToken) {
        editor.putString(Constants.USER_AUTH_TOKEN, userAuthToken);
        editor.apply();
    }

    public String getUserAuthToken() {
        return sharedpreferences.getString(Constants.USER_AUTH_TOKEN, Constants.DEFAULT_STRING_VALUE);
    }

    public void setHyperTrackDriverID(String courierId) {
        editor.putString(Constants.HYPERTRACK_DRIVER_ID, courierId);
        editor.apply();
    }

    public String getHyperTrackDriverID() {
        return sharedpreferences.getString(Constants.HYPERTRACK_DRIVER_ID, Constants.DEFAULT_STRING_VALUE);
    }

    public void setUserLoggedIn(boolean flag) {
        editor.putBoolean(Constants.USER_ONBOARD, flag);
        editor.apply();
    }

    public boolean isUserLoggedIn() {
        return sharedpreferences.getBoolean(Constants.USER_ONBOARD, false);
    }

    public void setProfileImage(String encodedImage) {
        editor.putString(Constants.USER_PROFILE_PIC_ENCODED, encodedImage);
        editor.apply();
    }

    public String getProfileImage() {
        return sharedpreferences.getString(Constants.USER_PROFILE_PIC_ENCODED, Constants.DEFAULT_STRING_VALUE);
    }

    public ArrayList<Place> getSavedPlaces() {
        ArrayList<Place> places = new ArrayList<>();
        places.add(0, new Place(1, "Home", "Vasant Vihar", 28.561014, 77.159403));
        return places;
    }
}
