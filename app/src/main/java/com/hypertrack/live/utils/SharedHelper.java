package com.hypertrack.live.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SharedHelper {

    public static final String PUB_KEY = "pub_key";
    public static final String USER_NAME = "user_name";
    public static final String USER_PHONE = "user_phone";
    public static final String HOME_PLACE = "home_place";
    public static final String USER_HOME_ADDRESS = "user_home_address";
    public static final String USER_HOME_LATLON = "user_home_latlon";

    public static final String COVID_19 = "COVID-19";

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

    public String getHyperTrackPubKey() {
        return preferences.getString(PUB_KEY, "");
    }

    public Map<String, Object> getDeviceMetadata() {
        Type listType = new TypeToken<PlaceModel>() {}.getType();
        PlaceModel placeModel = gson.fromJson(preferences.getString(HOME_PLACE, null), listType);

        Map<String, Object> map = new HashMap<>();
        map.put(USER_NAME, preferences.getString(USER_NAME, ""));
        map.put(USER_PHONE, preferences.getString(USER_PHONE, ""));
        if (placeModel != null) {
            map.put(USER_HOME_ADDRESS, placeModel.address);
            map.put(USER_HOME_LATLON, placeModel.latLng.latitude +"," + placeModel.latLng.longitude);
        }
        return map;
    }
}
