package io.hypertrack.sendeta.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.hypertrack.lib.internal.common.logging.HTLog;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.store.TaskManager;
import io.hypertrack.sendeta.util.GeofenceErrorMessages;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";


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
                HTLog.e(TAG, "GeoFencingEvent Error: " + errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            // Test that the reported transition was of interest.

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.i(TAG, "User entered Geo Fence");
                HTLog.i(TAG, "User entered Geo Fence");
            }

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.i(TAG, "User exited Geo fence");
                HTLog.i(TAG, "User exited Geo fence");
            }

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                Log.i(TAG, "User is dwelling in geo fence.");
                HTLog.i(TAG, "User is dwelling in geo fence.");
                TaskManager.getSharedManager(getApplicationContext()).OnGeoFenceSuccess();

            } else {
                // Log the error.
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));
                HTLog.e(TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));
            }

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition,
                    triggeringGeofences);

            Log.i("Debug", "GeoFenceTransition Details: " + geofenceTransitionDetails);
            HTLog.i(TAG, "GeoFenceTransition Details: " + geofenceTransitionDetails);

        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(Context context, int geofenceTransition,
                                                List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return getString(R.string.geofence_transition_dwelled);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}
