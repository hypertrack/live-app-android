package com.hypertrack.lib.internal.transmitter.utils;

import com.hypertrack.lib.BuildConfig;

/**
 * Created by piyush on 31/01/17.
 */
public class Constants {
    public static final String HT_SHARED_PREFS_KEY = "com.hypertrack:SharedPreference";

    public static final String HT_PREFS_NOTIFICATION_INTENT_CLASS_NAME = "com.hypertrack:Notification.IntentClassName";
    public static final String HT_PREFS_NOTIFICATION_INTENT_EXTRAS = "com.hypertrack:Notification.IntentExtras";
    public static final String HT_PREFS_NOTIFICATION_LARGE_ICON_RES_ID = "com.hypertrack:Notification.LargeIconResId";
    public static final String HT_PREFS_NOTIFICATION_SMALL_ICON_RES_ID = "com.hypertrack:Notification.SmallIconResId";
    public static final String HT_PREFS_NOTIFICATION_SMALL_ICON_BG_COLOR = "com.hypertrack:Notification.SmallIconBGColor";
    public static final String HT_PREFS_NOTIFICATION_TITLE = "com.hypertrack:Notification.Title";
    public static final String HT_PREFS_NOTIFICATION_TEXT = "com.hypertrack:Notification.Text";
    public static final String HT_PREFS_NOTIFICATION_REMOTE_VIEWS = "com.hypertrack:Notification.RemoteViews";
    public static final String HT_PREFS_NOTIFICATION_ACTION_LIST = "com.hypertrack:Notification.ActionList";

    public static final String DEVICE_HEALTH_BASE_TOPIC = "DeviceHealth/";

    public static final String GPS_BULK_URL = "gps/bulk/";
    public static final String GPS_LOG_BASE_TOPIC = "GPSLog/";

    public static final String SDK_CONTROLS_BASE_TOPIC = BuildConfig.MQTT_BASE_TOPIC + "Push/SDKControls/";

    public static final String HT_SDK_CONTROLS_KEY = "com.hypertrack:SDKControls";
    public static final String HT_ACTIVE_TRACKING_MODE_KEY = "com.hypertrack:ActiveTrackingMode";
    public static final String HT_USER_STOP_KEY = "com.hypertrack:UserGoefence";

    public static final String INTENT_ACTION_STOP_TIMEOUT_ALARM = "com.hypertrack:StopTimeoutAlarm";
    public static final String INTENT_ACTION_SDK_CONTROLS_TTL_ALARM = "com.hypertrack:SDKControlsTTLAlarm";
    public static final String INTENT_ACTION_STOP_GEOFENCE_TRANSITION = "com.hypertrack:StopGeofenceTransition";

    public static final String STOP_STARTED_EVENT_INTENT_ACTION = "com.hypertrack:StopStarted";
    public static final String STOP_ENDED_EVENT_INTENT_ACTION = "com.hypertrack:StopEnded";

    public static final int REQUEST_CODE_ACTIVITY_RECOGNITION = 0;
    public static final int REQUEST_CODE_GEOFENCE_TRANSITION = 1;
    public static final int REQUEST_CODE_STOP_GEOFENCE_ALARM = 2;
    public static final int REQUEST_CODE_SDK_CONTROLS_TTL_ALARM = 3;

    public static final long STOP_STARTED_GEOFENCE_TIMEOUT = 60000;

    public static final int POST_DATA_JOB = 21;
    public static final String POST_DATA_TAG = "com.hypertrack:PostData";

    public static final String USER_ACTIVITY_UNKNOWN = "unknown";
    public static final String USER_ACTIVITY_STATIONARY = "stationary";
    public static final String USER_ACTIVITY_WALKING = "walking";
    public static final String USER_ACTIVITY_RUNNING = "running";
    public static final String USER_ACTIVITY_CYCLING = "cycling";
    public static final String USER_ACTIVITY_AUTOMOTIVE = "automotive";
}
