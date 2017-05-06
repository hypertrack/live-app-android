package io.hypertrack.sendeta.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.hypertrack.lib.HyperTrack;

import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;

/**
 * Created by piyush on 08/07/16.
 */
public class GpsLocationReceiver extends BroadcastReceiver {

    public static final String LOCATION_CHANGED = "LOCATION_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

            if (HyperTrack.checkLocationServices(context) && SharedPreferenceManager.getGeofencingRequest() != null) {
                // Add Geofencing Request
                ActionManager.getSharedManager(context).setGeofencingRequest(SharedPreferenceManager.getGeofencingRequest());
                ActionManager.getSharedManager(context).addGeofencingRequest();
            }

            Intent locationChangedIntent = new Intent(LOCATION_CHANGED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(locationChangedIntent);
        }
    }
}
