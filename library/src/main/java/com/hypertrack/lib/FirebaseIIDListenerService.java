package com.hypertrack.lib;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hypertrack.lib.internal.common.logging.HTLog;

/**
 * Created by piyush on 04/02/17.
 */
public class FirebaseIIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = FirebaseIIDListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        try {
            // Get updated InstanceID token.
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            HTLog.d(TAG, "HyperTrack FCM token: " + refreshedToken);

            // Save FcmToken to UserPreferences
            HyperTrackImpl.getInstance().userPreferences.setFcmToken(refreshedToken);
            HTLog.i(TAG, "Transmitter Fcm Token refreshed successfully.");

            // Post FcmToken fetched to server
            HyperTrackImpl.getInstance().transmitterClient.postRegistrationToken();

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while FirebaseIIDListenerService.onTokenRefresh: " + e);
        }
    }
}
