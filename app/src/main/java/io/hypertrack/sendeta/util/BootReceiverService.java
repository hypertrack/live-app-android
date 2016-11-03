package io.hypertrack.sendeta.util;

import android.app.IntentService;
import android.content.Intent;

import io.hypertrack.sendeta.store.TaskManager;

/**
 * Created by piyush on 03/11/16.
 */
public class BootReceiverService extends IntentService {

    public BootReceiverService() {
        super(BootReceiverService.class.getSimpleName());
    }

    public BootReceiverService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (SharedPreferenceManager.getGeofencingRequest() != null) {
            // Add Geofencing Request
            TaskManager.getSharedManager(getApplicationContext()).setGeofencingRequest(SharedPreferenceManager.getGeofencingRequest());
            TaskManager.getSharedManager(getApplicationContext()).addGeofencingRequest();
        }
    }
}
