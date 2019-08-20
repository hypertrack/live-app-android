package com.hypertrack.live.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.hypertrack.live.R;

public class NotificationUtils {
    public static final int GPS_DISABLED_ERROR_ID = 101;

    public static void sendGpsDisabledError(Context context) {
        NotificationCompat.Builder builder = createNotification(context,
                new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        builder.setContentTitle(context.getString(R.string.tap_to_turn_location_settings));

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(GPS_DISABLED_ERROR_ID, builder.build());
    }

    public static void cancelGpsDisabledError(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GPS_DISABLED_ERROR_ID);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context) {
        String channelId = context.getString(R.string.default_notification_channel_id);
        String channelName = "Warframe market";
        NotificationChannel channel = new NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static NotificationCompat.Builder createNotification(Context context, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_status_bar)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);
        }
        return notificationBuilder;
    }
}
