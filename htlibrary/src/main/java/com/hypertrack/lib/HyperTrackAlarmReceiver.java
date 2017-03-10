package com.hypertrack.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;
import com.hypertrack.lib.internal.transmitter.utils.Constants;

/**
 * Created by piyush on 23/02/17.
 */
public class HyperTrackAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        HyperTrackImpl.getInstance().initialize(context.getApplicationContext(), null);

        if (intent != null) {
            // Check if Alarm scheduled is for Stop Geofence timeout
            if (Constants.INTENT_ACTION_STOP_TIMEOUT_ALARM.equals(intent.getAction())) {
                handleStopTimeoutAlarm();

            // Check if Alarm scheduled is for SDKControls BatchDuration TTL
            } else if (Constants.INTENT_ACTION_SDK_CONTROLS_TTL_ALARM.equals(intent.getAction())) {
                HyperTrackImpl.getInstance().transmitterClient.onBatchDurationTTLExpired();
            }
        }

        HyperTrackImpl.getInstance().handleIntent(context, intent);
    }

    private void handleStopTimeoutAlarm() {
        UserPreferences userPreferences = HyperTrackImpl.getInstance().userPreferences;

        // Get the Geofence saved in UserPreferences
        HyperTrackStop hyperTrackStop = userPreferences.getUserStop();
        if (hyperTrackStop != null) {

            // Update Stop.start timeout expired flag
            userPreferences.setUserStop(hyperTrackStop.updateStartTimeoutExpired());

            // Log Stop.started event
            HyperTrackImpl.getInstance().eventsManager.logStopStartedEvent(hyperTrackStop.getId(),
                    hyperTrackStop);

            // Broadcast User stop started intent
            HyperTrackImpl.getInstance().transmitterClient.getBroadcastManager()
                    .userStopStartedBroadcast(userPreferences.getUserId(), hyperTrackStop);
        }
    }
}