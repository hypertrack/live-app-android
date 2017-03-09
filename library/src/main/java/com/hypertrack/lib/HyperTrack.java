package com.hypertrack.lib;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.callbacks.HyperTrackEventCallback;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.HTConsumerClient;

/**
 * Created by suhas on 04/02/16.
 */
public class HyperTrack {

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 0;
    public static final int REQUEST_CODE_LOCATION_SERVICES = 1;

    /**
     * Call this method to initialize HyperTrack SDKs with your Account's PublishableKey
     * in the onCreate method of either your Application file or your app's Launcher activity.
     * Refer to the documentation <a href="https://docs.hypertrack.io/guides/authentication.html#publishable-key">
     * here</a>
     * <p>
     * NOTE: In your Application's onCreate method, pass the application instance by using `this`
     * keyword or else in your Activity's onCreate method, pass the application instance by using
     * `getApplication()` method
     *
     * @param context        Pass your Application's context as the first parameter
     * @param publishableKey Pass your account's PublishableKey as the second parameter
     */
    public static void initialize(@NonNull Context context, @NonNull String publishableKey) {
        HyperTrackImpl.getInstance().initialize(context, publishableKey);
    }

    /**
     * Call this method to set a {@link HyperTrackEventCallback}. This callback allows you to receive events
     * and errors happening in the HyperTrackSDK as they occur.
     *
     * @param callback Instance of HyperTrackEventCallback object which will be invoked on an Event or an error.
     */
    public static void setCallback(@NonNull HyperTrackEventCallback callback) {
        HyperTrackImpl.getInstance().setCallback(callback);
    }

    /**
     * Call this method to create a User on HyperTrack API Server for the current device.
     * Refer to the documentation on creating a user.
     * todo Add the create user documentation link here
     *
     * @param callback Pass instance of HyperTrack callback as parameter
     */
    public static void createUser(@Nullable HyperTrackCallback callback) {
        HyperTrackImpl.getInstance().createUser(Build.MODEL, callback);
    }

    /**
     * Call this method to create a User on HyperTrack API Server for the current device.
     * Refer to the documentation on creating a user.
     * todo Add the create user documentation link here
     *
     * @param userName Pass User's name as the first parameter
     * @param callback Pass instance of HyperTrack callback as parameter
     */
    public static void createUser(String userName, @Nullable HyperTrackCallback callback) {
        if (TextUtils.isEmpty(userName))
            userName = Build.MODEL;

        HyperTrackImpl.getInstance().createUser(userName, callback);
    }

    /**
     * Call this method to check whether the user has granted location permission for the app.
     *
     * @return Returns true if the location permission has been granted or not required, false otherwise.
     */
    public static boolean checkLocationPermission(Context context) {
        return HyperTrackUtils.isLocationPermissionAvailable(context);
    }

    /**
     * Call this method to request Location permission to power HyperTrack SDK.
     *
     * @param activity Pass instance of the activity where permission needs to be requested.
     */
    public static void requestPermissions(@NonNull Activity activity) {
        HyperTrackImpl.getInstance().requestPermissions(activity);
    }

    /**
     * Call this method to check whether the Location Services are currently enabled or not.
     *
     * @return Returns true if the location services are enabled in HIGH_ACCURACY mode, false otherwise.
     */
    public static boolean checkLocationServices(Context context) {
        return HyperTrackUtils.isLocationEnabled(context);
    }

    /**
     * Call this method to request user to enable Location Services to power HyperTrack SDK.
     *
     * @param activity Pass instance of the AppCompatActivity where location services needs to be enabled.
     * @param callback Pass instance of HyperTrack callback as parameter
     */
    public static void requestLocationServices(@NonNull AppCompatActivity activity,
                                               HyperTrackCallback callback) {
        HyperTrackImpl.getInstance().requestLocationServices(activity, callback);
    }

    /**
     * Call this method to fetch user's current location.
     *
     * @param callback Pass instance of HyperTrack callback as parameter
     */
    public static void getCurrentLocation(@NonNull HyperTrackCallback callback) {
        HyperTrackImpl.getInstance().getCurrentLocation(callback);
    }

    /**
     * Method to startTracking the user.
     */
    public static void startTracking() {
        HyperTrackImpl.getInstance().startTracking(null);
    }

    /**
     * Method to startTracking the user.
     *
     * @param callback Pass instance of HyperTrack callback as parameter
     */
    public static void startTracking(@Nullable HyperTrackCallback callback) {
        HyperTrackImpl.getInstance().startTracking(callback);
    }

    /**
     * Method to mark an Action complete
     *
     * @param actionId Pass the action's unique id generated on HyperTrack API Server
     */
    public static void completeAction(String actionId) {
        HyperTrackImpl.getInstance().completeAction(actionId);
    }

    /**
     * Method to create and mark an Action complete
     */
    public static void completeAction() {
        HyperTrackImpl.getInstance().completeAction(null);
    }

    /**
     * Method to stopTracking the user.
     */
    public static void stopTracking() {
        HyperTrackImpl.getInstance().stopTracking(null);
    }

    /**
     * Method to stopTracking the user.
     *
     * @param callback Pass instance of HyperTrack callback as parameter
     */
    public static void stopTracking(@Nullable HyperTrackCallback callback) {
        HyperTrackImpl.getInstance().stopTracking(callback);
    }

    /**
     * Check if the user is active currently i.e. SDK is active for the configured user.
     *
     * @return Returns true if the SDK is active for the configured user
     */
    public static boolean isTracking() {
        return HyperTrackImpl.getInstance().isTracking();
    }

    /**
     * Used to enable or disable debug logging using Log Level.
     * Defaults to {@link Log#WARN} if not specified.
     * <p>
     * Specify one of the following Log Levels to customize the logging behaviour.
     * <ul>
     * <li>{@link Log#ASSERT}</li>
     * <li>{@link Log#VERBOSE}</li>
     * <li>{@link Log#DEBUG}</li>
     * <li>{@link Log#INFO}</li>
     * <li>{@link Log#WARN}</li>
     * <li>{@link Log#ERROR}</li>
     * </ul>
     *
     * @param logLevel Specify the log level to be enabled.
     */
    public static void enableDebugLogging(int logLevel) {
        HyperTrackImpl.getInstance().enableDebugLogging(logLevel);
    }

    /**
     * Call this method to get the PublishableKey configured in the SDK currently.
     *
     * @return Returns a string containing the PublishableKey if configured, null otherwise.
     */
    public static String getPublishableKey(Context context) {
        return HyperTrackImpl.getInstance().getPublishableKey(context);
    }

    /**
     * Method to get currently configured UserID
     *
     * @return Returns String containing the UserID if it is configured, null otherwise.
     */
    public static String getUserId() {
        return HyperTrackImpl.getInstance().getUserID();
    }

    /**
     * Call this method to set UserID in HyperTrack SDK for the current user.
     * Refer to the documentation on creating a user.
     *
     * @param userID Pass your user's unique id generated from HyperTrack Server as parameter.
     */
    public static void setUserId(@NonNull final String userID) {
        HyperTrackImpl.getInstance().setUserId(userID);
    }

    /**
     * Method to get current SDK VersionName
     *
     * @return Returns the VersionName for the SDK casted as a String.
     */
    public static String getSDKVersion() {
        return HyperTrackImpl.getInstance().getSDKVersion();
    }

    /**
     * Method to get configured OS Platform for the current SDK
     *
     * @return SDKPlatform as a String representing the configured OS Platform
     */
    public static String getSDKPlatform() {
        return HyperTrackImpl.getInstance().getSDKPlatform();
    }

    /**
     * Method to set OS Platform for the SDK or the wrapper (Ex: "ReactNative" for ReactNative wrapper)
     *
     * @param sdkPlatform String representing the current OS Platform
     */
    public static void setSDKPlatform(String sdkPlatform) {
        HyperTrackImpl.getInstance().setSDKPlatform(sdkPlatform);
    }

    /**
     * Method to establish User's connection to the server. Call this method when the
     * user's connection needs to established in the lifecyle of your application, to be able to
     * implement backend-start calls. (Preferably in the onResume() method of your app's Launcher Activity)
     * <p>
     * For more reliability in backend-start calls, call this method just before backend-start need to
     * happen in your app's workflow.
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * Call this method in your app's workflow after which SDK can be started via backend-start
     * with ApplicationContext as parameter.
     *
     * @param userID HyperTrack User Id for the current user
     * @return Returns FALSE in the case another user is already active, TRUE otherwise.
     */
    public static boolean connectUser(@NonNull final String userID) {
        return HyperTrackImpl.getInstance().connectUser(userID);
    }

    /**
     * Method to get HTConsumerClient instance
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * Before calling this method, initialize the HyperTrackSDK using HyperTrack.initialize(@NonNull Context context, @NonNull String publishableKey)
     *
     * @return instance of HTConsumerClient
     */

    public static HTConsumerClient getHTConsumerClient() {
        return HyperTrackImpl.getInstance().consumerClient;
    }
}
