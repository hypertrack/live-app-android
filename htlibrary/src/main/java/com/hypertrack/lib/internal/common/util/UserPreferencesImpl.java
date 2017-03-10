package com.hypertrack.lib.internal.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;

import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by piyush on 31/01/17.
 */
public class UserPreferencesImpl implements UserPreferences {

    private static final String TAG = UserPreferences.class.getSimpleName();
    private Context context;
    private static UserPreferencesImpl sInstance;

    private UserPreferencesImpl(Context context) {
        this.context = context;
    }

    public static UserPreferencesImpl getInstance(Context context) {
        if (sInstance == null) {
            synchronized (UserPreferencesImpl.class) {
                if (sInstance == null) {
                    sInstance = new UserPreferencesImpl(context.getApplicationContext());
                }
            }
        }

        return sInstance;
    }

    @Override
    public void clearUserData() {
        setIsTracking(false);
        clearIsFirstLocation();
        clearLastRecordedActivity();
        clearLastRecordedLocation();
        clearUserStop();
    }

    @Override
    public String getSDKPlatform() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        return sharedpreferences.getString(SharedPrefConstants.USER_SDK_PLATFORM_KEY, "Android");
    }

    @Override
    public void setSDKPlatform(String sdkPlatform) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putString(SharedPrefConstants.USER_SDK_PLATFORM_KEY, sdkPlatform);
        editor.apply();
    }

    @Override
    public String getUserId() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        return sharedpreferences.getString(SharedPrefConstants.USER_ID_KEY, null);
    }

    @Override
    public void setUserID(String userID) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putString(SharedPrefConstants.USER_ID_KEY, userID);
        editor.apply();
    }

    @Override
    public boolean isTracking() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        return sharedpreferences.getBoolean(SharedPrefConstants.USER_IS_TRACKING_KEY, false);
    }

    @Override
    public void setIsTracking(boolean isTracking) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putBoolean(SharedPrefConstants.USER_IS_TRACKING_KEY, isTracking);
        editor.apply();
    }

    @Override
    public boolean isFirstLocation() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        return sharedpreferences.getBoolean(SharedPrefConstants.USER_IS_FIRST_LOCATION_KEY, true);
    }

    @Override
    public void setIsFirstLocation(boolean isFirstLocation) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putBoolean(SharedPrefConstants.USER_IS_FIRST_LOCATION_KEY, isFirstLocation);
        editor.apply();
    }

    @Override
    public void clearIsFirstLocation() {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.remove(SharedPrefConstants.USER_IS_FIRST_LOCATION_KEY);
        editor.apply();
    }

    @Override
    public String getGcmToken() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        return sharedpreferences.getString(SharedPrefConstants.GCM_TOKEN_KEY, null);
    }

    @Override
    public void setGcmToken(String gcmToken) {
        if (gcmToken == null || gcmToken.isEmpty()) {
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putString(SharedPrefConstants.GCM_TOKEN_KEY, gcmToken);
        editor.apply();
    }

    @Override
    public String getFcmToken() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        return sharedpreferences.getString(SharedPrefConstants.FCM_TOKEN_KEY, null);
    }

    @Override
    public void setFcmToken(String fcmToken) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putString(SharedPrefConstants.FCM_TOKEN_KEY, fcmToken);
        editor.apply();
    }

    @Override
    public boolean isGcmTokenPushed(@NonNull JSONObject jsonObject) {
        try {
            SharedPreferences sharedpreferences = getSharedPreferences();
            String jsonObjectString = sharedpreferences.getString(
                    SharedPrefConstants.GCM_TOKEN_PUSHED_KEY, null);
            return jsonObjectString != null && jsonObject.toString().equalsIgnoreCase(jsonObjectString);
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while isGcmTokenPushed: " + e);
            return false;
        }
    }

    @Override
    public void setGcmTokenPushed(@NonNull JSONObject jsonObject) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putString(SharedPrefConstants.GCM_TOKEN_PUSHED_KEY, jsonObject.toString());
        editor.apply();
    }

    @Override
    public boolean isFcmTokenPushed(@NonNull JSONObject jsonObject) {
        try {
            SharedPreferences sharedpreferences = getSharedPreferences();
            String jsonObjectString = sharedpreferences.getString(SharedPrefConstants.FCM_TOKEN_PUSHED_KEY,
                    null);
            return jsonObjectString != null && jsonObject.toString().equalsIgnoreCase(jsonObjectString);
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while isFcmTokenPushedToServer: " + e);
            return false;
        }
    }

    @Override
    public void setFcmTokenPushed(@NonNull JSONObject jsonObject) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putString(SharedPrefConstants.FCM_TOKEN_PUSHED_KEY, jsonObject.toString());
        editor.apply();
    }

    @Override
    public void setUserStop(HyperTrackStop hyperTrackStop) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        Gson gson = new Gson();
        editor.putString(SharedPrefConstants.USER_STOP_GEOFENCE, gson.toJson(hyperTrackStop));
        editor.apply();
    }

    @Override
    public HyperTrackStop getUserStop() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        String hyperTrackStopJson = sharedpreferences.getString(SharedPrefConstants.USER_STOP_GEOFENCE, null);
        if (hyperTrackStopJson == null)
            return null;

        Gson gson = new Gson();
        Type type = new TypeToken<HyperTrackStop>() {}.getType();
        return gson.fromJson(hyperTrackStopJson, type);
    }

    @Override
    public void clearUserStop() {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.remove(SharedPrefConstants.USER_STOP_GEOFENCE);
        editor.apply();
    }

    @Override
    public void setLastRecordedActivity(String activityName, int activityConfidence) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putString(SharedPrefConstants.LAST_RECORDED_USER_ACTIVITY, activityName);
        editor.putInt(SharedPrefConstants.LAST_RECORDED_USER_ACTIVITY_CONFIDENCE, activityConfidence);
        editor.apply();
    }

    @Override
    public String getLastRecordedActivityName() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString(SharedPrefConstants.LAST_RECORDED_USER_ACTIVITY, null);
    }

    @Override
    public int getLastRecordedActivityConfidence() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getInt(SharedPrefConstants.LAST_RECORDED_USER_ACTIVITY_CONFIDENCE, -1);
    }

    @Override
    public void clearLastRecordedActivity() {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.remove(SharedPrefConstants.LAST_RECORDED_USER_ACTIVITY);
        editor.remove(SharedPrefConstants.LAST_RECORDED_USER_ACTIVITY_CONFIDENCE);
        editor.apply();
    }

    @Override
    public HyperTrackLocation getLastRecordedLocation() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String locationJson = sharedPreferences.getString(SharedPrefConstants.LAST_RECORDED_LOCATION, null);
        if (locationJson == null)
            return null;

        Gson gson = new Gson();
        Type type = new TypeToken<HyperTrackLocation>() {}.getType();
        return gson.fromJson(locationJson, type);
    }

    @Override
    public void setLastRecordedLocation(HyperTrackLocation location) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        Gson gson = new Gson();
        editor.putString(SharedPrefConstants.LAST_RECORDED_LOCATION, gson.toJson(location));
        editor.apply();
    }

    @Override
    public void clearLastRecordedLocation() {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.remove(SharedPrefConstants.LAST_RECORDED_LOCATION);
        editor.apply();
    }

    @Override
    public void setLastPostToServerTime() {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putLong(SharedPrefConstants.HT_PREFS_PERIODIC_TASK_LAST_UPDATED_TIME, System.currentTimeMillis());
        editor.apply();
    }

    @Override
    public Long getLastPostToServerTime() {
        SharedPreferences sharedpreferences = getSharedPreferences();
        long lastUpdatedTime = sharedpreferences.getLong(SharedPrefConstants.HT_PREFS_PERIODIC_TASK_LAST_UPDATED_TIME,
                Long.MIN_VALUE);

        return (lastUpdatedTime != Long.MIN_VALUE) ? lastUpdatedTime : null;
    }

    @Override
    public void clearLastPostToServerTime() {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.remove(SharedPrefConstants.HT_PREFS_PERIODIC_TASK_LAST_UPDATED_TIME);
        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SharedPrefConstants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }
}
