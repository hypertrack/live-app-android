package com.hypertrack.lib.internal.transmitter.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.hypertrack.lib.HyperTrackConstants;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;

/**
 * Created by piyush on 01/02/17.
 */
public class BroadcastManagerImpl implements BroadcastManager {
    private Context context;

    public BroadcastManagerImpl(Context context) {
        this.context = context;
    }

    public void userConnSuccessfulBroadcast(String userID) {
        Intent intent = new Intent(HyperTrackConstants.HT_USER_CONNECTION_SUCCESSFUL_INTENT);
        intent.putExtra(HyperTrackConstants.HT_USER_ID_KEY, userID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userCurrentLocationBroadcast(HyperTrackLocation location, String userID) {
        Intent intent = new Intent(HyperTrackConstants.HT_USER_CURRENT_LOCATION_INTENT);
        intent.putExtra(HyperTrackConstants.HT_USER_ID_KEY, userID);
        intent.putExtra(HyperTrackConstants.HT_USER_CURRENT_LOCATION_KEY, location);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userTrackingStartedBroadcast(String userID) {
        Intent intent = new Intent(HyperTrackConstants.HT_USER_TRACKING_STARTED_INTENT);
        intent.putExtra(HyperTrackConstants.HT_USER_ID_KEY, userID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userTrackingEndedBroadcast(String userID) {
        Intent intent = new Intent(HyperTrackConstants.HT_USER_TRACKING_STOPPED_INTENT);
        intent.putExtra(HyperTrackConstants.HT_USER_ID_KEY, userID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userStopStartedBroadcast(String userID, HyperTrackStop stop) {
        Intent intent = new Intent(HyperTrackConstants.HT_USER_STOP_STARTED_INTENT);
        intent.putExtra(HyperTrackConstants.HT_USER_ID_KEY, userID);
        intent.putExtra(Constants.HT_USER_STOP_KEY, stop);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userStopEndedBroadcast(String userID, HyperTrackStop stop) {
        Intent intent = new Intent(HyperTrackConstants.HT_USER_STOP_ENDED_INTENT);
        intent.putExtra(HyperTrackConstants.HT_USER_ID_KEY, userID);
        intent.putExtra(Constants.HT_USER_STOP_KEY, stop);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userActionCompletedBroadcast(String userID, String actionId) {
        Intent intent = new Intent(HyperTrackConstants.HT_ACTION_COMPLETED_INTENT);
        intent.putExtra(HyperTrackConstants.HT_USER_ID_KEY, userID);
        intent.putExtra(HyperTrackConstants.HT_ACTION_ID_KEY, actionId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
