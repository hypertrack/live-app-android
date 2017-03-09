package com.hypertrack.lib.internal.transmitter.devicehealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.logging.HTLog;

/**
 * Created by piyush on 06/08/16.
 */

/**
 * package
 */
class LocationState {

    private static final String TAG = "LocationState";

    private static final String HT_SHARED_PREFERENCE_LOCATION_ENABLED = "com.hypertrack:Location.LocationEnabled";
    private static final String HT_SHARED_PREFERENCE_LOCATION_PROVIDER = "com.hypertrack:Location.LocationProvider";
    private static final String HT_SHARED_PREFERENCE_LOCATION_ACCURACY = "com.hypertrack:Location.LocationAccuracy";
    private static final String HT_SHARED_PREFERENCE_LOCATION_PERMISSION = "com.hypertrack:Location.LocationPermission";

    private static final String LOCATION_PROVIDER_GPS = "gps";
    private static final String LOCATION_PROVIDER_NETWORK = "network";
    private static final String LOCATION_PROVIDER_PASSIVE = "passive";
    private static final String LOCATION_PROVIDER_DISABLED = "disabled";
    private static final String LOCATION_PROVIDER_INVALID = "invalid";

    private static final String LOCATION_ACCURACY_HIGH = "high";
    private static final String LOCATION_ACCURACY_MEDIUM = "medium";
    private static final String LOCATION_ACCURACY_LOW = "low";

    private Context mContext;

    LocationState(Context mContext) {
        this.mContext = mContext;
    }

    // Methods to clear cached LocationState Data
    static void clearSavedLocationStateData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HT_SHARED_PREFERENCE_LOCATION_ENABLED);
        editor.remove(HT_SHARED_PREFERENCE_LOCATION_PROVIDER);
        editor.remove(HT_SHARED_PREFERENCE_LOCATION_ACCURACY);
        editor.remove(HT_SHARED_PREFERENCE_LOCATION_PERMISSION);
        editor.apply();
    }

    LocationHealth getLocationHealth() {
        try {
            // Fetch Current LocationState Params
            LocationHealth locationHealth = new LocationHealth();
            locationHealth.isLocationEnabled = HyperTrackUtils.isLocationEnabled(mContext);
            locationHealth.locationProvider = getLocationProvider(mContext);
            locationHealth.locationAccuracy = getLocationAccuracy(mContext);
            locationHealth.locationPermissionAvailable = HyperTrackUtils.isLocationPermissionAvailable(mContext);

            // Fetch Cached LocationState Params
            LocationHealth savedLocationHealth = getSavedLocationHealth();

            if (!locationHealth.equals(savedLocationHealth)) {
                saveLocationHealth(locationHealth);
                return locationHealth;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getLocationHealth: " + e.getMessage());
        }

        return null;
    }

    private String getLocationProvider(Context context) {
        String locationProvider = HyperTrackUtils.getLocationProvider(context);
        try {
            if (locationProvider == null) {
                return LOCATION_PROVIDER_DISABLED;

            } else if (locationProvider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                return LOCATION_PROVIDER_GPS;

            } else if (locationProvider.equalsIgnoreCase(LocationManager.PASSIVE_PROVIDER)) {
                return LOCATION_PROVIDER_NETWORK;

            } else if (locationProvider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                return LOCATION_PROVIDER_PASSIVE;

            } else {
                return LOCATION_PROVIDER_DISABLED;
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while detecting LocationProvider: " + e.getMessage());
            return LOCATION_PROVIDER_INVALID;
        }
    }

    private String getLocationAccuracy(Context context) {
        try {
            int locationAccuracy = HyperTrackUtils.getLocationAccuracy(context);

            switch (locationAccuracy) {
                case HyperTrackUtils.LOCATION_MODE_HIGH_ACCURACY:
                    return LOCATION_ACCURACY_HIGH;

                case HyperTrackUtils.LOCATION_MODE_SENSORS_ONLY:
                    return LOCATION_ACCURACY_MEDIUM;

                case HyperTrackUtils.LOCATION_MODE_BATTERY_SAVING:
                    return LOCATION_ACCURACY_LOW;

                case HyperTrackUtils.LOCATION_MODE_OFF:
                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while detecting getAccuracy: " + e.getMessage());
            return null;
        }
    }

    private LocationHealth getSavedLocationHealth() {
        LocationHealth locationHealth = new LocationHealth();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY,
                Context.MODE_PRIVATE);

        // Check if LocationHealth keys were present, Return null otherwise
        if (sharedPreferences.contains(HT_SHARED_PREFERENCE_LOCATION_ENABLED)
                || sharedPreferences.contains(HT_SHARED_PREFERENCE_LOCATION_PROVIDER)
                || sharedPreferences.contains(HT_SHARED_PREFERENCE_LOCATION_ACCURACY)
                || sharedPreferences.contains(HT_SHARED_PREFERENCE_LOCATION_PERMISSION)) {

            locationHealth.isLocationEnabled = sharedPreferences.getBoolean(HT_SHARED_PREFERENCE_LOCATION_ENABLED, false);
            locationHealth.locationAccuracy = sharedPreferences.getString(HT_SHARED_PREFERENCE_LOCATION_ACCURACY, null);
            locationHealth.locationProvider = sharedPreferences.getString(HT_SHARED_PREFERENCE_LOCATION_PROVIDER, null);
            locationHealth.locationPermissionAvailable = sharedPreferences.getBoolean(HT_SHARED_PREFERENCE_LOCATION_PERMISSION, false);

            return locationHealth;
        }

        return null;
    }

    private void saveLocationHealth(LocationHealth locationHealth) {
        SharedPreferences sharedpreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(HT_SHARED_PREFERENCE_LOCATION_ENABLED, locationHealth.isLocationEnabled);
        editor.putString(HT_SHARED_PREFERENCE_LOCATION_PROVIDER, locationHealth.locationProvider);
        editor.putString(HT_SHARED_PREFERENCE_LOCATION_ACCURACY, locationHealth.locationAccuracy);
        editor.putBoolean(HT_SHARED_PREFERENCE_LOCATION_PERMISSION, locationHealth.locationPermissionAvailable);
        editor.apply();
    }

    class LocationHealth {
        @SerializedName("enabled")
        private boolean isLocationEnabled;

        @SerializedName("provider")
        private String locationProvider;

        @SerializedName("accuracy")
        private String locationAccuracy;

        @SerializedName("permission")
        private boolean locationPermissionAvailable;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LocationHealth that = (LocationHealth) o;

            if (isLocationEnabled != that.isLocationEnabled) return false;
            if (locationPermissionAvailable != that.locationPermissionAvailable) return false;
            if (!locationProvider.equals(that.locationProvider)) return false;
            return locationAccuracy != null ? locationAccuracy.equals(that.locationAccuracy) : that.locationAccuracy == null;

        }
    }
}
