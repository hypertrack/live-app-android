package io.hypertrack.sendeta.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.GeofencingRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;

import io.hypertrack.lib.common.model.HTDriverVehicleType;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.sendeta.MetaApplication;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.model.Trip;

/**
 * Created by suhas on 25/02/16.
 */
public class SharedPreferenceManager {

    private static final String PREF_NAME = Constants.SHARED_PREFERENCES_NAME;
    private static final String USER_AUTH_TOKEN = "user_auth_token";
    private static final String GCM_TOKEN = "gcm_token";
    private static final String CURRENT_PLACE = "io.hypertrack.meta:CurrentPlace";
    private static final String CURRENT_TASK = "io.hypertrack.meta:CurrentTask";
    private static final String LAST_SELECTED_VEHICLE_TYPE = "io.hypertrack.meta:LastSelectedVehicleType";
    private static final String ONBOARDED_USER = "io.hypertrack.meta:OnboardedUser";
    private static final String LAST_KNOWN_LOCATION = "io.hypertrack.meta:LastKnownLocation";
    private static final String GEOFENCING_REQUEST = "io.hypertrack.meta:GeofencingRequest";

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

    public static MetaPlace getPlace() {
        String placeJson = getSharedPreferences().getString(CURRENT_PLACE, null);
        if (placeJson == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<MetaPlace>() {}.getType();

        return gson.fromJson(placeJson, type);
    }

    public static String getGCMToken() {
        return getSharedPreferences().getString(GCM_TOKEN, "");
    }

    public static void setGcmToken(String token) {
        SharedPreferences.Editor editor = getEditor();

        editor.putString(GCM_TOKEN, token);
        editor.apply();
    }

    public static void deletePlace() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(CURRENT_PLACE);
        editor.apply();
    }

    public static void setPlace(MetaPlace place) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String placeJson = gson.toJson(place);

        editor.putString(CURRENT_PLACE, placeJson);
        editor.apply();
    }

    public static void setTrip(Trip trip) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String tripJSON = gson.toJson(trip);

        editor.putString(CURRENT_TASK, tripJSON);
        editor.apply();
    }

    public static HTTask getTask(Context context) {
        String taskJson = getSharedPreferences().getString(CURRENT_TASK, null);
        if (taskJson == null) {
            return null;
        }

        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateTypeAdapter());
            gsonBuilder.registerTypeAdapter(Location.class, LocationDeserializer.getInstance());
            gsonBuilder.registerTypeAdapter(Location.class, LocationSerializer.getInstance());
            Gson gson = gsonBuilder.create();
            Type type = new TypeToken<HTTask>() {}.getType();

            return gson.fromJson(taskJson, type);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return null;
    }

    public static void setTask(HTTask task) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String taskJSON = gson.toJson(task);

        editor.putString(CURRENT_TASK, taskJSON);
        editor.apply();
    }


    public static void deleteTask(Context context) {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(CURRENT_TASK);
        editor.apply();
    }

    public static OnboardingUser getOnboardingUser() {
        String userJSON = getSharedPreferences().getString(ONBOARDED_USER, null);

        if (userJSON == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<OnboardingUser>() {}.getType();

        return gson.fromJson(userJSON, type);
    }

    public static void setOnboardingUser(OnboardingUser user) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String userJSON = gson.toJson(user);

        editor.putString(ONBOARDED_USER, userJSON);
        editor.apply();
    }

    public static Location getLastKnownLocation() {
        String lastKnownLocationJSON = getSharedPreferences().getString(LAST_KNOWN_LOCATION, null);
        if (lastKnownLocationJSON == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Location>() {}.getType();

        return gson.fromJson(lastKnownLocationJSON, type);
    }

    public static void setLastKnownLocation(Location lastKnownLocation) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String lastKnownLocationJSON = gson.toJson(lastKnownLocation);

        editor.putString(LAST_KNOWN_LOCATION, lastKnownLocationJSON);
        editor.apply();
    }

    public static GeofencingRequest getGeofencingRequest() {
        String geofencingRequestJSON = getSharedPreferences().getString(GEOFENCING_REQUEST, null);
        if (geofencingRequestJSON == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<GeofencingRequest>() {}.getType();

        return gson.fromJson(geofencingRequestJSON, type);
    }

    public static void setGeofencingRequest(GeofencingRequest request) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String geofencingRequestJSON = gson.toJson(request);

        editor.putString(GEOFENCING_REQUEST, geofencingRequestJSON);
        editor.apply();
    }

    public static void removeGeofencingRequest() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(GEOFENCING_REQUEST);
        editor.apply();
    }

    public static HTDriverVehicleType getLastSelectedVehicleType(Context context) {
        String vehicleTypeString = getSharedPreferences().getString(LAST_SELECTED_VEHICLE_TYPE, null);
        if (TextUtils.isEmpty(vehicleTypeString)) {
            return HTDriverVehicleType.CAR;
        }

        if (vehicleTypeString.equalsIgnoreCase(HTDriverVehicleType.CAR.toString())) {
            return HTDriverVehicleType.CAR;
        } else if (vehicleTypeString.equalsIgnoreCase(HTDriverVehicleType.MOTORCYCLE.toString())) {
            return HTDriverVehicleType.MOTORCYCLE;
        } else if (vehicleTypeString.equalsIgnoreCase(HTDriverVehicleType.WALK.toString())) {
            return HTDriverVehicleType.WALK;
        } else if (vehicleTypeString.equalsIgnoreCase(HTDriverVehicleType.VAN.toString())) {
            return HTDriverVehicleType.VAN;
        }

        return HTDriverVehicleType.CAR;
    }

    public static void setLastSelectedVehicleType(HTDriverVehicleType vehicleType) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(LAST_SELECTED_VEHICLE_TYPE, vehicleType.toString());
        editor.apply();
    }
}
