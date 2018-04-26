
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.location.GeofencingRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.User;

import java.lang.reflect.Type;
import java.util.Date;

import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.CrashlyticsWrapper;
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

    private static final String HYPERTRACK_LIVE_USER = "io.hypertrack.meta:OnboardedUser";
    private static final String LAST_KNOWN_LOCATION = "io.hypertrack.meta:LastKnownLocation";
    private static final String GEOFENCING_REQUEST = "io.hypertrack.meta:GeofencingRequest";
    private static final String TRACKING_ACTION = "io.hypertrack.meta:TrackingAction";

    private static final String CURRENT_ACTION = "io.hypertrack.meta:CurrentAction";
    private static final String CURRENT_ACTION_ID = "io.hypertrack.meta:CurrentActionID";

    private static final String FEEDBACK_ACTIVITY_LIST = "io.hypertrack.meta:FeedbackActivityList";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    private static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, DateSerializer.getInstance());
        gsonBuilder.registerTypeAdapter(Date.class, DateDeserializer.getInstance());
        gsonBuilder.registerTypeAdapter(Location.class, LocationSerializer.getInstance());
        gsonBuilder.registerTypeAdapter(Location.class, LocationDeserializer.getInstance());
        return gsonBuilder.create();
    }

    static void setPlace(Context context, Place place) {
        SharedPreferences.Editor editor = getEditor(context);

        Gson gson = new Gson();
        String placeJson = gson.toJson(place);

        editor.putString(CURRENT_PLACE, placeJson);
        editor.apply();
    }

    static Place getActionPlace(Context context) {
        String placeJson = getSharedPreferences(context).getString(CURRENT_PLACE, null);
        if (placeJson == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Place>() {
        }.getType();

        return gson.fromJson(placeJson, type);
    }

    public static void deletePlace(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(CURRENT_PLACE);
        editor.apply();
    }

    public static String getActionID(Context context) {
        return getSharedPreferences(context).getString(CURRENT_ACTION_ID, null);
    }

    public static void setActionID(Context context, String actionID) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(CURRENT_ACTION_ID, actionID);
        editor.apply();
    }

    public static void deleteActionID(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(CURRENT_ACTION_ID);
        editor.apply();
    }

    public static Action getAction(Context context) {
        String actionJson = getSharedPreferences(context).getString(CURRENT_ACTION, null);
        if (actionJson == null)
            return null;
        try {
            Gson gson = getGson();
            Type type = new TypeToken<Action>() {
            }.getType();

            return gson.fromJson(actionJson, type);
        } catch (Exception e) {
            e.printStackTrace();
            CrashlyticsWrapper.log(e);
        }
        return null;
    }

    public static void setAction(Context context, Action action) {
        SharedPreferences.Editor editor = getEditor(context);

        Gson gson = getGson();
        String actionJSON = gson.toJson(action);

        editor.putString(CURRENT_ACTION, actionJSON);
        editor.apply();
    }

    public static void deleteAction(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(CURRENT_ACTION);
        editor.apply();
    }

    public static Action getTrackingAction(Context context) {
        String actionJson = getSharedPreferences(context).getString(TRACKING_ACTION, null);
        if (actionJson == null)
            return null;
        try {
            Gson gson = getGson();
            Type type = new TypeToken<Action>() {
            }.getType();

            return gson.fromJson(actionJson, type);
        } catch (Exception e) {
            e.printStackTrace();
            CrashlyticsWrapper.log(e);
        }
        return null;
    }

    public static void setTrackingAction(Context context, Action action) {
        SharedPreferences.Editor editor = getEditor(context);

        Gson gson = getGson();
        String actionJSON = gson.toJson(action);

        editor.putString(TRACKING_ACTION, actionJSON);
        editor.apply();
    }

    public static void deleteTrackingAction(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(TRACKING_ACTION);
        editor.apply();
    }

    public static User getHyperTrackLiveUser(Context context) {
        String userJSON = getSharedPreferences(context).getString(HYPERTRACK_LIVE_USER, null);

        if (userJSON == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();

        return gson.fromJson(userJSON, type);
    }

    public static void setHyperTrackLiveUser(Context context, User user) {
        SharedPreferences.Editor editor = getEditor(context);

        Gson gson = new Gson();
        String userJSON = gson.toJson(user);

        editor.putString(HYPERTRACK_LIVE_USER, userJSON);
        editor.apply();
    }

    public static GeofencingRequest getGeofencingRequest(Context context) {
        String geofencingRequestJSON = getSharedPreferences(context).getString(GEOFENCING_REQUEST, null);
        if (geofencingRequestJSON == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<GeofencingRequest>() {
        }.getType();

        return gson.fromJson(geofencingRequestJSON, type);
    }

    public static void setGeofencingRequest(Context context, GeofencingRequest request) {
        SharedPreferences.Editor editor = getEditor(context);

        Gson gson = new Gson();
        String geofencingRequestJSON = gson.toJson(request);

        editor.putString(GEOFENCING_REQUEST, geofencingRequestJSON);
        editor.apply();
    }

    public static void removeGeofencingRequest(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(GEOFENCING_REQUEST);
        editor.apply();
    }
}