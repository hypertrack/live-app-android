package com.hypertrack.lib;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.hypertrack.lib.internal.common.logging.HTLog;

/**
 * Created by piyush on 27/07/16.
 */
public class HyperTrackGcmListenerService extends GcmListenerService {

    public static final String HT_SDK_NOTIFICATION_KEY = "hypertrack_sdk_notification";
    public static final String HT_USER_ID_KEY = "user_id";
    private static final String TAG = HyperTrackGcmListenerService.class.getSimpleName();

    /**
     * Method to be invoked in case your app has an implementation of GcmListenerService of its own.
     * Refer to Android UserSDK Gcm Integration docs for more reference.
     *
     * @param context
     * @param data
     */
    public static void triggerHypertrackNotification(final Context context, Bundle data) {
        HyperTrackImpl.getInstance().initialize(context.getApplicationContext(), null);
        HyperTrackImpl.getInstance().handleIntent(context, null);

        String sdkNotification = data.getString(HT_SDK_NOTIFICATION_KEY);
        if (sdkNotification != null && sdkNotification.equalsIgnoreCase("true")) {

            String userID = data.getString(HT_USER_ID_KEY);
            String configuredUserID = HyperTrack.getUserId();

            // Check if the UserID is same as the one currently configured
            if (userID != null && !userID.isEmpty() && userID.equalsIgnoreCase(configuredUserID)) {
                HTLog.i(TAG, "TransmitterGCM Notification Received for userID: " + userID);

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
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        triggerHypertrackNotification(getApplicationContext(), data);
    }
}
