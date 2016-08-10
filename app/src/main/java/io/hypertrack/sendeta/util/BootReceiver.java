package io.hypertrack.sendeta.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.sendeta.store.TripManager;

/**
 * Created by piyush on 16/07/16.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        HTLog.i(TAG, "SendETA Boot receiver onReceive");

        if (SharedPreferenceManager.getGeofencingRequest() != null) {
            // Add Geofencing Request
            TripManager.getSharedManager().setGeofencingRequest(SharedPreferenceManager.getGeofencingRequest());
            TripManager.getSharedManager().addGeofencingRequest();
        }
    }
}
