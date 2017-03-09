package com.hypertrack.lib;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;

import java.util.List;

/**
 * Created by piyush on 22/02/17.
 */
public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();

    public GeofenceTransitionService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        HyperTrackImpl.getInstance().initialize(getApplicationContext(), null);

        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                HTLog.e(TAG, "GeoFencingEvent Error: " + geofencingEvent.getErrorCode());
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
            }

            List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
            if (geofenceList != null && geofenceList.size() > 0) {
                UserPreferences userPreferences = HyperTrackImpl.getInstance().userPreferences;
                HyperTrackStop hyperTrackStop = userPreferences.getUserStop();

                for (Geofence geofence : geofenceList) {
                    if (geofence != null && hyperTrackStop != null
                            && hyperTrackStop.getId().equals(geofence.getRequestId())) {
                        HTLog.i(TAG, "Geofence Triggered for code: " + hyperTrackStop.getId() + ", location: "
                                + geofencingEvent.getTriggeringLocation());

                        // Check if the geofence was triggered for a valid stop
                        if (hyperTrackStop.isStopStarted()) {

                            // Construct stopEndedLocation from geofence location and user's activity details
                            HyperTrackLocation stopEndedLocation = new HyperTrackLocation(geofencingEvent.getTriggeringLocation());
                            stopEndedLocation.setActivityDetails(userPreferences.getLastRecordedActivityName(),
                                    userPreferences.getLastRecordedActivityConfidence());

                            // Check if Geofence TriggeringLocation accuracy is good
                            if (stopEndedLocation.getAccuracy() != null && stopEndedLocation.getAccuracy() <= 100F) {

                                // Log StopEnded event
                                HyperTrackImpl.getInstance().eventsManager.logStopEndedEvent(hyperTrackStop.getId(),
                                        hyperTrackStop, stopEndedLocation);
                                // Broadcast stop.ended event
                                HyperTrackImpl.getInstance().transmitterClient.getBroadcastManager()
                                        .userStopEndedBroadcast(userPreferences.getUserId(), hyperTrackStop);

                            } else {
                                HTLog.w(TAG, "Error occurred in GeofenceTransitionService, Inaccurate Location for Stop ID: "
                                        + hyperTrackStop.getId());
                                return;
                            }
                        }

                        // Remove user's stop
                        HyperTrackImpl.getInstance().transmitterClient.getServiceManager().removeUserStop();
                    } else {
                        HTLog.w(TAG, "Error occurred in GeofenceTransitionService, Geofence Request ID: "
                                + (geofence != null ? geofence.getRequestId() : "null") + " not equal to Stop ID: "
                                + (hyperTrackStop != null ? hyperTrackStop.getId() : "null"));
                    }
                }
            }
        }

        HyperTrackImpl.getInstance().handleIntent(getApplicationContext(), intent);
    }
}
