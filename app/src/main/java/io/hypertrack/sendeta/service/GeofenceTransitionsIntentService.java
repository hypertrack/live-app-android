
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.util.ErrorMessages;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
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
                String errorMessage = ErrorMessages.getGeofenceErrorString(this,
                        geofencingEvent.getErrorCode());
                HyperLog.e(TAG, errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                HyperLog.i(TAG, "User is dwelling in geo fence.");
                ActionManager.getSharedManager(getApplicationContext()).OnGeoFenceSuccess();

            } else {
                // Log the error.
                HyperLog.e(TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));
            }

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    geofencingEvent.getTriggeringGeofences());

            HyperLog.i(TAG, "GeoFenceTransition Details: " + geofenceTransitionDetails);
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition  The ID of the geofence transition.
     * @param triggeringGeofences The geofence(s) triggered.
     * @return The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(int geofenceTransition,
                                                List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
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
