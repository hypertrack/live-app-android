package com.hypertrack.lib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.common.util.Utils;
import com.hypertrack.lib.internal.transmitter.devicehealth.DeviceHealth;
import com.hypertrack.lib.internal.transmitter.events.EventsManager;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;
import com.hypertrack.lib.internal.transmitter.utils.Constants;
import com.hypertrack.lib.models.Error;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * Created by piyush on 02/02/17.
 */
public class HyperTrackServiceManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = HyperTrackServiceManager.class.getSimpleName();
    private static final long LOCATION_UPDATE_INTERVAL = 1000L;
    private static final long LOCATION_UPDATE_FASTEST_INTERVAL = 500L;
    private Context context;
    private SmartScheduler jobScheduler;
    private UserPreferences userPreferences;
    private EventsManager eventsManager;

    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;
    private LocationListener locationListener;
    private HyperTrackStop userStopToBeAdded;

    private boolean addGeofencePending, removeGeofencePending, startLocationUpdateOncePending,
            stopUpdateLocationOncePending;
    private HyperTrackCallback startLocationUpdateOnceCallback;

    public HyperTrackServiceManager(Context context, SmartScheduler jobScheduler,
                                    UserPreferences userPreferences, EventsManager eventsManager) {
        this.context = context;
        this.jobScheduler = jobScheduler;
        this.userPreferences = userPreferences;
        this.eventsManager = eventsManager;
    }

    void onLocationSettingsError() {
        eventsManager.logHealthChangedEvent(DeviceHealth.getDeviceHealth(context));
    }

    void onPowerSaverModeChanged() {
        try {
            // Update HTJobScheduler on PowerSaver Mode Change
            boolean isPowerSaveMode = Utils.checkIfPowerSaverModeEnabled(context);
            jobScheduler.onPowerSaverModeChanged(isPowerSaveMode);
            eventsManager.logHealthChangedEvent(DeviceHealth.getDeviceHealth(context));
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while onPowerSaverModeChanged: " + e);
        }
    }

    void onLocationChanged(Location location, String provider) {
        try {
            String mActivityName = userPreferences.getLastRecordedActivityName();
            int mActivityConfidence = userPreferences.getLastRecordedActivityConfidence();

            // Add HyperTrackLocation to DB
            HyperTrackLocation updatedLocation = getHyperTrackLocation(location, provider,
                    mActivityName, mActivityConfidence);

            // Save updated Location to UserPreferences
            userPreferences.setLastRecordedLocation(updatedLocation);

            // Check if current location is the first location
            if (location != null && userPreferences.isFirstLocation()) {
                HTLog.i(TAG, "First Location received. Adding stop.started event");
                userPreferences.setIsFirstLocation(false);

                // Add a Stop
                this.userStopToBeAdded = new HyperTrackStop(updatedLocation);
                addUserStop(this.userStopToBeAdded);
            } else {
                // Check if location changed event can be logged
                HyperTrackStop existingUserStop = userPreferences.getUserStop();
                if (existingUserStop == null || !existingUserStop.isStopStarted() ||
                        handleFarOffLocationFallback(existingUserStop, updatedLocation))
                    eventsManager.logLocationChangedEvent(updatedLocation);
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while onLocationChanged: " + e);
        }
    }

    void onUserActivityChanged(String updatedActivityName, int updatedActivityConfidence) {
        HTLog.i(TAG, "Activity Changed: " + updatedActivityName + ", confidence: " + updatedActivityConfidence);

        // Check if stop has not started yet and user started moving
        HyperTrackStop existingStop = userPreferences.getUserStop();
        if (existingStop != null && !existingStop.isStopStarted()
                && Constants.USER_ACTIVITY_AUTOMOTIVE.equals(updatedActivityName)) {
            // Remove user's stop
            HyperTrackImpl.getInstance().transmitterClient.getServiceManager().removeUserStop();
        }

        // Fetch user's last updated location
        HyperTrackLocation location = userPreferences.getLastRecordedLocation();
        boolean userStopAdded = false;
        // Check if the user has stopped or not
        if (checkForStopDetection(location, updatedActivityName)) {
            // Check if a valid location is available
            if (location != null) {
                this.userStopToBeAdded = new HyperTrackStop(location);
                userStopAdded = addUserStop(this.userStopToBeAdded);

            } else {
                userPreferences.setUserStop(new HyperTrackStop());
                HTLog.e(TAG, "Location is null. Cannot set a geofence currently.");
            }
        }

        // Log ActivityChanged event if no stop was detected
        if (!userStopAdded && location != null) {
            location.setActivityDetails(updatedActivityName, updatedActivityConfidence);
            eventsManager.logActivityChangedEvent(location);
        }

        // Save updated activity data in UserPreferences
        userPreferences.setLastRecordedActivity(updatedActivityName, updatedActivityConfidence);
    }

    private boolean checkForStopDetection(HyperTrackLocation location, String activityName) {
        // Check if user is on a stop currently
        if (hasExistingStop(location)) {
            return false;
        }

        // Check if current location is the first location
        if (location != null && userPreferences.isFirstLocation()) {
            HTLog.i(TAG, "First Location received. Adding stop.started event");
            userPreferences.setIsFirstLocation(false);
            return true;
        }

        return checkActivityForStopDetection(userPreferences.getLastRecordedActivityName(), activityName);
    }

    private boolean hasExistingStop(HyperTrackLocation location) {
        // Fetch existing user stop, if any
        HyperTrackStop existingStop = userPreferences.getUserStop();
        if (existingStop != null) {
            // Check if stop has already started or not
            if (existingStop.isStopStarted()) {
                return true;
            }

            // Check if stop exists for the this location or not
            if (location != null && location.equals(existingStop.getLocation())) {
                HTLog.w(TAG, "Stop " + existingStop.getId() + " already exists for same location. Not adding a new stop.");
                return true;
            }
        }

        return false;
    }

    private boolean checkActivityForStopDetection(String lastRecordedActivity, String updatedActivity) {
        if (lastRecordedActivity == null)
            return Constants.USER_ACTIVITY_STATIONARY.equals(updatedActivity)
                    || Constants.USER_ACTIVITY_UNKNOWN.equals(updatedActivity);

        switch (lastRecordedActivity) {
            case Constants.USER_ACTIVITY_AUTOMOTIVE:
                return true;

            case Constants.USER_ACTIVITY_CYCLING:
            case Constants.USER_ACTIVITY_RUNNING:
            case Constants.USER_ACTIVITY_WALKING:
                if (Constants.USER_ACTIVITY_STATIONARY.equals(updatedActivity)
                        || Constants.USER_ACTIVITY_UNKNOWN.equals(updatedActivity))
                    return true;
                break;
            default:
                return false;
        }

        return false;
    }

    public void startLocationUpdateOnce(final HyperTrackCallback callback) {
        try {
            // Check if Location Permission is granted
            if (HyperTrackUtils.isLocationPermissionAvailable(context)) {

                // Check if Location Settings are enabled
                if (HyperTrackUtils.isLocationEnabled(context)) {

                    // Check if GoogleAPIClient is connected & LocationRequest is created
                    if (!connectGoogleApiClient()) {

                        // Set CurrentLocation pending state
                        this.startLocationUpdateOncePending = true;
                        this.startLocationUpdateOnceCallback = callback;
                        return;
                    }

                    // Create LocationRequest object
                    createLocationRequest();

                    // Fetch Current location
                    startUpdateLocationOnce(callback);

                    // Reset CurrentLocation pending state
                    this.startLocationUpdateOnceCallback = null;

                } else {
                    HTLog.e(TAG, "Error occurred while startLocationUpdateOnce. Code: " +
                            Error.Code.LOCATION_SETTINGS_DISABLED);
                    if (callback != null) {
                        callback.onError(new ErrorResponse(Error.Type.LOCATION_SETTINGS_DISABLED));
                    }
                }
            } else {
                HTLog.e(TAG, "Error occurred while startLocationUpdateOnce. Code: " +
                        Error.Code.PERMISSIONS_NOT_REQUESTED);
                if (callback != null) {
                    callback.onError(new ErrorResponse(Error.Type.PERMISSIONS_NOT_REQUESTED));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeUserStop() {
        // Remove the user's stop timeout and geofence
        userPreferences.clearUserStop();
        removeStopStartedTimeout();
        removeGeofence();
    }

    public synchronized boolean addUserStop(HyperTrackStop userStop) {
        try {
            if (userStop == null) {
                HTLog.e(TAG, "Error ocurred while addUserStop: HyperTrackStop is NULL.");
                return false;
            }

            // Update UserStop to be added
            this.userStopToBeAdded = userStop;

            // Remove existing geofence, if any
            removeGeofence();

            // Add Geofence
            addGeofence(userStop);
            return true;

        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while addUserStop: " + e);
        }

        return false;
    }

    private void removeGeofence() {
        try {
            // Connect GoogleAPIClient if not connected
            if (!connectGoogleApiClient()) {
                this.removeGeofencePending = true;
                return;
            }

            // Remove existing Geofence
            HTLog.i(TAG, "Removing existing geofence");
            LocationServices.GeofencingApi.removeGeofences(googleApiClient, getGeofencePendingIntent());
            userPreferences.clearUserStop();

            // Check if there was a pending updateGeofence command
            if (this.addGeofencePending) {
                HTLog.i(TAG, "Geofence removed. Has pending addGeofence command.");
                this.addGeofencePending = false;
                addGeofence(this.userStopToBeAdded);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while removeGeofence: " + e);
        }
    }

    private void addGeofence(final HyperTrackStop hyperTrackStop) {
        if (hyperTrackStop == null || hyperTrackStop.getLocation() == null
                || hyperTrackStop.getLocation().getGeoJSONLocation() == null) {
            HTLog.e(TAG, "Error ocurred while addGeofences: HyperTrackStop or Location is NULL.");
            return;
        }

        // Set StopStarted Timeout
        if (!hyperTrackStop.isStopStarted()) {
            setStopStartedTimeout(hyperTrackStop);
        }

        // Connect GoogleAPIClient if not connected
        if (!connectGoogleApiClient()) {
            this.addGeofencePending = true;
            return;
        }

        HTLog.i(TAG, "Adding a geofence for StopID: " + hyperTrackStop.getId());

        // Save User's Stop to UserPreferences
        userPreferences.setUserStop(hyperTrackStop);

        LocationServices.GeofencingApi.addGeofences(googleApiClient, createGeofenceRequest(hyperTrackStop),
                getGeofencePendingIntent()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // Update isAdded flag for User's Stop in UserPreferences
                    HTLog.i(TAG, "Geofence added successfully for Stop ID: " + hyperTrackStop.getId());
                    hyperTrackStop.setAdded(true);
                    userPreferences.setUserStop(hyperTrackStop);
                    userStopToBeAdded = null;

                } else {
                    HTLog.e(TAG, "Error while adding geofence: " + status.getStatusCode() +
                            (status.getStatusMessage() != null ? ", " + status.getStatusMessage() : ""));

                    // Check if adding Geofences failed due to TOO_MANY_GEOFENCES/TOO_MANY_PENDING_INTENTS
                    if (status.getStatusCode() == GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES
                            || status.getStatusCode() == GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS) {

                        // Enqueue removeGeofences & updateGeofences again
                        addGeofencePending = true;
                        removeGeofence();
                    }
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Execute pending removeGeofence command, if any
        if (this.removeGeofencePending) {
            this.removeGeofencePending = false;
            HTLog.i(TAG, "GoogleAPIClient connected. Has pending removeGeofence command.");
            removeGeofence();

        } else if (this.addGeofencePending) {
            // Execute pending updateGeofence command, if any
            this.addGeofencePending = false;
            HTLog.i(TAG, "GoogleAPIClient connected. Has pending addGeofence command.");
            addUserStop(userStopToBeAdded);
        }

        // Execute pending startLocationUpdateOnce command, if any
        if (this.startLocationUpdateOncePending) {
            this.startLocationUpdateOncePending = false;
            HTLog.i(TAG, "GoogleAPIClient connected. Has pending startLocationUpdateOnce command.");
            startLocationUpdateOnce(this.startLocationUpdateOnceCallback);
        }

        // Execute pending stopUpdateLocationOnce command, if any
        if (this.stopUpdateLocationOncePending) {
            HTLog.i(TAG, "GoogleAPIClient connected. Has pending stopUpdateLocationOnce command.");
            this.stopUpdateLocationOncePending = false;
            stopUpdateLocationOnce();
        }
    }

    private void setStopStartedTimeout(HyperTrackStop hyperTrackStop) {
        try {
            Intent intent = new Intent(context, HyperTrackAlarmReceiver.class);
            intent.setAction(Constants.INTENT_ACTION_STOP_TIMEOUT_ALARM);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    Constants.REQUEST_CODE_STOP_GEOFENCE_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();

            //Set the alarm for the first time and update the same in SharedPreferences
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long triggerInMillis = calendar.getTimeInMillis() + Constants.STOP_STARTED_GEOFENCE_TIMEOUT;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerInMillis, pendingIntent);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarm.setExact(AlarmManager.RTC_WAKEUP, triggerInMillis, pendingIntent);

            } else {
                alarm.set(AlarmManager.RTC_WAKEUP, triggerInMillis, pendingIntent);
            }

            HTLog.i(TAG, "Stop timeout set for StopID: " + hyperTrackStop.getId());
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while setStopStartedTimeout: " + e);
        }
    }

    private void removeStopStartedTimeout() {
        try {
            Intent intent = new Intent(context, HyperTrackAlarmReceiver.class);
            intent.setAction(Constants.INTENT_ACTION_STOP_TIMEOUT_ALARM);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    Constants.REQUEST_CODE_STOP_GEOFENCE_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Removes the stop started timeout
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pendingIntent);

            HTLog.i(TAG, "Stop timeout removed.");
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while removeStopStartedTimeout: " + e);
        }
    }

    private HyperTrackLocation getHyperTrackLocation(Location location, String provider,
                                                     String activityName, int activityConfidence) {
        // Update HyperTrackLocation with Location Provider, Activity Data & Confidence
        HyperTrackLocation hyperTrackLocation = new HyperTrackLocation(location, provider);
        if (activityName != null && activityConfidence != -1) {
            hyperTrackLocation.setActivityDetails(activityName, activityConfidence);
        }
        return hyperTrackLocation;
    }

    private boolean handleFarOffLocationFallback(final HyperTrackStop userStop,
                                                 final HyperTrackLocation updatedLocation) {
        // Add LocationChanged event only if user not at a stop
        if (userStop !=null && userStop.getLocation() != null && userStop.getLocation().getGeoJSONLocation() != null
                && userStop.getLocation().getAccuracy() != null && userStop.getLocation().getAccuracy() <= 50F) {

            // FALLBACK: Check if user has moved far away from stop and geofence didnt get triggered
            double distance = HyperTrackUtils.distance(
                    userStop.getLocation().getGeoJSONLocation().getLatitude(),
                    userStop.getLocation().getGeoJSONLocation().getLongitude(),
                    updatedLocation.getGeoJSONLocation().getLatitude(),
                    updatedLocation.getGeoJSONLocation().getLongitude(), "km");

            if (distance > 0.3) {
                HTLog.i(TAG, "handleFarOffLocationFallback triggered for Stop ID: " + userStop.getId());
                // Log StopEnded event and Remove user's stop
                eventsManager.logStopEndedEvent(userStop.getId(), userStop, updatedLocation);
                removeUserStop();
                return true;
            }
        }

        return false;
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent geofencingIntent = new Intent(context, GeofenceTransitionService.class);
        geofencingIntent.setAction(Constants.INTENT_ACTION_STOP_GEOFENCE_TRANSITION);
        return PendingIntent.getService(context,
                Constants.REQUEST_CODE_GEOFENCE_TRANSITION, geofencingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest createGeofenceRequest(HyperTrackStop hyperTrackStop) {
        List<com.google.android.gms.location.Geofence> geoFenceList = new ArrayList<>();
        geoFenceList.add(new Geofence.Builder()
                .setRequestId(hyperTrackStop.getId())
                .setCircularRegion(
                        hyperTrackStop.getLocation().getGeoJSONLocation().getLatitude(),
                        hyperTrackStop.getLocation().getGeoJSONLocation().getLongitude(),
                        hyperTrackStop.getRadius()
                )
                .setTransitionTypes(hyperTrackStop.getTransitionType())
                .setNotificationResponsiveness(hyperTrackStop.getNotificationResponsiveness())
                .setExpirationDuration(hyperTrackStop.getExpirationDuration())
                .build());

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(hyperTrackStop.getInitialTrigger());
        builder.addGeofences(geoFenceList);
        return builder.build();
    }

    private void startUpdateLocationOnce(final HyperTrackCallback callback) {
        try {
            this.locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Check if a valid Location is received or not
                    if (location == null || location.getLatitude() == 0.0 || location.getLongitude() == 0.0) {
                        HTLog.e(TAG, "Invalid Location received in startLocationUpdateOnce: " +
                                (location != null ? location.toString() : "null"));
                        if (callback != null) {
                            callback.onError(new ErrorResponse(Error.Type.INVALID_LOCATION_RECEIVED));
                        }
                    } else {

                        // Handle onLocationChanged event
                        HTLog.i(TAG, "Current Location Changed: " + location.toString());
                        if (callback != null) {
                            callback.onSuccess(new SuccessResponse(location));
                        }
                    }

                    // Stop location update on first location received
                    stopUpdateLocationOnce();
                }
            };

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest,
                    locationListener);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while startUpdateLocationOnce: " + e);
            if (callback != null) {
                callback.onError(new ErrorResponse());
            }
        }
    }

    private void stopUpdateLocationOnce() {
        try {
            if (!connectGoogleApiClient()) {
                this.stopUpdateLocationOncePending = true;
                return;
            }

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this.locationListener);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while stopUpdateLocationOnce: " + e);
        }
    }

    private boolean connectGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
            HTLog.i(TAG, "Initiating GoogleAPIClient connection");
            return false;
        }

        return true;
    }

    private void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL)
                    .setInterval(LOCATION_UPDATE_INTERVAL);
        }
    }

    public void clearPendingCommands() {
        addGeofencePending = false;
        removeGeofencePending = false;
        startLocationUpdateOncePending = false;
        stopUpdateLocationOncePending = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        // If your connection to the sensor gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            HTLog.i(TAG, "HyperTrackServiceManager: GoogleAPIClient Connection Suspended: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            HTLog.i(TAG, "HyperTrackServiceManager: GoogleAPIClient Connection Suspended: Service Disconnected");
        } else {
            HTLog.i(TAG, "HyperTrackServiceManager: GoogleAPIClient Connection Suspended");
        }

        // Check for pending startLocationUpdateOnce command
        if (this.startLocationUpdateOncePending) {
            this.startLocationUpdateOncePending = false;
            if (startLocationUpdateOnceCallback != null) {
                startLocationUpdateOnceCallback.onError(new ErrorResponse(
                        Error.Type.GOOGLE_API_CLIENT_CONN_SUSPENDED));
            }
            return;
        }

        // Retry to connect to GoogleAPIClient
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connectGoogleApiClient();
            }
        }, 1000);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        HTLog.i(TAG, "HyperTrackServiceManager: GoogleAPIClient Connection Failed: " + connectionResult.toString());

        // Check for pending startLocationUpdateOnce command
        if (this.startLocationUpdateOncePending) {
            this.startLocationUpdateOncePending = false;
            if (startLocationUpdateOnceCallback != null) {
                startLocationUpdateOnceCallback.onError(new ErrorResponse(
                        Error.Type.GOOGLE_API_CLIENT_CONN_SUSPENDED));
            }
            return;
        }

        connectGoogleApiClient();
    }
}
