package com.hypertrack.lib;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hypertrack.lib.internal.common.logging.HTLog;

import java.util.Map;

/**
 * Created by piyush on 04/02/17.
 */
public class HyperTrackFirebaseMessagingService extends FirebaseMessagingService {

    public static final String HT_SDK_NOTIFICATION_KEY = "hypertrack_sdk_notification";
    public static final String HT_USER_ID = "user_id";
    private static final String TAG = HyperTrackFirebaseMessagingService.class.getSimpleName();

    /**
     * Method to be invoked in case your app has an implementation of GcmListenerService of its own.
     * Refer to Android UserSDK Gcm Integration docs for more reference.
     *
     * @param context
     * @param data
     */
    public static void triggerHypertrackNotification(final Context context, Map<String, String> data) {
        HyperTrackImpl.getInstance().initialize(context.getApplicationContext(), null);
        HyperTrackImpl.getInstance().handleIntent(context, null);

        if (data == null || data.size() == 0)
            return;

        String sdkNotification = data.get(HT_SDK_NOTIFICATION_KEY);
        if (sdkNotification != null && sdkNotification.equalsIgnoreCase("true")) {

            String userID = data.get(HT_USER_ID);
            String configuredUserID = HyperTrack.getUserId();

            // Check if the UserID is same as the one currently configured
            if (userID != null && !userID.isEmpty() && userID.equalsIgnoreCase(configuredUserID)) {
                HTLog.i(TAG, "TransmitterFCM Notification Received for userID: " + userID);

                // Flush cached data to server
                HyperTrackImpl.getInstance().transmitterClient.flushCachedDataToServer();
                // UpdateSDKControls for current user
                HyperTrackImpl.getInstance().transmitterClient.updateSDKControls(context, userID);
            }
        }
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        triggerHypertrackNotification(getApplicationContext(), remoteMessage.getData());
    }
}
