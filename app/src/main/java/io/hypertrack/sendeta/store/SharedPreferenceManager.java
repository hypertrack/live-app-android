
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

import java.lang.reflect.Type;
import java.util.Date;
import java.util.LinkedHashMap;

import io.hypertrack.sendeta.MyApplication;
import io.hypertrack.sendeta.model.HyperTrackLiveUser;
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
    private static final String TRACKING_SETTING = "io.hypertrack.meta:TrackingSetting";
    private static final String TRACKING_DIALOG = "io.hypertrack.meta:TrackingDialog";
    private static final String PREVIOUS_USER_ID = "io.hypertrack.meta:PreviousUserID";

    private static final String FEEDBACK_ACTIVITY_LIST = "io.hypertrack.meta:FeedbackActivityList";

    private static SharedPreferences getSharedPreferences() {
        Context context = MyApplication.getInstance().getApplicationContext();
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

    public static String getPreviousUserId(Context context) {
        return getSharedPreferences().getString(PREVIOUS_USER_ID, null);
    }

    public static void setPreviousUserId(String previousUserId) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(PREVIOUS_USER_ID, previousUserId);
        editor.apply();
    }

    public static void deletePreviousUserId() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(PREVIOUS_USER_ID);
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
            CrashlyticsWrapper.log(e);
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

    public static Action getTrackingAction(Context context) {
        String actionJson = getSharedPreferences().getString(TRACKING_ACTION, null);
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

    public static void setTrackingAction(Action action) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = getGson();
        String actionJSON = gson.toJson(action);

        editor.putString(TRACKING_ACTION, actionJSON);
        editor.apply();
    }

    public static void deleteTrackingAction() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(TRACKING_ACTION);
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

    public static void resetBackgroundTracking() {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(TRACKING_DIALOG, false);
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

    public static void setActivityFeedbackLookupId(String lookupId, String feedbackType) {
        LinkedHashMap<String, String> lookupIds = getActivityFeedbackLookupId();
        lookupIds.put(lookupId, feedbackType);
        if(lookupIds.size()>30){
            lookupIds.remove(lookupIds.size()-1);
        }
        String json = new GsonBuilder().create().toJson(lookupIds);
        SharedPreferences.Editor editor = getEditor();
        editor.putString(FEEDBACK_ACTIVITY_LIST, json);
        editor.apply();
    }

    public static LinkedHashMap<String, String> getActivityFeedbackLookupId() {

        String json = getSharedPreferences().getString(FEEDBACK_ACTIVITY_LIST, "");
        Type type = new TypeToken<LinkedHashMap<String, String>>() {
        }.getType();

        LinkedHashMap<String, String> lookupIds = null;

        try {
            lookupIds = new GsonBuilder().create().fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lookupIds == null) {
            lookupIds = new LinkedHashMap<>();
        }

        return lookupIds;
    }
}
