package com.hypertrack.live.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SharedHelper {

    public static final String PUB_KEY = "pub_key";
    public static final String USER_EMAIL_KEY = "user_email";
    public static final String USER_NAME_KEY = "user_name";
    public static final String USER_PHONE_KEY = "user_phone";
    public static final String HOME_PLACE_KEY = "home_place";
    public static final String USER_HOME_ADDRESS_KEY = "user_home_address";
    public static final String USER_HOME_LATLON_KEY = "user_home_latlon";

    public static final String COVID_19 = "COVID-19";
    private static final String RECENT = "recent";
    private static final String SELECTED_TRIP_ID = "selected_trip_id";

    private static SharedHelper instance;

    private final Gson gson = new Gson();
    private final SharedPreferences preferences;

    public static synchronized SharedHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedHelper(context);
        }
        return instance;
    }

    private SharedHelper(Context context) {
        preferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    public SharedPreferences sharedPreferences() {
        return preferences;
    }

    @NonNull
    public String getHyperTrackPubKey() {
        return preferences.getString(PUB_KEY, "");
    }

    public Map<String, Object> getDeviceMetadata() {
        Type listType = new TypeToken<PlaceModel>() {}.getType();
        PlaceModel placeModel = gson.fromJson(preferences.getString(HOME_PLACE_KEY, null), listType);

        Map<String, Object> map = new HashMap<>();
        map.put(USER_NAME_KEY, preferences.getString(USER_NAME_KEY, ""));
        map.put(USER_PHONE_KEY, preferences.getString(USER_PHONE_KEY, ""));
        if (placeModel != null) {
            map.put(USER_HOME_ADDRESS_KEY, placeModel.address);
            map.put(USER_HOME_LATLON_KEY, placeModel.latLng.latitude +"," + placeModel.latLng.longitude);
        }
        return map;
    }

    public boolean isHomePlaceSet() {
        return preferences.contains(HOME_PLACE_KEY);
    }

    @Nullable public PlaceModel getHomePlace() {
        try {
            return gson.fromJson(
                    preferences.getString(HOME_PLACE_KEY, null),
                    new TypeToken<PlaceModel>() {}.getType()
            );
        } catch (JsonSyntaxException ignored) { }
        return null;
    }

    public void setHomePlace(@Nullable PlaceModel home) {
        String homeJson = gson.toJson(home);
        preferences
                .edit()
                .putString(SharedHelper.HOME_PLACE_KEY, homeJson)
                .apply();
    }

    @NonNull public Set<PlaceModel> getRecentPlaces() {
        String recentJson = preferences.getString(RECENT, "[]");
        Type listType = new TypeToken<Set<PlaceModel>>() {}.getType();
        try {
            return gson.fromJson(recentJson, listType);
        } catch (JsonSyntaxException ignored) { }

        return Collections.emptySet();
    }

    public void setRecentPlaces(@Nullable Collection<PlaceModel> recentPlaces) {
        if (recentPlaces == null) recentPlaces = Collections.emptySet();
        String recentJson = gson.toJson(recentPlaces);
        preferences.edit().putString(RECENT, recentJson).apply();
    }

    @Nullable public String getSelectedTripId() {
        return preferences.getString(SELECTED_TRIP_ID, null);
    }

    public void setSelectedTripId(@NonNull String tripId) {
        preferences.edit().putString(SELECTED_TRIP_ID, tripId).apply();
    }

    public void clearSelectedTripId() {
        preferences.edit().remove(SELECTED_TRIP_ID).apply();
    }

    public void logout() {
        preferences.edit().clear().apply();
    }
}
