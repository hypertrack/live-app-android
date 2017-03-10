package com.hypertrack.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.callbacks.HyperTrackEventCallback;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.logging.DeviceLogDatabaseHelper;
import com.hypertrack.lib.internal.common.logging.DeviceLogsManager;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.network.HTNetworkResponse;
import com.hypertrack.lib.internal.common.network.HyperTrackGetRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest.HTNetworkClient;
import com.hypertrack.lib.internal.common.network.HyperTrackPostRequest;
import com.hypertrack.lib.internal.common.network.NetworkErrorUtil;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.network.NetworkManagerImpl;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.common.util.UserPreferencesImpl;
import com.hypertrack.lib.internal.common.util.ValidationUtil;
import com.hypertrack.lib.internal.consumer.ConsumerClient;
import com.hypertrack.lib.internal.transmitter.TransmitterClient;
import com.hypertrack.lib.internal.transmitter.controls.SDKControlsManager;
import com.hypertrack.lib.internal.transmitter.devicehealth.DeviceHealth;
import com.hypertrack.lib.internal.transmitter.events.EventsDatabaseHelper;
import com.hypertrack.lib.internal.transmitter.events.EventsManager;
import com.hypertrack.lib.internal.transmitter.utils.BroadcastManager;
import com.hypertrack.lib.internal.transmitter.utils.BroadcastManagerImpl;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.Error;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.ServiceNotificationParams;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;

import org.json.JSONObject;

import java.util.List;

import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * Created by piyush on 17/02/17.
 */

/**
 * package
 */
class HyperTrackImpl {

    private static final String TAG = HyperTrackImpl.class.getSimpleName();
    private static final int LOCATION_UPDATE_INTERVAL_TIME = 5000;
    private static final int LOCATION_UPDATE_FASTEST_INTERVAL_TIME = 1000;
    private static HyperTrackImpl sInstance;
    private Context mContext;
    TransmitterClient transmitterClient;
    private ConsumerClient consumerClient;
    private HyperTrackEventCallback eventCallback;
    UserPreferences userPreferences;
    DeviceLogsManager logsManager;
    EventsManager eventsManager;
    private SmartScheduler scheduler;
    private NetworkManager networkManager;
    private BroadcastManager broadcastManager;

    private HyperTrackImpl() {
    }

    static HyperTrackImpl getInstance() {
        if (sInstance == null) {
            synchronized (HyperTrackImpl.class) {
                if (sInstance == null) {
                    sInstance = new HyperTrackImpl();
                }
            }
        }

        return sInstance;
    }

    void setCallback(HyperTrackEventCallback callback) {
        this.eventCallback = callback;
        if (this.transmitterClient != null)
            transmitterClient.setEventCallback(eventCallback);

        if (this.eventsManager != null)
            eventsManager.setEventCallback(eventCallback);
    }

    void initialize(@NonNull Context context, String publishableKey) {
        // Set HyperTrack PublishableKey
        if (publishableKey != null) {
            setPublishableKey(context, publishableKey);
        }

        mContext = context.getApplicationContext();

        // HyperTrack Initializations
        HTLog.initHTLog(mContext , DeviceLogDatabaseHelper.getInstance(mContext));

        if (scheduler == null)
            scheduler = SmartScheduler.getInstance(mContext);

        if (userPreferences == null)
            userPreferences = UserPreferencesImpl.getInstance(mContext);

        if (networkManager == null)
            networkManager = NetworkManagerImpl.getInstance(mContext, userPreferences.getUserId(),
                    scheduler);

        if (logsManager == null)
            logsManager = DeviceLogsManager.getInstance(mContext,
                    DeviceLogDatabaseHelper.getInstance(mContext), networkManager);

        if (broadcastManager == null)
            broadcastManager = new BroadcastManagerImpl(mContext);

        if (eventsManager == null)
            eventsManager = EventsManager.getInstance(mContext, EventsDatabaseHelper.getInstance(mContext),
                    userPreferences, networkManager, logsManager, broadcastManager, eventCallback);

        // Initialize TransmitterClient
        initTransmitterClient(mContext);

        // Initialize ConsumerClient
        initConsumerClient(mContext);
    }

    String getPublishableKey(Context context) {
        if (context == null) {
            return null;
        }

        return context.getSharedPreferences(HTConstants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
                .getString(HTConstants.PUBLISHABLE_KEY_PREFS_KEY, null);
    }

    void setUserId(@NonNull final String userID) {
        if (!checkIfSDKInitialized(null))
            return;

        transmitterClient.setUserID(userID);
    }

    void createUser(@NonNull String userName, final HyperTrackCallback callback) {
        if (!checkIfSDKInitialized(callback))
            return;

        try {
            // Check for CreateUser validation errors
            ErrorResponse validationError = ValidationUtil.getNetworkCallValidationError(mContext);
            if (validationError != null) {
                callback.onError(validationError);
                if (eventCallback != null)
                    eventCallback.onError(validationError);
                return;
            }

            String url = BuildConfig.CORE_API_BASE_URL + HTConstants.CREATE_USER_URL;
            JSONObject requestBody = new JSONObject();
            requestBody.put("name", userName);

            HyperTrackPostRequest<User> postNetworkRequest = new HyperTrackPostRequest<>(
                    HTConstants.CREATE_USER_TAG, mContext, url, HTNetworkClient.HT_NETWORK_CLIENT_HTTP,
                    requestBody, User.class,
                    new HTNetworkResponse.Listener<User>(){
                        @Override
                        public void onResponse(User user) {
                            // Set UserId with the created userId
                            HyperTrack.setUserId(user.getId());

                            if (callback != null) {
                                callback.onSuccess(new SuccessResponse(user));
                            }
                        }
                    },
                    new HTNetworkResponse.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error, Exception exception) {
                            ErrorResponse errorResponse = new ErrorResponse(error);
                            HTLog.e(TAG, "Error occurred while createUser: " + errorResponse.getErrorMessage());
                            if (callback != null) {
                                callback.onError(errorResponse);
                            }

                            // Check for Invalid Token errors
                            if (eventCallback != null && NetworkErrorUtil.isInvalidTokenError(error)) {
                                eventCallback.onError(errorResponse);
                            }
                        }
                    });
            networkManager.execute(mContext, postNetworkRequest);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while createUser: " + e);
            if (callback != null) {
                callback.onError(new ErrorResponse());
            }
        }
    }

    void requestPermissions(@NonNull final Activity activity) {
        // Check if Location Permission is available or not
        if (HyperTrackUtils.isLocationPermissionAvailable(activity))
            return;

        // Show Rationale & Request for LOCATION permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(activity.getString(R.string.location_permission_rationale_msg));
            builder.setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Request for LOCATION permission
                    ActivityCompat.requestPermissions(activity,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            HyperTrack.REQUEST_CODE_LOCATION_PERMISSION);
                }
            });
            builder.show();

        } else {
            // Request for LOCATION permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    HyperTrack.REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

    void requestLocationServices(@NonNull final AppCompatActivity activity, final HyperTrackCallback callback) {
        if (HyperTrackUtils.isLocationEnabled(activity)) {
            if (callback != null) {
                callback.onSuccess(new SuccessResponse(null));
            }
            return;
        }

        // Build GoogleAPIClient object
        final GoogleApiClient apiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .build();

        // Build LocationRequest object
        final LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL_TIME)
                .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL_TIME);

        // Register for GoogleAPIClient ConnectionCallbacks
        apiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                // GoogleAPIClient connection successful, Request for LocationServices Resolution
                onGoogleAPIClientConnection(activity, apiClient, request, callback);
            }

            @Override
            public void onConnectionSuspended(int i) {
                String cause = "";
                // If your connection to the sensor gets lost at some point,
                // you'll be able to determine the reason and react to it here.
                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    cause = " Cause: Network Lost";
                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    cause = " Cause: Service Disconnected";
                }

                HTLog.e(TAG, Error.Message.GOOGLE_API_CLIENT_CONN_SUSPENDED + cause);
                if (callback != null) {
                    callback.onError(new ErrorResponse(Error.Code.GOOGLE_API_CLIENT_CONN_SUSPENDED,
                            Error.Message.GOOGLE_API_CLIENT_CONN_SUSPENDED + (cause.isEmpty() ? "" : cause)));
                }
            }
        });

        // Register for GoogleAPIClient ConnectionFailedListener
        apiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                HTLog.i(TAG, Error.Message.GOOGLE_API_CLIENT_CONN_FAILED + connectionResult.toString());

                if (callback != null)
                    callback.onError(new ErrorResponse(Error.Code.GOOGLE_API_CLIENT_CONN_FAILED,
                            Error.Message.GOOGLE_API_CLIENT_CONN_FAILED + " Result: " + connectionResult.toString()));
            }
        });

        apiClient.connect();
    }

    private void onGoogleAPIClientConnection(final AppCompatActivity activity, final GoogleApiClient apiClient,
                                             final LocationRequest request, final HyperTrackCallback callback) {

        HyperTrackUtils.checkIfLocationIsEnabled(apiClient, request,
                new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                        final Status status = locationSettingsResult.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can
                                // initialize location requests here.
                                //Start Location Service here if not already active
                                HTLog.i(TAG, "LocationServices enabled by user successfully!");
                                if (callback != null)
                                    callback.onSuccess(new SuccessResponse(null));
                                break;

                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(activity,
                                            HyperTrack.REQUEST_CODE_LOCATION_SERVICES);

                                    if (callback != null)
                                        callback.onSuccess(new SuccessResponse(null));

                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                    e.printStackTrace();
                                    HTLog.e(TAG, "Exception occurred while requestLocationServices: " + e);
                                    if (callback != null)
                                        callback.onError(new ErrorResponse());
                                }
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way
                                // to fix the settings so we won't show the dialog.
                                // This happens when phone is in Airplane/Flight Mode
                                // Uncomment ErrorMessage to prevent this from popping up on AirplaneMode
                                // Toast.makeText(Home.this, R.string.invalid_current_location, Toast.LENGTH_SHORT).show();
                                HTLog.e(TAG, Error.Message.LOCATION_SETTINGS_CHANGE_UNAVAILABLE);
                                if (callback != null)
                                    callback.onError(new ErrorResponse(Error.Type.LOCATION_SETTINGS_CHANGE_UNAVAILABLE));
                                break;
                        }
                    }
                });
    }

    void getCurrentLocation(@NonNull HyperTrackCallback callback) {
        if (!checkIfSDKInitialized(callback))
            return;

        transmitterClient.getCurrentLocation(callback);
    }

    void startTracking(HyperTrackCallback callback) {
        if (!checkIfSDKInitialized(callback))
            return;

        eventsManager.logTrackingStartedEvent();
        transmitterClient.startTracking(true, callback);
    }

    void assignActions(@NonNull final List<String> actionIds, @NonNull final HyperTrackCallback callback) {
        if (!checkIfSDKInitialized(callback))
            return;

        try {
            // Flush cached data to server
            transmitterClient.flushCachedDataToServer();

            // Check for GetAction validation errors
            ErrorResponse validationError = ValidationUtil.getNetworkCallValidationError(mContext);
            if (validationError != null) {
                callback.onError(validationError);
                if (eventCallback != null)
                    eventCallback.onError(validationError);
                return;
            }

            if (TextUtils.isEmpty(userPreferences.getUserId())) {
                validationError = new ErrorResponse(Error.Type.USER_ID_NOT_CONFIGURED);
                callback.onError(validationError);
                if (eventCallback != null)
                    eventCallback.onError(validationError);
                return;
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("action_ids", actionIds);

            String url = BuildConfig.CORE_API_BASE_URL + HTConstants.ASSIGN_ACTION_USER_PATH
                    + userPreferences.getUserId() + HTConstants.ASSIGN_ACTION_URL;
            HyperTrackPostRequest<User> postNetworkRequest = new HyperTrackPostRequest<>(HTConstants.ASSIGN_ACTION_TAG,
                    mContext, url, HTNetworkClient.HT_NETWORK_CLIENT_HTTP, requestBody, User.class,
                    new HTNetworkResponse.Listener<User>() {
                        @Override
                        public void onResponse(User user) {
                            // Check if User is being tracked currently or not
                            if (!HyperTrack.isTracking())
                                HyperTrack.startTracking();

                            callback.onSuccess(new SuccessResponse(user));
                        }
                    },
                    new HTNetworkResponse.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error, Exception exception) {
                            callback.onError(new ErrorResponse(error));
                        }
                    });
            networkManager.execute(mContext, postNetworkRequest);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while assignActions: " + e);
            callback.onError(new ErrorResponse());
        }
    }

    void getAction(@NonNull String actionId, @NonNull final HyperTrackCallback callback) {
        if (!checkIfSDKInitialized(callback))
            return;

        try {
            // Flush cached data to server
            transmitterClient.flushCachedDataToServer();

            // Check for GetAction validation errors
            ErrorResponse validationError = ValidationUtil.getNetworkCallValidationError(mContext);
            if (validationError != null) {
                callback.onError(validationError);
                if (eventCallback != null)
                    eventCallback.onError(validationError);
                return;
            }

            String url = BuildConfig.CORE_API_BASE_URL + HTConstants.GET_ACTION_URL + actionId;
            HyperTrackGetRequest<Action> getNetworkRequest = new HyperTrackGetRequest<>(
                    HTConstants.GET_ACTION_TAG, mContext, url, HTNetworkClient.HT_NETWORK_CLIENT_HTTP, Action.class,
                    new HTNetworkResponse.Listener<Action>() {
                        @Override
                        public void onResponse(Action action) {
                            callback.onSuccess(new SuccessResponse(action));
                        }
                    },
                    new HTNetworkResponse.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error, Exception exception) {
                            ErrorResponse errorResponse = new ErrorResponse(error);
                            HTLog.e(TAG, "Error occurred while createUser: " + errorResponse.getErrorMessage());
                            callback.onError(errorResponse);
                            // Check for Invalid Token errors
                            if (eventCallback != null && NetworkErrorUtil.isInvalidTokenError(error)) {
                                eventCallback.onError(errorResponse);
                            }
                        }
                    });
            networkManager.execute(mContext, getNetworkRequest);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while createUser: " + e);
            callback.onError(new ErrorResponse());
        }
    }

    void completeAction(String actionId) {
        if (!checkIfSDKInitialized(null))
            return;

        transmitterClient.completeAction(actionId);
    }

    void stopTracking(HyperTrackCallback callback) {
        if (!checkIfSDKInitialized(callback))
            return;

        transmitterClient.stopTracking(callback);
        eventsManager.logTrackingEndedEvent();
    }

    boolean isTracking() {
        if (!checkIfSDKInitialized(null))
            return false;

        return transmitterClient.isTracking();
    }

    String getUserID() {
        if (!checkIfSDKInitialized(null))
            return null;

        return transmitterClient.getUserID();
    }

    boolean setServiceNotificationParams(final ServiceNotificationParams notificationParams) {
        if (!checkIfSDKInitialized(null))
            return false;

        return transmitterClient.setServiceNotificationParams(notificationParams);
    }

    void clearServiceNotificationParams() {
        if (!checkIfSDKInitialized(null))
            return;

        transmitterClient.clearServiceNotificationParams();
    }

    void enableDebugLogging(int logLevel) {
        HTLog.setLogLevel(logLevel);
    }

    String getSDKVersion() {
        return BuildConfig.VERSION_NAME;
    }

    String getSDKPlatform() {
        if (!checkIfSDKInitialized(null))
            return null;

        return userPreferences.getSDKPlatform();
    }

    void setSDKPlatform(String sdkPlatform) {
        if (!checkIfSDKInitialized(null))
            return;

        if (!TextUtils.isEmpty(sdkPlatform)) {
            userPreferences.setSDKPlatform(sdkPlatform);
        }
    }

    boolean connectUser(@NonNull final String userID) {
        if (!checkIfSDKInitialized(null))
            return false;

        return transmitterClient.connectUser(userID);
    }

    /**
     * HyperTrackImpl Private Methods
     */

    private boolean checkIfSDKInitialized(@Nullable HyperTrackCallback callback) {
        if (this.mContext == null) {
            if (callback != null) {
                callback.onError(new ErrorResponse(Error.Type.SDK_NOT_INITIALIZED));
            }
            if (eventCallback != null) {
                eventCallback.onError(new ErrorResponse(Error.Type.SDK_NOT_INITIALIZED));
            }
            Log.e(TAG, Error.Message.SDK_NOT_INITIALIZED);
            return false;
        }

        return true;
    }

    private void initTransmitterClient(Context mContext) {
        if (transmitterClient == null) {
            transmitterClient = new TransmitterClient(mContext, userPreferences, scheduler,
                    broadcastManager, new SDKControlsManager(mContext), logsManager, networkManager,
                    eventsManager);
        }

        transmitterClient.initTransmitter();
        transmitterClient.setEventCallback(eventCallback);
    }

    private void initConsumerClient(Context mContext) {
        if (consumerClient == null) {
            consumerClient = new ConsumerClient(mContext, scheduler, logsManager,
                    networkManager);
        }
        consumerClient.initConsumerClient(mContext);
    }

    private void setPublishableKey(Context context, String publishableKey) {
        // Check if a valid publishable key was provided
        if (publishableKey == null || publishableKey.isEmpty()) {
            HTLog.w(TAG, Error.Message.INVALID_PUBLISHABLE_KEY);
            return;
        }

        // Check if Secret Key was passed as PublishableKey
        if (publishableKey.contains("sk_")) {
            HTLog.w(TAG, Error.Message.SECRET_KEY_USED_AS_PUBLISHABLE_KEY);
        }

        savePublishableKey(publishableKey, context);
    }

    private void savePublishableKey(String publishableKey, Context context) {
        if (context == null) {
            return;
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(HTConstants.HT_SHARED_PREFS_KEY,
                Context.MODE_PRIVATE).edit();
        editor.putString(HTConstants.PUBLISHABLE_KEY_PREFS_KEY, publishableKey.trim());
        editor.apply();
    }

    /**
     * HyperTrackImpl Intent methods
     */

    void handleIntent(Context context, Intent intent) {
        // Log DeviceHealth event
        eventsManager.logHealthChangedEvent(DeviceHealth.getDeviceHealth(context));

        if (!isTracking() || intent == null || intent.getAction() == null)
            return;

        HTLog.i(TAG, "HyperTrackImpl.handleEvent called with action: " + intent.getAction());

        // Handle Boot Completed intent
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            handleBootCompletedIntent(context);

            // Handle Location Settings Changed intent
        } else if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            handleLocationSettingsChangedIntent(context);
        }

    }

    private void handleBootCompletedIntent(Context context) {
        HTLog.i(TAG, "Boot receiver on received");
        Intent onBootServiceIntent = new Intent(context, BootReceiverService.class);
        context.startService(onBootServiceIntent);
    }

    private void handleLocationSettingsChangedIntent(Context context) {
        // Check if user isTracking currently and Location settings were changed
        if (isTracking() && HyperTrackUtils.isLocationEnabled(context)) {
            HTLog.i(TAG, "Location Settings Changed called");
            transmitterClient.startTracking(true, null);
        }
    }
}
