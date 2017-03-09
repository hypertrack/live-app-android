package com.hypertrack.lib.internal.transmitter.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

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
        Intent intent = new Intent(TransmitterConstants.HT_USER_CONNECTION_SUCCESSFUL_INTENT);
        intent.putExtra(TransmitterConstants.HT_USER_ID_KEY, userID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userCurrentLocationBroadcast(HyperTrackLocation location, String userID) {
        Bundle bundle = new Bundle();
        bundle.putString(TransmitterConstants.HT_USER_ID_KEY, userID);
        bundle.putSerializable(TransmitterConstants.HT_USER_CURRENT_LOCATION_KEY, location);

        Intent intent = new Intent(TransmitterConstants.HT_USER_CURRENT_LOCATION_INTENT);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userTrackingStartedBroadcast(String userID) {
        Intent intent = new Intent(TransmitterConstants.HT_USER_TRACKING_STARTED_INTENT);
        intent.putExtra(TransmitterConstants.HT_USER_ID_KEY, userID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userTrackingEndedBroadcast(String userID) {
        Intent intent = new Intent(TransmitterConstants.HT_USER_TRACKING_STOPPED_INTENT);
        intent.putExtra(TransmitterConstants.HT_USER_ID_KEY, userID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userStopStartedBroadcast(String userID, HyperTrackStop stop) {
        Intent intent = new Intent(TransmitterConstants.HT_USER_STOP_STARTED_INTENT);
        intent.putExtra(TransmitterConstants.HT_USER_ID_KEY, userID);
        intent.putExtra(Constants.HT_USER_STOP_KEY, stop);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userStopEndedBroadcast(String userID, HyperTrackStop stop) {
        Intent intent = new Intent(TransmitterConstants.HT_USER_STOP_ENDED_INTENT);
        intent.putExtra(TransmitterConstants.HT_USER_ID_KEY, userID);
        intent.putExtra(Constants.HT_USER_STOP_KEY, stop);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void userActionCompletedBroadcast(String userID, String actionId) {
        Intent intent = new Intent(TransmitterConstants.HT_ACTION_COMPLETED_INTENT);
        intent.putExtra(TransmitterConstants.HT_USER_ID_KEY, userID);
        intent.putExtra(TransmitterConstants.HT_ACTION_ID_KEY, actionId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
