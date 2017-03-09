package com.hypertrack.lib.internal.transmitter.utils;

import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;

/**
 * Created by piyush on 01/02/17.
 */
public interface BroadcastManager {
    void userConnSuccessfulBroadcast(String userID);

    void userCurrentLocationBroadcast(HyperTrackLocation location, String userID);
    void userTrackingStartedBroadcast(String userID);

    void userTrackingEndedBroadcast(String userID);
    void userStopStartedBroadcast(String userID, HyperTrackStop stop);
    void userStopEndedBroadcast(String userID, HyperTrackStop stop);

    void userActionCompletedBroadcast(String userID, String actionId);
}
