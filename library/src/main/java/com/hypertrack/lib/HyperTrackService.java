package com.hypertrack.lib;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.common.util.UserPreferencesImpl;
import com.hypertrack.lib.internal.transmitter.controls.SDKControls;
import com.hypertrack.lib.internal.transmitter.utils.Constants;
import com.hypertrack.lib.internal.transmitter.utils.GPSStatusListener;
import com.hypertrack.lib.internal.transmitter.utils.HTServiceNotificationUtils;

/**
 * Created by piyush on 06/08/16.
 */
public class HyperTrackService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = HyperTrackService.class.getSimpleName();
    private static final int FOREGROUND_ID = 101010;

    public static HyperTrackService hyperTrackService;

    private HyperTrackServiceManager serviceManager;
    BroadcastReceiver mPowerSaverModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check for PowerSaverMode
            serviceManager.onPowerSaverModeChanged();
        }
    };
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private SDKControls sdkControls;
    private boolean isActiveTrackingMode;
    private UserPreferences userPreferences;
    private android.location.LocationManager androidLocationManager;
    private GPSStatusListener gpsStatusListener;
    private boolean powerSaverModeRegistered;

    // Private Constructor to prevent instantiation of LocationService from other modules
    public HyperTrackService() {
        super("HyperTrackService");
    }

    // On Service Destroyed
    @Override
    public void onDestroy() {
        try {
            HTLog.i(TAG, "HyperTrack Service onDestroy called");
            hyperTrackService = null;

            this.stopServices();
            this.stopListenForGPSStatus();
            this.registerPowerSaverModeReceiver(false);
            super.onDestroy();
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while HyperTrackService.onDestroy: " + e);
        }
    }

    // Stop Services & Updates
    private void stopServices() {
        // Stop Foreground Service Notification
        this.stopForeground(true);

        // Stop Location & Activity Updates
        this.stopUpdates();

        // Disconnect GoogleAPIClient
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    // Stop Location & Activity Updates
    private void stopUpdates() {
        this.stopLocationPolling();
        this.stopActivityRecognition();
        this.stopListenForGPSStatus();
    }

    // Self Stopping the Service
    public void stopSelfService() {
        HTLog.i(TAG, "Self stopping LocationService");
        this.stopSelf();
    }

    private void initialize() {
        userPreferences = UserPreferencesImpl.getInstance(getApplicationContext());
        serviceManager = HyperTrackImpl.getInstance().transmitterClient.getServiceManager();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            HTLog.i(TAG, "onStartCommand for LocationService called");

            this.initialize();

            // Check if Location Permission is available
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                HTLog.e(TAG, "Location Permission unavailable, startLocationPolling failed");
                serviceManager.onLocationSettingsError();
                this.stopSelfService();
                return START_STICKY;
            }

            hyperTrackService = this;

            // Check if Location is enabled
            if (!HyperTrackUtils.isLocationEnabled(getApplicationContext())) {
                serviceManager.onLocationSettingsError();
            }

            // Check if userID is available
            if (userPreferences.getUserId() == null) {
                this.stopSelfService();
                return START_STICKY;

            } else {
                if (intent != null && intent.hasExtra(Constants.HT_SDK_CONTROLS_KEY)) {
                    sdkControls = (SDKControls) intent.getSerializableExtra(Constants.HT_SDK_CONTROLS_KEY);
                    isActiveTrackingMode = intent.getBooleanExtra(Constants.HT_ACTIVE_TRACKING_MODE_KEY, false);
                }

                // Stop already active location updates
                stopUpdates();

                //Create Location Request object
                createLocationRequest();

                //Construct and connect GoogleApiClient object
                if (connectGoogleApiClient()) {
                    //Start Location & activity updates
                    HTLog.i(TAG, "GoogleAPIClient already CONNECTED");
                    startUpdates();
                }

                // Start Service in Foreground Mode, if User is being Actively tracked
                if (isActiveTrackingMode) {
                    this.startForeground();
                }
            }

            this.registerPowerSaverModeReceiver(true);
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while HyperTrackService.onCreate: " + e);
        }

        HTLog.i(TAG, "LocationService Duration: " + sdkControls.getMinimumDuration()
                + ", Displacement: " + sdkControls.getMinimumDisplacement());

        return START_REDELIVER_INTENT;
    }

    // Location Changed Listener
    @Override
    public void onLocationChanged(Location location) {
        if (location == null || location.getLatitude() == 0.0 || location.getLongitude() == 0.0) {
            HTLog.e(TAG, "Invalid Location received in onLocationChanged: " +
                    (location != null ? location.toString() : "null"));
        } else {
            HTLog.i(TAG, "Location Changed: " + location.toString());

            // Handle onLocationChanged event
            serviceManager.onLocationChanged(location, getLocationProvider());
        }
    }

    private String getLocationProvider() {
        // Initialize GPSStatusListener in case it is not initialized yet
        this.startListenForGPSStatus();

        String provider = null;
        if (gpsStatusListener != null)
            provider = gpsStatusListener.getProvider();

        return provider;
    }

    private void registerPowerSaverModeReceiver(boolean register) {
        try {
            if (register && !powerSaverModeRegistered) {
                powerSaverModeRegistered = true;
                this.registerReceiver(mPowerSaverModeChangedReceiver,
                        new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGED"));
            } else if (powerSaverModeRegistered) {
                powerSaverModeRegistered = false;
                this.unregisterReceiver(mPowerSaverModeChangedReceiver);
            }
        } catch (IllegalArgumentException e) {
            HTLog.e(TAG, "Exception occurred while registerPowerSaverModeReceiver(" + register + "): " + e);
        }
    }

    public void startForeground() {
        try {
            startForeground(FOREGROUND_ID, HTServiceNotificationUtils.getForegroundNotification(getApplicationContext()));
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while startForeground: " + e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connection to GoogleApiClient SUCCESSFUL");
        //Start Location & activity updates
        startUpdates();
    }

    private void startUpdates() {
        startLocationPolling();
        startListenForGPSStatus();
        startActivityRecognition();
    }

    /**
     * Method to initiate Location Polling
     */
    private void startLocationPolling() {
        try {
            if (mGoogleApiClient != null && mLocationRequest != null) {

                // Check if GoogleAPIClient is connected
                if (mGoogleApiClient.isConnected()) {

                    // Check if Location is enabled
                    if (HyperTrackUtils.isLocationEnabled(getApplicationContext())) {

                        // Check if Location Permission is available
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            HTLog.e(TAG, "Location Permission unavailable, startLocationPolling failed");
                            return;
                        }

                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        HTLog.i(TAG, "FusedLocation Updates Initiated!");

                    } else {
                        HTLog.e(TAG, "Could not initiate startLocationPolling: locationEnabled: " +
                                HyperTrackUtils.isLocationEnabled(getApplicationContext()));
                    }
                    return;

                } else {
                    HTLog.w(TAG, "GoogleAPIClient not connected yet. Retrying to connect");
                }
            }

            HTLog.w(TAG, "GoogleAPIClient or Location Request null. Re-initializing them.");

            //Create Location Request object
            this.createLocationRequest();

            //Construct and connect GoogleApiClient object
            this.connectGoogleApiClient();
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while startLocationPolling: " + e);
        }
    }

    private void startListenForGPSStatus() {
        if (gpsStatusListener != null)
            return;

        try {
            androidLocationManager = (android.location.LocationManager) this.getSystemService(LOCATION_SERVICE);
            gpsStatusListener = new GPSStatusListener(androidLocationManager);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                HTLog.e(TAG, "Could not start Listen For GPS Status: ACCESS_FINE_LOCATION permission unavailable");
                return;
            }

            if (!androidLocationManager.addGpsStatusListener(gpsStatusListener)) {
                HTLog.w(TAG, "GpsStatusListener could NOT be added successfully");
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while start Listen For GPS Status: " + e.getMessage());
        }
    }

    private void startActivityRecognition() {
        try {
            // Get Minimum Duration to start ActivityRecognition
            long updateDuration = sdkControls.getMinimumDuration() * 1000;
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                    updateDuration, getActivityRecognitionPendingIntent());
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while startActivityRecognition: " + e);
        }
    }

    /**
     * Method to cease further Location Polling
     */
    private void stopLocationPolling() {
        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, HyperTrackService.this);
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while stopLocationPolling: " + e);
        }
    }

    private void stopListenForGPSStatus() {
        if (androidLocationManager != null) {
            androidLocationManager.removeGpsStatusListener(gpsStatusListener);
            androidLocationManager = null;
            gpsStatusListener = null;
        }
    }

    private void stopActivityRecognition() {
        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,
                        getActivityRecognitionPendingIntent());
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while stopActivityRecognition: " + e);
        }
    }

    private PendingIntent getActivityRecognitionPendingIntent() {
        Intent activityIntent = new Intent(this, ActivityRecognitionService.class);
        return PendingIntent.getService(getApplicationContext(),
                Constants.REQUEST_CODE_ACTIVITY_RECOGNITION, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Method to instantiate Google API Client object and initiate its connection
     */
    private boolean connectGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(ActivityRecognition.API)
                    .build();
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            HTLog.i(TAG, "Initiating GoogleAPIClient connection");
            return false;
        }

        return true;
    }

    /**
     * Method to instantiate Location Request object with Location Interval and priority params specified
     */
    public void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
        }

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(sdkControls.getMinimumDuration() * 1000);
        mLocationRequest.setFastestInterval(sdkControls.getMinimumDuration() * 1000);
        mLocationRequest.setSmallestDisplacement(sdkControls.getMinimumDisplacement());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        HTLog.i(TAG, "GoogleAPIClient Connection Failed: " + connectionResult.toString());

        // Retry to connect to GoogleAPIClient
        connectGoogleApiClient();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // If your connection to the sensor gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            HTLog.i(TAG, "GoogleAPIClient Connection Suspended: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            HTLog.i(TAG, "GoogleAPIClient Connection Suspended: Service Disconnected");
        } else {
            HTLog.i(TAG, "GoogleAPIClient Connection Suspended");
        }

        // Retry to connect to GoogleAPIClient
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connectGoogleApiClient();
            }
        }, 1000);
    }
}