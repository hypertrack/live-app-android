package io.hypertrack.sendeta.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;
import io.hypertrack.sendeta.store.TripManager;
import io.hypertrack.sendeta.util.GeofenceErrorMessages;
import io.hypertrack.sendeta.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";
    private HTTransmitterService transmitterService;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(this,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            // Test that the reported transition was of interest.

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                HTLog.i(TAG, "User entered Geo Fence");
            }

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                HTLog.i(TAG, "User exited Geo fence");
            }

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                HTLog.i(TAG, "User is dwelling in geo fence.");
                TripManager.getSharedManager().OnGeoFenceSuccess();

            } else {
                // Log the error.
                HTLog.e(TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));
            }

        }
    }

}
