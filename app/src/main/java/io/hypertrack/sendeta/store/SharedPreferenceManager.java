package io.hypertrack.sendeta.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;

import com.google.android.gms.location.GeofencingRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.Place;

import java.lang.reflect.Type;
import java.util.Date;

import io.hypertrack.sendeta.MetaApplication;
import io.hypertrack.sendeta.model.HyperTrackLiveUser;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.DateDeserializer;
import io.hypertrack.sendeta.util.DateSerializer;
import io.hypertrack.sendeta.util.LocationDeserializer;
import io.hypertrack.sendeta.util.LocationSerializer;

/**
 * Created by suhas on 25/02/16.
 */
public class SharedPreferenceManager {

    private static final String PREF_NAME = Constants.SHARED_PREFERENCES_NAME;
    private static final String CURRENT_PLACE = "io.hypertrack.meta:CurrentPlace";

    private static final String LAST_SELECTED_VEHICLE_TYPE = "io.hypertrack.meta:LastSelectedVehicleType";
    private static final String HYPERTRACK_LIVE_USER = "io.hypertrack.meta:OnboardedUser";
    private static final String LAST_KNOWN_LOCATION = "io.hypertrack.meta:LastKnownLocation";
    private static final String GEOFENCING_REQUEST = "io.hypertrack.meta:GeofencingRequest";

    private static final String CURRENT_ACTION = "io.hypertrack.meta:CurrentAction";
    private static final String CURRENT_ACTION_ID = "io.hypertrack.meta:CurrentActionID";
    private static final String TRACKING_SETTING = "io.hypertrack.meta:TrackingSetting";
    private static final String TRACKING_DIALOG = "io.hypertrack.meta:TrackingDialog";

    private static SharedPreferences getSharedPreferences() {
        Context context = MetaApplication.getInstance().getApplicationContext();
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

    private static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, DateSerializer.getInstance());
        gsonBuilder.registerTypeAdapter(Date.class, DateDeserializer.getInstance());
        gsonBuilder.registerTypeAdapter(Location.class, LocationSerializer.getInstance());
        gsonBuilder.registerTypeAdapter(Location.class, LocationDeserializer.getInstance());
        return gsonBuilder.create();
    }

    public static void setPlace(Place place) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String placeJson = gson.toJson(place);

        editor.putString(CURRENT_PLACE, placeJson);
        editor.apply();
    }

    public static Place getActionPlace() {
        String placeJson = getSharedPreferences().getString(CURRENT_PLACE, null);
        if (placeJson == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Place>() {
        }.getType();

        return gson.fromJson(placeJson, type);
    }

    public static void deletePlace() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(CURRENT_PLACE);
        editor.apply();
    }

    public static String getActionID(Context context) {
        return getSharedPreferences().getString(CURRENT_ACTION_ID, null);
    }

    public static void setActionID(String actionID) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(CURRENT_ACTION_ID, actionID);
        editor.apply();
    }

    public static void deleteActionID() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(CURRENT_ACTION_ID);
        editor.apply();
    }

    public static Action getAction(Context context) {
        String actionJson = getSharedPreferences().getString(CURRENT_ACTION, null);
        if (actionJson == null)
            return null;
        try {
            Gson gson = getGson();
            Type type = new TypeToken<Action>() {
            }.getType();

            return gson.fromJson(actionJson, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setAction(Action action) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = getGson();
        String actionJSON = gson.toJson(action);

        editor.putString(CURRENT_ACTION, actionJSON);
        editor.apply();
    }

    public static void deleteAction() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(CURRENT_ACTION);
        editor.apply();
    }

    public static HyperTrackLiveUser getHyperTrackLiveUser() {
        String userJSON = getSharedPreferences().getString(HYPERTRACK_LIVE_USER, null);

        if (userJSON == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<HyperTrackLiveUser>() {
        }.getType();

        return gson.fromJson(userJSON, type);
    }

    public static void setHyperTrackLiveUser(HyperTrackLiveUser user) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String userJSON = gson.toJson(user);

        editor.putString(HYPERTRACK_LIVE_USER, userJSON);
        editor.apply();
    }

    public static Location getLastKnownLocation() {
        String lastKnownLocationJSON = getSharedPreferences().getString(LAST_KNOWN_LOCATION, null);
        if (lastKnownLocationJSON == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Location>() {
        }.getType();

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
        Type type = new TypeToken<GeofencingRequest>() {
        }.getType();

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

    public static HTUserVehicleType getLastSelectedVehicleType(Context context) {
        String vehicleTypeString = getSharedPreferences().getString(LAST_SELECTED_VEHICLE_TYPE, null);
        if (TextUtils.isEmpty(vehicleTypeString)) {
            return HTUserVehicleType.CAR;
        }

        if (vehicleTypeString.equalsIgnoreCase(HTUserVehicleType.CAR.toString())) {
            return HTUserVehicleType.CAR;
        } else if (vehicleTypeString.equalsIgnoreCase(HTUserVehicleType.MOTORCYCLE.toString())) {
            return HTUserVehicleType.MOTORCYCLE;
        } else if (vehicleTypeString.equalsIgnoreCase(HTUserVehicleType.WALK.toString())) {
            return HTUserVehicleType.WALK;
        } else if (vehicleTypeString.equalsIgnoreCase(HTUserVehicleType.VAN.toString())) {
            return HTUserVehicleType.VAN;
        }

        return HTUserVehicleType.CAR;
    }

    public static void setLastSelectedVehicleType(HTUserVehicleType vehicleType) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(LAST_SELECTED_VEHICLE_TYPE, vehicleType.toString());
        editor.apply();
    }

    private static void setTrackingSetting(boolean flag) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(TRACKING_SETTING, flag);
        editor.apply();
    }

    public static void setRequestedForBackgroundTracking() {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(TRACKING_DIALOG, true);
        editor.apply();
    }

    public static boolean hasRequestedForBackgroundTracking() {
        return getSharedPreferences().getBoolean(TRACKING_DIALOG, false);
    }

    public static void setTrackingON() {
        setTrackingSetting(true);
    }

    public static void setTrackingOFF() {
        setTrackingSetting(false);
    }

    public static boolean isTrackingON() {
        return getSharedPreferences().getBoolean(TRACKING_SETTING, false);
    }
}
