package io.hypertrack.sendeta.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.view.Home;

/**
 * Created by piyush on 27/07/16.
 */
public class MetaGCMListenerService extends GcmListenerService {

    private static final String TAG = MetaGCMListenerService.class.getSimpleName();

    private static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_CALLBACK_URL = "callback";
    private static final String KEY_NOTIFICATION_TYPE = "notificationType";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PENDING_INTENT = "pintent";
    private static final String KEY_BIG_TEXT = "bigText";
    private static final String KEY_TEMPLATE = "template";
    private static final String KEY_DEEP_LINK = "dl";

    private static final String TYPE_NOTIFICATION = "notification";

    // template types
    public static final int TEMPLATE_STANDARD = 0;
    public static final int TEMPLATE_BIG_TEXT = 1;
    public static final int TEMPLATE_BIG_TEXT_WITH_ACTION = 2;
    public static final int TEMPLATE_IMAGE = 3;
    public static final int TEMPLATE_IMAGE_WITH_ACTION = 4;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        parseGCMMessage(data);

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
//        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    private void parseGCMMessage(Bundle data) {
        if (data == null)
            return;
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Test Notification")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

//    private void sendNotification() {
//        // Gets an instance of notification builder object
//        final android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(mContext);
//
//        // Sets the notification id to menu id
//        final int mNotificationId = menuId;
//
//        // Creates an intent to be passed on click
//        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.putExtra(Constants.NOTIFICATION_TITILE, title);
//        intent.putExtra(Constants.NOTIFICATION_DESC, text);
//        intent.putExtra(Constants.NOTIFICATION_KEY_DEEP_LINK, intentParams);
//        intent.putExtra(Constants.NOTIFICATION_KEY_ID, mNotificationId);
//        // the requestCode has been set to mId because the pending intent used to be replaced
//        // for an old un-clicked notification when a new notification is received
//        // (due to use of PendingIntent.FLAG_UPDATE_CURRENT which replaces the existing pending
//        // intent with same request code)
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), menuId, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Sets the parameters required for standard notification
//        builder.setContentTitle("SendETA")
//                .setContentText("")
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent);
//
//        if (!TextUtils.isEmpty(title)) {
//            builder.setContentTitle(title);
//        }
//        if (!TextUtils.isEmpty(text)) {
//            builder.setContentText(text);
//        }
//    }

}
