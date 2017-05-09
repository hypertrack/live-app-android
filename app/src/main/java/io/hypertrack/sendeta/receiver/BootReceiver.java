package io.hypertrack.sendeta.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hypertrack.lib.internal.common.logging.HTLog;

import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;

/**
 * Created by piyush on 16/07/16.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        HTLog.i(TAG, "HyperTrackLive Boot receiver onReceive");

        if (SharedPreferenceManager.getGeofencingRequest() != null) {
            // Add Geofencing Request
            ActionManager.getSharedManager(context).setGeofencingRequest(SharedPreferenceManager.getGeofencingRequest());
            ActionManager.getSharedManager(context).addGeofencingRequest();
        }
    }
}
