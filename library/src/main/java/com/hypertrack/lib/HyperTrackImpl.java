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
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.network.NetworkManagerImpl;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.common.util.UserPreferencesImpl;
import com.hypertrack.lib.internal.consumer.HTConsumerClient;
import com.hypertrack.lib.internal.transmitter.TransmitterClient;
import com.hypertrack.lib.internal.transmitter.controls.SDKControlsManager;
import com.hypertrack.lib.internal.transmitter.devicehealth.DeviceHealth;
import com.hypertrack.lib.internal.transmitter.events.EventsDatabaseHelper;
import com.hypertrack.lib.internal.transmitter.events.EventsManager;
import com.hypertrack.lib.internal.transmitter.utils.BroadcastManager;
import com.hypertrack.lib.internal.transmitter.utils.BroadcastManagerImpl;
import com.hypertrack.lib.models.Error;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

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
    TransmitterClient transmitterClient;
    UserPreferences userPreferences;
    DeviceLogsManager logsManager;
    EventsManager eventsManager;
    HTConsumerClient consumerClient;
    private HyperTrackEventCallback eventCallback;
    private SmartScheduler scheduler;
    private NetworkManager networkManager;
    private BroadcastManager broadcastManager;

    private HyperTrackImpl() {
    }

    public static HyperTrackImpl getInstance() {
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

        context = context.getApplicationContext();

        // HyperTrack Initializations
        HTLog.initHTLog(context, DeviceLogDatabaseHelper.getInstance(context));

        if (scheduler == null)
            scheduler = SmartScheduler.getInstance(context);

        if (userPreferences == null)
            userPreferences = UserPreferencesImpl.getInstance(context);

        if (networkManager == null)
            networkManager = NetworkManagerImpl.getInstance(context, userPreferences.getUserId(),
                    scheduler);

        if (logsManager == null)
            logsManager = DeviceLogsManager.getInstance(context,
                    DeviceLogDatabaseHelper.getInstance(context), networkManager);

        if (broadcastManager == null)
            broadcastManager = new BroadcastManagerImpl(context);

        if (eventsManager == null)
            eventsManager = EventsManager.getInstance(context, EventsDatabaseHelper.getInstance(context),
                    userPreferences, networkManager, logsManager, broadcastManager, eventCallback);

        // Initialize TransmitterClient
        initTransmitterClient(context);

        // Initialize HTConsumerClient
        initHTConsumerClient(context);
    }

    String getPublishableKey(Context context) {
        if (context == null) {
            return null;
        }

        return context.getSharedPreferences(HTConstants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
                .getString(HTConstants.PUBLISHABLE_KEY_PREFS_KEY, null);
    }

    void createUser(@NonNull String userName, final HyperTrackCallback callback) {
        transmitterClient.createUser(userName, callback);
    }

    void setUserId(@NonNull final String userID) {
        transmitterClient.setUserID(userID);
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
        transmitterClient.getCurrentLocation(callback);
    }

    boolean connectUser(@NonNull final String userID) {
        return transmitterClient.connectUser(userID);
    }

    void startTracking(HyperTrackCallback callback) {
        eventsManager.logTrackingStartedEvent();
        transmitterClient.startTracking(true, callback);
    }

    void completeAction(String actionId) {
        transmitterClient.completeAction(actionId);
    }

    void stopTracking(HyperTrackCallback callback) {
        transmitterClient.stopTracking(callback);
        eventsManager.logTrackingEndedEvent();
    }

    boolean isTracking() {
        return transmitterClient.isTracking();
    }

    String getUserID() {
        return transmitterClient.getUserID();
    }

    String getSDKVersion() {
        return BuildConfig.VERSION_NAME;
    }

    String getSDKPlatform() {
        return userPreferences.getSDKPlatform();
    }

    void setSDKPlatform(String sdkPlatform) {
        if (!TextUtils.isEmpty(sdkPlatform)) {
            userPreferences.setSDKPlatform(sdkPlatform);
        }
    }

    void enableDebugLogging(int logLevel) {
        HTLog.setLogLevel(logLevel);
    }

    /**
     * HyperTrackImpl Private Methods
     */

    private void initTransmitterClient(Context mContext) {
        if (transmitterClient == null) {
            transmitterClient = new TransmitterClient(mContext, userPreferences, scheduler,
                    broadcastManager, new SDKControlsManager(mContext), logsManager, networkManager,
                    eventsManager);
        }

        transmitterClient.initTransmitter();
        transmitterClient.setEventCallback(eventCallback);
    }

    private void initHTConsumerClient(Context mContext) {
        if (consumerClient == null) {
            consumerClient = new HTConsumerClient(mContext, scheduler, logsManager,
                    networkManager);
        }
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
