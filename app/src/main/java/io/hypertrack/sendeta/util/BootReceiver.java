package io.hypertrack.sendeta.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.sendeta.store.TaskManager;

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
            TaskManager.getSharedManager(context).setGeofencingRequest(SharedPreferenceManager.getGeofencingRequest());
            TaskManager.getSharedManager(context).addGeofencingRequest();
        }
    }
}
