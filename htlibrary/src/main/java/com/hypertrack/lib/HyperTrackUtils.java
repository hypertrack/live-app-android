package com.hypertrack.lib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.models.Error;

/**
 * Created by piyush on 02/02/17.
 */
public class HyperTrackUtils {
    private static final String TAG = HyperTrackUtils.class.getSimpleName();

    /**
     * Location access disabled.
     */
    public static final int LOCATION_MODE_OFF = 0;
    /**
     * Network Location Provider disabled, but GPS and other sensors enabled.
     */
    public static final int LOCATION_MODE_SENSORS_ONLY = 1;
    /**
     * Reduced power usage, such as limiting the number of GPS updates per hour. Requests
     * with {@link android.location.Criteria#POWER_HIGH} may be downgraded to
     * {@link android.location.Criteria#POWER_MEDIUM}.
     */
    public static final int LOCATION_MODE_BATTERY_SAVING = 2;
    /**
     * Best-effort location computation allowed.
     */
    public static final int LOCATION_MODE_HIGH_ACCURACY = 3;

    /**
     * Location Utility Methods
     */

    public static boolean isLocationEnabled(Context context) {
        // Check if Current Device's SDK Version is Kitkat (Android 4.4 = API 19) & above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                HTLog.e(TAG, "Exception occurred while detecting isLocationEnabled: " + e.getMessage());
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
    }

    public static String getLocationProvider(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;

        } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            return LocationManager.PASSIVE_PROVIDER;

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }

        return null;
    }

    public static boolean isLocationAccuracyHigh(Context context) {
        return getLocationAccuracy(context) == LOCATION_MODE_HIGH_ACCURACY;
    }

    public static int getLocationAccuracy(Context context) {
        // Check if Current Device's SDK Version is Kitkat (Android 4.4 = API 19) & above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                HTLog.e(TAG, "Exception occurred while detecting getAccuracy: " + e.getMessage());
                return Settings.Secure.LOCATION_MODE_OFF;
            }
        } else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    return LOCATION_MODE_HIGH_ACCURACY;
                } else {
                    return LOCATION_MODE_SENSORS_ONLY;
                }
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                return LOCATION_MODE_BATTERY_SAVING;
            }
        }

        return LOCATION_MODE_OFF;
    }

    public static boolean isLocationPermissionAvailable(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Method to check if the Location Services are enabled and in case not, request user to
     * enable them.
     */
    public static void checkIfLocationIsEnabled(GoogleApiClient apiClient, LocationRequest request,
                                          ResultCallback<LocationSettingsResult> callback) {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request).setAlwaysShow(true);
        PendingResult<LocationSettingsResult> pendingResult =
                LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());
        pendingResult.setResultCallback(callback);
    }

    /**
     * Network Connectivity Utility Methods
     */

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    /**
     * Play Services Utility Methods
     */

    public static int isPlayServicesAvailable(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    /**
     * User Credentials Validation Utility Methods
     */

    public static boolean isPublishableKeyConfigured(Context context) {
        String publishableKey = HyperTrack.getPublishableKey(context);

        if (publishableKey != null && publishableKey.contains("sk_")) {
            HTLog.w(TAG, Error.Message.SECRET_KEY_USED_AS_PUBLISHABLE_KEY);
        }

        return !TextUtils.isEmpty(publishableKey);
    }

    /**
     * Method to calculate distance between two sets of Locations [(lat1, lon1), (lat2, lon2)]
     *
     * @param lat1  Latitude param of the first location
     * @param lon1  Longitude param of the first location
     * @param lat2  Latitude param of the first location
     * @param lon2  Longitude param of the first location
     * @param unit  Unit in which the distance needs to be calculated
     * @return Returns the computed distance between the locations
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit.equalsIgnoreCase("K") || unit.equalsIgnoreCase("km")) {
            dist = dist * 1.609344;
        } else if (unit.equalsIgnoreCase("N") || unit.equalsIgnoreCase("nautical")) {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /**
     * Call this method to convert decimal degrees to radians
     * @param deg   Decimal degree param to be converted to radian
     * @return Returns radian value for a given decimal degree
     */
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Call this method to convert radians to decimal degrees
     * @param rad   Radian param to be converted to decimal degree
     * @return Returns decimal degree value for a given radian
     */
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
