package com.hypertrack.lib;

/**
 * Created by piyush on 01/02/17.
 */
public class HyperTrackConstants {

    // UserID Key
    public static final String HT_USER_ID_KEY = "com.hypertrack:UserID";

    /**
     * User Connection Successful Intent
     */
    // User Connection successful intent
    public static final String HT_USER_CONNECTION_SUCCESSFUL_INTENT = "com.hypertrack.UserConnectionSuccessfulIntent";

    /**
     * User Tracking Intents
     */
    // User Tracking Started Intent
    public static final String HT_USER_TRACKING_STARTED_INTENT = "com.hypertrack:onUserTrackingStartedIntent";
    // User Tracking Stopped Intent
    public static final String HT_USER_TRACKING_STOPPED_INTENT = "com.hypertrack:onUserTrackingStoppedIntent";

    /**
     * User Stop Events
     */
    public static final String HT_USER_STOP_STARTED_INTENT = "com.hypertrack:onUserStopStartedIntent";
    public static final String HT_USER_STOP_ENDED_INTENT = "com.hypertrack:onUserStopEndedIntent";

    /**
     * User's CurrentLocation Intent
     */
    // User's Current Location Intent
    public static final String HT_USER_CURRENT_LOCATION_INTENT = "com.hypertrack.UserCurrentLocationIntent";
    // User's CurrentLocation Key
    public static final String HT_USER_CURRENT_LOCATION_KEY = "com.hypertrack.UserCurrentLocation";

    /**
     * Action Completed Intent
     */
    public static final String HT_ACTION_COMPLETED_INTENT = "com.hypertrack:onActionCompletedIntent";
    public static final String HT_ACTION_ID_KEY = "com.hypertrack:ActionID";
    /**
     * Service Notification Customization Constants
     */
    public static final String HT_SERVICE_NOTIFICATION_INTENT_TYPE = "com.hypertrack.service_notification.intent_type";
    public static final String HT_SERVICE_NOTIFICATION_INTENT_ACTION_TEXT = "com.hypertrack.service_notification.intent.action_text";
    public static final String HT_SERVICE_NOTIFICATION_INTENT_TYPE_NOTIFICATION_CLICK = "com.hypertrack.intent_type.notification";
    public static final String HT_SERVICE_NOTIFICATION_INTENT_TYPE_ACTION_CLICK = "com.hypertrack.intent_type.action";
    public static final String HT_SERVICE_NOTIFICATION_INTENT_EXTRAS_LIST = "com.hypertrack.service_notification.intent.extras_list";
}
