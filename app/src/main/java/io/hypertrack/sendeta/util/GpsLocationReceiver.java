package io.hypertrack.sendeta.util;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import io.hypertrack.sendeta.store.TripManager;

/**
 * Created by piyush on 08/07/16.
 */
public class GpsLocationReceiver extends BroadcastReceiver {

    public static final String LOCATION_CHANGED = "LOCATION_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

            if (isLocationEnabled(context) && SharedPreferenceManager.getGeofencingRequest() != null) {
                // Add Geofencing Request
                TripManager.getSharedManager().setGeofencingRequest(SharedPreferenceManager.getGeofencingRequest());
                TripManager.getSharedManager().addGeofencingRequest();
            }

            Intent locationChangedIntent = new Intent(LOCATION_CHANGED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(locationChangedIntent);
        }
    }

    private boolean isLocationEnabled(Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            // Find out what the settings say about which providers are enabled
            int mode = Settings.Secure.getInt(
                    contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            if (mode == Settings.Secure.LOCATION_MODE_OFF) {
                // Location is turned OFF!
                return false;
            } else {
                // Location is turned ON!
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
