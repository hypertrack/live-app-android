package com.hypertrack.lib.internal.transmitter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackAlarmReceiver;
import com.hypertrack.lib.HyperTrackService;
import com.hypertrack.lib.HyperTrackServiceManager;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.R;
import com.hypertrack.lib.RegistrationService;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.callbacks.HyperTrackEventCallback;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.logging.DeviceLogsManager;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.network.HTNetworkResponse;
import com.hypertrack.lib.internal.common.network.HyperTrackGetRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest.HTNetworkClient;
import com.hypertrack.lib.internal.common.network.HyperTrackPostRequest;
import com.hypertrack.lib.internal.common.network.MQTTMessageArrivedCallback;
import com.hypertrack.lib.internal.common.network.MQTTSubscriptionSuccessCallback;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.common.util.Utils;
import com.hypertrack.lib.internal.common.util.ValidationUtil;
import com.hypertrack.lib.internal.transmitter.controls.SDKControls;
import com.hypertrack.lib.internal.transmitter.controls.SDKControlsManager;
import com.hypertrack.lib.internal.transmitter.controls.UpdateControlsCallback;
import com.hypertrack.lib.internal.transmitter.devicehealth.DeviceHealth;
import com.hypertrack.lib.internal.transmitter.events.EventsManager;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;
import com.hypertrack.lib.internal.transmitter.utils.BroadcastManager;
import com.hypertrack.lib.internal.transmitter.utils.Constants;
import com.hypertrack.lib.internal.transmitter.utils.HTServiceNotificationUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.ServiceNotificationParams;
import com.hypertrack.lib.models.SuccessResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import io.hypertrack.smart_scheduler.Job;
import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * Created by piyush on 31/01/17.
 */
public class TransmitterClient implements SmartScheduler.JobScheduledCallback, MQTTMessageArrivedCallback,
        MQTTSubscriptionSuccessCallback {

    private static final String TAG = TransmitterClient.class.getSimpleName();
    public Context mContext;

    private SmartScheduler jobScheduler;
    private UserPreferences userPreferences;
    private DeviceLogsManager logsManager;
    private NetworkManager networkManager;
    private EventsManager eventsManager;
    private HyperTrackServiceManager serviceManager;
    private BroadcastManager broadcastManager;
    private SDKControlsManager sdkControlsManager;
    private HyperTrackEventCallback eventCallback;

    public TransmitterClient(Context context, UserPreferences userPreferences,
                             SmartScheduler scheduler, BroadcastManager broadcastManager,
                             SDKControlsManager sdkControlsManager, DeviceLogsManager logsManager,
                             NetworkManager networkManager, EventsManager eventsManager) {
        this.mContext = context;
        this.userPreferences = userPreferences;
        this.jobScheduler = scheduler;
        this.broadcastManager = broadcastManager;
        this.sdkControlsManager = sdkControlsManager;
        this.logsManager = logsManager;
        this.networkManager = networkManager;
        this.eventsManager = eventsManager;
        this.serviceManager = new HyperTrackServiceManager(context, scheduler, userPreferences,
                eventsManager);

        // Set Application Callbacks
        this.setApplicationLifecycleCallbacks(context);
    }

    public void initTransmitter() {
        // Initialize TransmitterClient instance
        if (isTracking() && HyperTrackService.hyperTrackService == null) {
            this.startTracking(true, null);

        } else {
            // Post DeviceLogs To Server
            if (HyperTrackUtils.isInternetConnected(mContext) && logsManager.hasPendingDeviceLogs()) {
                logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);
            }

            // Check for any pending data for Current user
            if (!TextUtils.isEmpty(getUserID()) && hasPendingData()) {
                initPostDataJob();
            }
        }
    }

    public HyperTrackServiceManager getServiceManager() {
        return serviceManager;
    }

    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

    public void setEventCallback(HyperTrackEventCallback eventCallback) {
        this.eventCallback = eventCallback;
    }

    public HyperTrackEventCallback getEventCallback() {
        return eventCallback;
    }

    public boolean setUserID(@NonNull String userID) {
        if (userID.isEmpty())
            return false;

        // Check if this UserID is already configured or not
        if (!userID.equals(userPreferences.getUserId())) {
            HTLog.i(TAG, "setUserID called for UserID: " + userID);
            userPreferences.setUserID(userID);
        }

        networkManager.setUserID(userID);

        // Register GcmToken if Gcm is set-up
        registerGcmToken();
        return true;
    }

    public void getCurrentLocation(HyperTrackCallback callback) {
        serviceManager.startLocationUpdateOnce(callback);
    }

    public boolean connectUser(@NonNull final String userID) {
        if (userID.isEmpty())
            return false;

        // Set UserID if not configured already
        setUserID(userID);

        // Check if user isTracking right now
        if (!isTracking()) {
            // TODO: 22/02/17 Add implementation to disconnectUser here
            return false;
        }

        // Check if User is already connected
        if (isUserConnected(userID)) {
            userPreferences.setUserID(userID);
            HTLog.i(TAG, "UserID: " + userID + " connected");

            broadcastManager.userConnSuccessfulBroadcast(userID);
            return true;
        }

        // Initialize connection for the given userID
        // TODO: 19/02/17 Add implementation for connectUser here
        return true;
    }

    private void registerGcmToken() {
        // Check if FCM has been integrated in the app
        String firebaseDatabaseUrl = mContext.getResources().getString(R.string.firebase_database_url);
        if (!firebaseDatabaseUrl.isEmpty()) {

            // Check if FCM Token is available, provided FCM has been integrated in the app.
            String fcmToken = FirebaseInstanceId.getInstance().getToken();
            if (!TextUtils.isEmpty(fcmToken)) {
                userPreferences.setFcmToken(fcmToken);
                postRegistrationToken();
            }
            return;
        }

        // Check if GCM has been integrated in the app
        String defaultSenderId = mContext.getResources().getString(R.string.gcm_defaultSenderId);
        if (!defaultSenderId.isEmpty()) {

            // Check if GCM Token is already available or not
            String gcmToken = userPreferences.getGcmToken();
            if (!TextUtils.isEmpty(gcmToken)) {
                postRegistrationToken();
                return;
            }

            // Register for Gcm Token
            HTLog.i(TAG, "Registering Gcm Token.");
            Intent intent = new Intent(mContext, RegistrationService.class);
            mContext.startService(intent);
        }
    }

    public boolean isTracking() {
        return userPreferences.isTracking();
    }

    public boolean isUserConnected() {
        return isUserConnected(getUserID());
    }

    private boolean isUserConnected(String userID) {
        return userPreferences.getUserId() != null && userPreferences.getUserId().equalsIgnoreCase(userID)
                && networkManager.getMqttClient().isTopicSubscribed(Constants.SDK_CONTROLS_BASE_TOPIC + userID);
    }

    public String getUserID() {
        return userPreferences.getUserId();
    }

    public boolean setServiceNotificationParams(ServiceNotificationParams notificationParams) {
        if (notificationParams == null) {
            return false;
        }

        // Set Service Notification Params to SharedPreferences
        setNotificationBuilder(notificationParams);

        // Update Service Notification in case Service is already running
        if (HyperTrackService.hyperTrackService!= null) {
            HyperTrackService.hyperTrackService.startForeground();
            return true;
        }

        return false;
    }

    public void clearServiceNotificationParams() {
        SharedPreferences sharedpreferences = mContext.getSharedPreferences(
                Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(Constants.HT_PREFS_NOTIFICATION_LARGE_ICON_RES_ID);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_SMALL_ICON_RES_ID);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_SMALL_ICON_BG_COLOR);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_TITLE);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_TEXT);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_INTENT_CLASS_NAME);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_INTENT_EXTRAS);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_REMOTE_VIEWS);
        editor.remove(Constants.HT_PREFS_NOTIFICATION_ACTION_LIST);
        editor.apply();
    }

    private void setNotificationBuilder(final ServiceNotificationParams params) {
        try {
            Gson gson = new GsonBuilder().create();
            String remoteViews = gson.toJson(params.getContentView());

            SharedPreferences sharedpreferences = mContext.getSharedPreferences(
                    Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(Constants.HT_PREFS_NOTIFICATION_LARGE_ICON_RES_ID, params.getLargeIconResId());
            editor.putInt(Constants.HT_PREFS_NOTIFICATION_SMALL_ICON_RES_ID, params.getSmallIconResId());
            editor.putInt(Constants.HT_PREFS_NOTIFICATION_SMALL_ICON_BG_COLOR, params.getSmallIconBGColor());
            editor.putString(Constants.HT_PREFS_NOTIFICATION_TITLE, params.getContentTitle());
            editor.putString(Constants.HT_PREFS_NOTIFICATION_TEXT, params.getContentText());
            editor.putString(Constants.HT_PREFS_NOTIFICATION_INTENT_CLASS_NAME,
                    params.getContentIntentActivityClassName());
            editor.putStringSet(Constants.HT_PREFS_NOTIFICATION_INTENT_EXTRAS,
                    HTServiceNotificationUtils.getNotificationIntentExtras(params.getContentIntentExtras()));
            editor.putString(Constants.HT_PREFS_NOTIFICATION_REMOTE_VIEWS, remoteViews);
            editor.putStringSet(Constants.HT_PREFS_NOTIFICATION_ACTION_LIST,
                    HTServiceNotificationUtils.getNotificationActionListJSON(gson, params.getActionsList()));
            editor.apply();
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while setNotificationBuilder: " + e.getMessage());
        }
    }

    public void startTracking(boolean startService, HyperTrackCallback callback) {
        try {
            // Check for StartTracking validation errors
            ErrorResponse validationError = ValidationUtil.getStartTrackingValidationError(mContext);
            if (validationError != null) {
                if (callback != null) {
                    callback.onError(validationError);
                }
                if (eventCallback != null) {
                    eventCallback.onError(validationError);
                }
                return;
            }

            // Re-enable Geofence on Location Settings change
            HyperTrackStop hyperTrackStop = userPreferences.getUserStop();
            if (hyperTrackStop != null) {
                serviceManager.addUserStop(hyperTrackStop);
            }

            // Connect User, in case not connected yet
            connectUser(userPreferences.getUserId());

            HTLog.i(TAG, "User Tracking started for User ID: " + userPreferences.getUserId());

            // Set isTracking to TRUE
            userPreferences.setIsTracking(true);

            // Initiate LocationService, if it needs to be started
            if (startService) {
                startHyperTrackService();
            }

            // Initiate Periodic Jobs, if they need to be started
            initPostDataJob();

            // Collect DeviceHealth event
            eventsManager.logHealthChangedEvent(DeviceHealth.getDeviceHealth(mContext));

            if (callback != null)
                callback.onSuccess(new SuccessResponse(getUserID()));

        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while startTracking: " + e);
            if (callback != null) {
                callback.onError(new ErrorResponse());
            }
        }
    }

    private void initPostDataJob() {
        SDKControls controls = sdkControlsManager.getSDKControls();
        long repeatDuration = controls.getBatchDuration();

        // Create a Periodic HTJob object with BatchDuration * Multiplier as the interval
        Job postDataPeriodicTaskJob = new Job.Builder(Constants.POST_DATA_JOB, this, Constants.POST_DATA_TAG)
                .setPeriodic(repeatDuration * 1000)
                .setRequiredNetworkType(Job.NetworkType.NETWORK_TYPE_CONNECTED)
                .build();

        // Schedule Periodic Job, if not scheduled already
        if (!jobScheduler.contains(postDataPeriodicTaskJob))
            jobScheduler.addJob(postDataPeriodicTaskJob);

        // Set PostDataPeriodicTask LastUpdatedTime
        userPreferences.setLastPostToServerTime();

        // Remove BatchDuration TTL Alarm, if any exists
        removeBatchDurationTTLAlarm();

        // Set SDKControls TTL timeout for current batch_duration values
        if (controls.getTtl() != null && controls.getTtl() != 0) {
            setBatchDurationTTLAlarm(controls.getTtl());
        }
    }

    private void startHyperTrackService() {
        // Fetch updated SDKControls
        SDKControls sdkControls = sdkControlsManager.getSDKControls();

        // Start LocationService with SDKControls as parameter
        Intent hypertrackServiceIntent = new Intent(mContext, HyperTrackService.class);
        hypertrackServiceIntent.putExtra(Constants.HT_SDK_CONTROLS_KEY, sdkControls);
        hypertrackServiceIntent.putExtra(Constants.HT_ACTIVE_TRACKING_MODE_KEY,
                isActiveTrackingEnabled(sdkControls));
        mContext.startService(hypertrackServiceIntent);
    }

    private boolean isActiveTrackingEnabled(SDKControls sdkControls) {
        // TODO: 22/02/17 Add logic for User's ActiveTracking mode here
        return sdkControls != null && sdkControls.getMinimumDuration() <= 30;

    }

    public void completeAction(String actionId) {
        eventsManager.logActionCompletedEvent(actionId, userPreferences.getLastRecordedLocation());
    }

    @Override
    public void onJobScheduled(Context context, Job job) {
        if (job == null)
            return;
        switch (job.getJobId()) {
            case Constants.POST_DATA_JOB:
                postCachedDataToServer();
                break;
            default:
                break;
        }
    }

    private void postCachedDataToServer() {
        Log.i(TAG, "PostData job scheduled");
        if (!isTracking() && !hasPendingData()) {
            jobScheduler.removeJob(Constants.POST_DATA_JOB);
            networkManager.disconnect();
            return;
        }

        eventsManager.postEvents(null, null);
        logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);
    }

    public void flushCachedDataToServer() {
        if (hasPendingData()) {
            eventsManager.flushEvents();
        }
        logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);
    }

    private boolean hasPendingData() {
        return eventsManager.hasPendingEvents();
    }

    public void stopTracking(HyperTrackCallback callback) {
        try {
            // Check for StartTracking validation errors
            ErrorResponse validationError = ValidationUtil.getValidationError(mContext);
            if (validationError != null) {
                if (callback != null) {
                    callback.onError(validationError);
                }
                return;
            }

            String userId = userPreferences.getUserId();
            HTLog.i(TAG, "User's Tracking stopped for User ID: " + (userId != null ? userId : "null"));

            if (HyperTrackService.hyperTrackService != null) {
                HyperTrackService.hyperTrackService.stopSelfService();
            }

            // Clear userPreferences indicating user state
            userPreferences.clearUserData();
            serviceManager.removeUserStop();
            serviceManager.clearPendingCommands();
            sdkControlsManager.clearControls();

            // Collect DeviceHealth event
            eventsManager.logHealthChangedEvent(DeviceHealth.getDeviceHealth(mContext));

            // Clear Saved DeviceHealth data
            DeviceHealth.clearSavedDeviceHealthParams(mContext);

            if (callback != null)
                callback.onSuccess(new SuccessResponse(getUserID()));
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while stopTracking: " + e);
            if (callback != null)
                callback.onError(new ErrorResponse());
        }
    }

    /**
     * Fetch the new SDK Controls from Server and update the SDK Controls of user.
     */
    public void updateSDKControls(final Context mContext, final String userID) {
        String url = BuildConfig.CORE_API_BASE_URL + "users/" + userID + "/controls/";
        HyperTrackGetRequest<SDKControls> getNetworkRequests = new HyperTrackGetRequest<>(TAG, mContext,
                url, HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_HTTP, SDKControls.class,
                new HTNetworkResponse.Listener<SDKControls>() {
                    @Override
                    public void onResponse(final SDKControls updatedSDKControls) {
                        Log.d(TAG, "updated sdk controls: " + updatedSDKControls);
                        processSDKControlsUpdate(updatedSDKControls);
                    }
                },
                new HTNetworkResponse.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error, Exception exception) {
                        HTLog.e(TAG, "Error occurred while updateSDKControls: " + exception);
                    }
                });
        networkManager.execute(mContext, getNetworkRequests);
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        try {
            if (message == null || message.isEmpty()) {
                HTLog.e(TAG, "Error occurred while TransmissionManager.onMessageArrived: message is null");
                return;
            }

            String sdkControlsTopic = Constants.SDK_CONTROLS_BASE_TOPIC + userPreferences.getUserId();

            // Handle SDKControls updated MQTT message
            if (topic != null && topic.equalsIgnoreCase(sdkControlsTopic)) {
                Gson gson = new Gson();
                SDKControls updatedSDKControls = gson.fromJson(message, SDKControls.class);

                processSDKControlsUpdate(updatedSDKControls);
            }

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while onSDKControlsUpdated: " + e);
        }
    }

    private void processSDKControlsUpdate(final SDKControls updatedSDKControls) {
        // Set updated SDKControls to persistence
        sdkControlsManager.setSDKControls(updatedSDKControls, new UpdateControlsCallback() {
            @Override
            public void onGoOnlineCommand(boolean resetSchedulingControls, boolean resetCollectionControls) {
                HTLog.i(TAG, "onSDKControlsUpdated called with resetCollectionControls: " + resetCollectionControls
                        + ", resetSchedulingControls: " + resetSchedulingControls);

                // Re-initiate UserActivity
                if (!HyperTrack.isTracking()) {
                    // FOR BACKEND_START: Start user tracking with logging a tracking.started event
                    HyperTrack.startTracking();

                } else {
                    // Start user tracking without logging a tracking.started event
                    boolean startService = (HyperTrackService.hyperTrackService == null) || resetCollectionControls;
                    startTracking(startService, null);
                }
            }

            @Override
            public void onGoActiveCommand(boolean resetSchedulingControls, boolean resetCollectionControls) {
                HTLog.i(TAG, "onSDKControlsUpdated called with resetCollectionControls: " + resetCollectionControls
                        + ", resetSchedulingControls: " + resetSchedulingControls);

                // Re-initiate UserActivity
                boolean startService = (HyperTrackService.hyperTrackService == null) || resetCollectionControls;
                startTracking(startService, null);
            }

            @Override
            public void onFlushDataCommand() {
                // FLUSH command was sent from server, Flushing User's data
                flushCachedDataToServer();
            }

            @Override
            public void onGoOfflineCommand() {
                // FOR BACKEND_START: Stop user tracking with logging a tracking.ended event
                if (HyperTrack.isTracking())
                    HyperTrack.stopTracking();
            }
        });
    }

    public void onBatchDurationTTLExpired() {
        // Fetch BatchDuration TTL expired SDKControls & process these updates
        SDKControls updatedControls = sdkControlsManager.getTTLExpiredSDKControls();
        processSDKControlsUpdate(updatedControls);
    }

    private void setBatchDurationTTLAlarm(int ttl) {
        try {
            Intent intent = new Intent(mContext, HyperTrackAlarmReceiver.class);
            intent.setAction(Constants.INTENT_ACTION_SDK_CONTROLS_TTL_ALARM);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                    Constants.REQUEST_CODE_SDK_CONTROLS_TTL_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();

            AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            long triggerInMillis = calendar.getTimeInMillis() + (ttl * 1000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerInMillis, pendingIntent);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarm.setExact(AlarmManager.RTC_WAKEUP, triggerInMillis, pendingIntent);

            } else {
                alarm.set(AlarmManager.RTC_WAKEUP, triggerInMillis, pendingIntent);
            }

            HTLog.i(TAG, "SDKControls TTL timeout set for " + ttl + " seconds.");
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while setBatchDurationTTLAlarm: " + e);
        }
    }

    private void removeBatchDurationTTLAlarm() {
        try {
            Intent intent = new Intent(mContext, HyperTrackAlarmReceiver.class);
            intent.setAction(Constants.INTENT_ACTION_SDK_CONTROLS_TTL_ALARM);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                    Constants.REQUEST_CODE_SDK_CONTROLS_TTL_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Removes the stop started timeout
            AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pendingIntent);
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while removeBatchDurationTTLAlarm: " + e);
        }
    }

    @Override
    public void onMQTTSubscriptionSuccess(String topic) {
        // TODO: 18/02/17 Add implementation for postUserConnectionSuccessful event
    }

    public void postRegistrationToken() {
        try {
            if (!HyperTrackUtils.isInternetConnected(mContext)) {
                return;
            }

            // Check if RegistrationToken and UserID are available or not
            String gcmToken = userPreferences.getGcmToken();
            String fcmToken = userPreferences.getFcmToken();
            final String userId = userPreferences.getUserId();

            if ((TextUtils.isEmpty(gcmToken) && TextUtils.isEmpty(fcmToken)) || TextUtils.isEmpty(userId)) {
                HTLog.e(TAG, "Error while postRegistrationToken: FcmToken/GcmToken or UserID is NULL.");
                return;
            }

            // Check if FCM has been integrated in the app or not
            String firebaseDatabaseUrl = mContext.getResources().getString(R.string.firebase_database_url);
            boolean isFcmEnabled = !TextUtils.isEmpty(firebaseDatabaseUrl) && !TextUtils.isEmpty(fcmToken);

            // Construct a request object with the required paramters
            final JSONObject requestBody = new JSONObject();
            requestBody.put("user_id", userId);
            requestBody.put("device_id", Utils.getDeviceId(mContext));

            if (isFcmEnabled) {
                requestBody.put("cloud_message_type", "FCM");
                requestBody.put("registration_id", fcmToken);

                if (userPreferences.isFcmTokenPushed(requestBody)) {
                    return;
                }
            } else {
                requestBody.put("cloud_message_type", "GCM");
                requestBody.put("registration_id", gcmToken);

                if (userPreferences.isGcmTokenPushed(requestBody)) {
                    return;
                }
            }

            HTLog.i(TAG, "Pushing Registration Token to server: " + requestBody.toString());

            String url = BuildConfig.CORE_API_BASE_URL + HTConstants.POST_REGISTRATION_TOKEN_URL;
            HyperTrackPostRequest<JSONObject> postNetworkRequest = new HyperTrackPostRequest<>(
                    HTConstants.POST_REGISTRATION_TOKEN_TAG, mContext, url,
                    HTNetworkClient.HT_NETWORK_CLIENT_HTTP, requestBody, JSONObject.class,
                    new HTNetworkResponse.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            HTLog.i(TAG, "Registration Token pushed to server successfully");
                            try {
                                if (requestBody.getString("cloud_message_type").equalsIgnoreCase("FCM")) {
                                    userPreferences.setFcmTokenPushed(requestBody);
                                } else {
                                    userPreferences.setGcmTokenPushed(requestBody);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new HTNetworkResponse.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error, Exception exception) {
                            // do nothing
                        }
                    });

            networkManager.cancel(TAG);
            networkManager.execute(mContext, postNetworkRequest);

        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while postRegistrationToken: " + e);
        }
    }

    private void setApplicationLifecycleCallbacks(Context context) {
        try {
            Application application = (Application) context;
            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    // do nothing
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    // do nothing
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    // do nothing
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    // do nothing
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    // do nothing
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    // do nothing
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    // do nothing
                }
            });

            application.registerComponentCallbacks(new ComponentCallbacks2() {
                @Override
                public void onTrimMemory(int level) {
                    // do nothing
                    HTLog.w(TAG, "onTrimMemory application lifecycle callback called.");
                }

                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    // do nothing
                }

                @Override
                public void onLowMemory() {
                    // do nothing
                    HTLog.w(TAG, "onLowMemory application lifecycle callback called.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while setApplicationLifecycleCallbacks");
        }
    }
}