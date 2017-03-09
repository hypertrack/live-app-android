package com.hypertrack.lib.internal.transmitter.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.models.ServiceNotificationAction;
import com.hypertrack.lib.models.ServiceNotificationActionIntentType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by piyush on 21/09/16.
 */

public class HTServiceNotificationUtils {

    public static final String TAG = HTServiceNotificationUtils.class.getSimpleName();
    public static final int NOTIFICATION_CLICK_INTENT_REQUEST_CODE = 101010;
    public static final int NOTIFICATION_ACTION_INTENT_REQUEST_CODE = 111111;

    public static Set<String> getNotificationIntentExtras(ArrayList<String> intentExtrasList) {
        if (intentExtrasList != null && !intentExtrasList.isEmpty()) {
            Set<String> intentExtrasSet = new HashSet<>();

            for (String intentExtra : intentExtrasList) {
                intentExtrasSet.add(intentExtra);
            }

            return intentExtrasSet;
        }

        return null;
    }
    public static Set<String> getNotificationActionListJSON(Gson gson, List<ServiceNotificationAction> actionsList) {
        if (actionsList != null && !actionsList.isEmpty()) {
            Set<String> actionListJSON = new HashSet<>();

            for (ServiceNotificationAction action : actionsList) {
                String actionJSON = gson.toJson(action);
                actionListJSON.add(actionJSON);
            }

            return actionListJSON;
        }

        return null;
    }

    public static Notification getForegroundNotification(Context context) {
        Notification notification = new Notification();

        PendingIntent pendingIntent = getNotificationPendingIntent(context);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        try {
            final ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            final int appIconResId = applicationInfo.icon;
            final String appName = (String) context.getPackageManager().getApplicationLabel(applicationInfo);

            // Set NotificationTitle
            String notificationTitle = getNotificationTitle(context);
            if (notificationTitle == null) {
                notificationTitle = context.getString(com.hypertrack.lib.R.string.ht_service_notification_title, appName);
            }

            // Set NotificationText
            String notificationText = getNotificationText(context);
            if (notificationText == null) {
                notificationText = context.getString(com.hypertrack.lib.R.string.ht_service_notification_text);
            }

            // Check if HT Service Notification Large Icon was defined or not
            int largeIconResId = getLargeIconResId(context);
            if (largeIconResId == 0) {
                largeIconResId = context.getResources().getIdentifier("ic_ht_service_notification_large",
                        "drawable", context.getPackageName());
            }

            // Check if HT Service Notification Small Icon was defined or not
            int smallIconResId = getSmallIconResId(context);
            if (smallIconResId == 0) {
                smallIconResId = context.getResources().getIdentifier("ic_ht_service_notification_small",
                        "drawable", context.getPackageName());
                if (smallIconResId == 0) {
                    smallIconResId = appIconResId;
                }
            }

            final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.bigText(notificationText);

            // Sets the parameters required for standard notification
            builder.setStyle(bigTextStyle)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setSmallIcon(smallIconResId)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIconResId))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent);

            int smallIconBGColor = getSmallIconBGColor(context);
            if (smallIconBGColor != 0) {
                builder.setColor(smallIconBGColor);
            }

            try {
                // Add Actions to Service Notification
                List<NotificationCompat.Action> actions = getNotificationActions(context);
                for (NotificationCompat.Action action : actions) {
                    if (action != null) {
                        builder.addAction(action);
                    }
                }
            } catch (ClassNotFoundException e) {
                HTLog.e(TAG, "Exception occurred while getNotificationActions: " + e.getMessage());
            }

            notification = builder.build();

            RemoteViews remoteViews = getNotificationRemoteViews(context);
            if (remoteViews != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification.bigContentView = remoteViews;
                    notification.contentView = remoteViews;
                } else {
                    notification.contentView = remoteViews;
                }
            }

        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getForegroundNotification: " + e.getMessage());
        }

        return notification;
    }

    private static PendingIntent getNotificationPendingIntent(Context context) {
        String className = getNotificationClassName(context);
        Intent intent = null;

        if (!TextUtils.isEmpty(className)) {
            Class<?> intentClass;

            try {
                intentClass = Class.forName(className);
                intent = new Intent(context, intentClass);

            } catch (ClassNotFoundException e) {
                HTLog.e(TAG, "Exception occurred while getNotificationPendingIntent: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        }

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(TransmitterConstants.HT_SERVICE_NOTIFICATION_INTENT_TYPE,
                    TransmitterConstants.HT_SERVICE_NOTIFICATION_INTENT_TYPE_NOTIFICATION_CLICK);
            intent.putStringArrayListExtra(TransmitterConstants.HT_SERVICE_NOTIFICATION_INTENT_EXTRAS_LIST,
                    getNotificationIntentExtras(context));
        }

        return PendingIntent.getActivity(context, NOTIFICATION_CLICK_INTENT_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static int getLargeIconResId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedpreferences.getInt(Constants.HT_PREFS_NOTIFICATION_LARGE_ICON_RES_ID, 0);
    }

    private static int getSmallIconResId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedpreferences.getInt(Constants.HT_PREFS_NOTIFICATION_SMALL_ICON_RES_ID, 0);
    }

    private static int getSmallIconBGColor(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedpreferences.getInt(Constants.HT_PREFS_NOTIFICATION_SMALL_ICON_BG_COLOR, 0);
    }

    private static String getNotificationClassName(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedpreferences.getString(Constants.HT_PREFS_NOTIFICATION_INTENT_CLASS_NAME, null);
    }

    private static ArrayList<String> getNotificationIntentExtras(Context context) {
        SharedPreferences sharedpreferences =
                context.getSharedPreferences(Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        Set<String> intentExtrasSet = sharedpreferences.getStringSet(
                Constants.HT_PREFS_NOTIFICATION_INTENT_EXTRAS, null);

        if (intentExtrasSet == null || intentExtrasSet.isEmpty())
            return null;

        return new ArrayList<>(intentExtrasSet);
    }

    private static String getNotificationTitle(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedpreferences.getString(Constants.HT_PREFS_NOTIFICATION_TITLE, null);
    }

    private static String getNotificationText(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedpreferences.getString(Constants.HT_PREFS_NOTIFICATION_TEXT, null);
    }

    private static List<NotificationCompat.Action> getNotificationActions(Context context) throws ClassNotFoundException {
        List<NotificationCompat.Action> actions = new ArrayList<>();
        List<ServiceNotificationAction> actionParamsList = getNotificationActionParamsList(context);

        if (actionParamsList != null && !actionParamsList.isEmpty()) {

            for (ServiceNotificationAction actionParams : actionParamsList) {

                if (actionParams == null)
                    continue;

                String actionClassName = actionParams.getActionIntentClassName();
                Intent actionIntent = null;
                if (!TextUtils.isEmpty(actionClassName)) {
                    Class<?> intentClass = Class.forName(actionClassName);
                    actionIntent = new Intent(context, intentClass);
                    actionIntent.putExtra(TransmitterConstants.HT_SERVICE_NOTIFICATION_INTENT_TYPE,
                            TransmitterConstants.HT_SERVICE_NOTIFICATION_INTENT_TYPE_ACTION_CLICK);
                    actionIntent.putExtra(TransmitterConstants.HT_SERVICE_NOTIFICATION_INTENT_ACTION_TEXT,
                            actionParams.getActionText());
                    actionIntent.putStringArrayListExtra(TransmitterConstants.HT_SERVICE_NOTIFICATION_INTENT_EXTRAS_LIST,
                            actionParams.getActionIntentExtras());
                }

                PendingIntent actionPendingIntent = null;

                if (ServiceNotificationActionIntentType.ACTION_INTENT_TYPE_ACTIVITY.equals(actionParams.getActionIntentType())) {
                    actionPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ACTION_INTENT_REQUEST_CODE,
                            actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else if (ServiceNotificationActionIntentType.ACTION_INTENT_TYPE_SERVICE.equals(actionParams.getActionIntentType())) {
                    actionPendingIntent = PendingIntent.getService(context, NOTIFICATION_ACTION_INTENT_REQUEST_CODE,
                            actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else if (ServiceNotificationActionIntentType.ACTION_INTENT_TYPE_BROADCAST.equals(actionParams.getActionIntentType())) {
                    actionPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ACTION_INTENT_REQUEST_CODE,
                            actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }

                if (actionPendingIntent != null) {
                    NotificationCompat.Action action = new NotificationCompat.Action(actionParams.getActionIconResId(),
                            actionParams.getActionText(), actionPendingIntent);
                    actions.add(action);
                }
            }
        }

        return actions;
    }

    private static List<ServiceNotificationAction> getNotificationActionParamsList(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        Set<String> actionsListJSON = sharedpreferences.getStringSet(
                Constants.HT_PREFS_NOTIFICATION_ACTION_LIST, null);

        if (actionsListJSON != null && !actionsListJSON.isEmpty()) {
            List<ServiceNotificationAction> actionList = new ArrayList<>();
            Gson gson = new GsonBuilder().create();

            for (String actionJSON : actionsListJSON) {
                ServiceNotificationAction action = gson.fromJson(actionJSON, ServiceNotificationAction.class);
                if (action != null) {
                    actionList.add(action);
                }
            }

            return actionList;
        }

        return null;
    }

    private static RemoteViews getNotificationRemoteViews(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String remoteViewsJSON = sharedpreferences.getString(Constants.HT_PREFS_NOTIFICATION_REMOTE_VIEWS,
                null);

        if (TextUtils.isEmpty(remoteViewsJSON)) {
            return null;
        }

        Gson gson = new GsonBuilder().create();

        return gson.fromJson(remoteViewsJSON, RemoteViews.class);
    }
}
