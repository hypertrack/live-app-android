package io.hypertrack.sendeta.util;


/**
 * Created by suhas on 12/11/15.
 */
public class Constants {
    public static final String TAG = Constants.class.getSimpleName();

    public static final String SHARED_PREFERENCES_NAME = "io.hypertrack.meta";
    public static final String MEMBERSHIP_SHARED_PREFERENCES_NAME = "io.hypertrack.meta.membership";
    public static final String DEFAULT_STRING_VALUE = "none";

    // Constants to Round off ETATimes
    public static final int MINUTES_ON_ETA_MARKER_LIMIT = 199;
    public static final int MINUTES_IN_AN_HOUR = 60;
    public static final int MINUTES_TO_ROUND_OFF_TO_HOUR = 30;

    // REQUEST_CODEs
    public static final int REQUEST_CHECK_SETTINGS = 1;
    public static final int FAVORITE_PLACE_REQUEST_CODE = 100;
    public static final int EDIT_PROFILE_REQUEST_CODE = 101;
    public static final int BUSINESS_PROFILE_REQUEST_CODE = 102;
    public static final int SHARE_REQUEST_CODE = 200;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300;

    //Enums for Location Settings
    public static final double MAP_DEFAULT_LATITUDE_DELHI = 28.614092;
    public static final double MAP_DEFAULT_LONGITUDE_DELHI = 77.209053;

    // Notification Key Constants
    public static final String KEY_PUSH_DESTINATION = "push_destination";
    public static final String KEY_ACCOUNT_ID = "account_id";
    public static final String KEY_TASK = "task";
    public static final String KEY_PUSH_DESTINATION_LAT = "lat";
    public static final String KEY_PUSH_DESTINATION_LNG = "lng";

    public static final String KEY_PUSH_TASK = "push_task";
    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_ADDRESS = "address";
}
