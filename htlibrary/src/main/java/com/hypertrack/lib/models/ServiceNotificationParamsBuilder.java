package com.hypertrack.lib.models;

import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piyush on 11/08/16.
 */
public class ServiceNotificationParamsBuilder {

    private int smallIconResId = 0;
    private int smallIconBGColor = 0;
    private int largeIconResId = 0;
    private String contentTitle = null;
    private String contentText = null;
    private Class contentIntentActivityClass;
    private ArrayList<String> contentIntentExtras = null;
    private List<ServiceNotificationAction> actionsList = null;
    private RemoteViews contentView = null;

    /**
     * Method to set the Large Icon for the Notification.
     * <p>
     * You can also customise the icon by adding drawable resources in your application with the
     * same name as:
     * - For Large Notification Icon, add an icon with the name ic_ht_service_notification_large
     *
     * @param largeIconResId ResourceId of the Icon to be displayed as LargeIcon in the notification
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setLargeIcon(int largeIconResId) {
        this.largeIconResId = largeIconResId;
        return this;
    }

    /**
     * Method to set the Small Icon for the Notification.
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * This is a required field. If not specified, The Notification's Small Icon is
     * selected according to the app-icon specified in the android:icon tag in your
     * AndroidManifest.xml file. You can also customise the icon by adding drawable resources
     * in your application with the same name as follows:
     * - For Small Notification Icon, add an icon with the name "ic_ht_service_notification_small"
     *
     * @param smallIconResId ResourceId of the Icon to be displayed as SmallIcon in the notification
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setSmallIcon(int smallIconResId) {
        this.smallIconResId = smallIconResId;
        return this;
    }

    /**
     * Method to set the Small Icon Background Color for the Notification.
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * If set, the background color for smallIcon will be changed in the notification but
     * not in the StatusBar.
     *
     * @param smallIconBGColor The accent color to use as background for smallIcon in the notification
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setSmallIconBGColor(int smallIconBGColor) {
        this.smallIconBGColor = smallIconBGColor;
        return this;
    }

    /**
     * Method to set the Content Title of the Notification.
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * Set contentTitle as empty string to hide Notification Title. If contentTitle is set as null, the default
     * fallback contentTitle would be picked for the Notification.
     * <p>
     * By default, The notification contentTitle is "app-label is running" where the app-label is
     * specified as app:label in the application tag in your AndroidManifest.xml file. You can
     * customise the contentTitle by adding string resources in your application with the same name
     * as "ht_service_notification_title"
     *
     * @param title Text to be set as Notification contentTitle.
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setContentTitle(String title) {
        this.contentTitle = title;
        return this;
    }

    /**
     * Method to set the Content Text of the Notification.
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * Set contentText as empty string to hide Notification Text. If contentText is set as null, the default
     * fallback contentText would be picked for the Notification.
     * <p>
     * By default, The notification contentText is "Touch here to open the app". You can customise
     * the contentText by adding string resources in your application with the same name as
     * "ht_service_notification_text"
     *
     * @param text Text to be set as Notification contentText.
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setContextText(String text) {
        this.contentText = text;
        return this;
    }

    /**
     * Method to set the Content Intent Activity Class of the Notification.
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * If not set, the default intent action would be the launcher activity of the application.
     *
     * @param contentIntentActivityClass Class object of the Activity to be launched on NotificationClick.
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setContentIntentActivityClass(Class contentIntentActivityClass) {
        this.contentIntentActivityClass = contentIntentActivityClass;
        return this;
    }

    /**
     * Method to set extras for Notification's Content Intent (Intent set for Notification Click).
     * <p>
     *
     * @param contentIntentExtras ArrayList of String objects containing the extras to be set for Notification's Content Intent.
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setContentIntentExtras(ArrayList<String> contentIntentExtras) {
        this.contentIntentExtras = contentIntentExtras;
        return this;
    }

    /**
     * Method to add Action Buttons to the Notification.
     * <p>
     *
     * @param actionsList List of ServiceNotificationAction to be set as Notification actions.
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setActionsList(List<ServiceNotificationAction> actionsList) {
        this.actionsList = actionsList;
        return this;
    }

    /**
     * Method to set the Content View of the Notification. (Inflated View of a Custom Layout for the notification)
     * <p>
     *
     * @param contentView RemoteViews instance of the Notification's custom layout.
     * @return ServiceNotificationParamsBuilder object to customise the Service Notification
     */
    public ServiceNotificationParamsBuilder setContentView(RemoteViews contentView) {
        this.contentView = contentView;
        return this;
    }

    public ServiceNotificationParams build() {
        return new ServiceNotificationParams(contentTitle, contentText,
                contentIntentActivityClass != null ? contentIntentActivityClass.getName() : null,
                actionsList, contentView, smallIconResId, largeIconResId, smallIconBGColor, contentIntentExtras);
    }
}
