package io.hypertrack.sendeta.store;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.common.model.HTDriverVehicleType;
import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.lib.transmitter.model.HTTaskParams;
import io.hypertrack.lib.transmitter.model.HTTaskParamsBuilder;
import io.hypertrack.lib.transmitter.model.HTTrip;
import io.hypertrack.lib.transmitter.model.HTTripParams;
import io.hypertrack.lib.transmitter.model.HTTripParamsBuilder;
import io.hypertrack.lib.transmitter.model.ServiceNotificationParams;
import io.hypertrack.lib.transmitter.model.ServiceNotificationParamsBuilder;
import io.hypertrack.lib.transmitter.model.TransmitterConstants;
import io.hypertrack.lib.transmitter.model.callback.HTCompleteTaskStatusCallback;
import io.hypertrack.lib.transmitter.model.callback.HTTripStatusCallback;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;
import io.hypertrack.sendeta.MetaApplication;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.Task;
import io.hypertrack.sendeta.model.Trip;
import io.hypertrack.sendeta.model.TripETAResponse;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.service.GeofenceTransitionsIntentService;
import io.hypertrack.sendeta.store.callback.TripETACallback;
import io.hypertrack.sendeta.store.callback.TripManagerCallback;
import io.hypertrack.sendeta.store.callback.TripManagerListener;
import io.hypertrack.sendeta.store.callback.UserStoreGetTaskCallback;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.view.SplashScreen;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 18/06/16.
 */
public class TripManager implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = TripManager.class.getSimpleName();

    private static final long REFRESH_DELAY = 30000;

    public static final int LOITERING_DELAY_MS = 30000;
    private static final int NOTIFICATION_RESPONSIVENESS_MS = 5000;
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;
    private static final String GEOFENCE_REQUEST_ID = "io.hypertrack.meta:GeoFence";

    private HTTransmitterService transmitter = HTTransmitterService.getInstance(MetaApplication.getInstance().getApplicationContext());

    private Trip trip;
    private MetaPlace place;

    private int selectedAccountId;

    private HTTrip hyperTrackTrip;
    private HTDriverVehicleType vehicleType = HTDriverVehicleType.CAR;

    private GoogleApiClient mGoogleAPIClient;
    private GeofencingRequest geofencingRequest;
    private PendingIntent mGeofencePendingIntent;
    private boolean addGeofencingRequest;

    private TripManagerListener tripRefreshedListener;
    private TripManagerListener tripEndedListener;
    private BroadcastReceiver mTripEndedReceiver;
    private Handler handler;

    private static TripManager sSharedManager;

    public static TripManager getSharedManager() {
        if (sSharedManager == null) {
            sSharedManager = new TripManager();
        }

        return sSharedManager;
    }

    private TripManager() {
        this.setupGoogleAPIClient();

        if (transmitter != null) {
            //Customize Notification Settings
            ServiceNotificationParamsBuilder builder = new ServiceNotificationParamsBuilder();
            ServiceNotificationParams notificationParams = builder
                    .setSmallIconBGColor(ContextCompat.getColor(MetaApplication.getInstance().getApplicationContext(),
                            R.color.colorAccent))
                    .setContentIntentActivityClass(SplashScreen.class)
                    .build();
            transmitter.setServiceNotificationParams(notificationParams);
        }
    }

    private void setupGoogleAPIClient() {
        if (this.mGoogleAPIClient == null) {
            this.mGoogleAPIClient = new GoogleApiClient.Builder(MetaApplication.getInstance().getApplicationContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
        }

        this.mGoogleAPIClient.connect();
    }

    private void getCachedTripData() {
        this.trip = SharedPreferenceManager.getTrip();
        this.place = SharedPreferenceManager.getPlace();
    }

    public boolean shouldRestoreState() {
        this.getCachedTripData();

        // Check if current Trip exists in Shared Preference or not
        if (this.trip != null
                && this.place != null
                && transmitter.isTripActive()
//                && transmitter.getActiveTripID().equalsIgnoreCase(this.trip.getHypertrackTripID())
                ) {

            // Restore the current trip with locally cached data
            // Start Refreshing the trip without any delay

            // Added a delay to initiate RestoreTripStart Call (to account for delay in onMapLoadedCallback)
            onTripStart(4000);
            return true;

        } else {
            this.clearState();
            return false;
        }
    }

    final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {

            transmitter.refreshTrip(new HTTripStatusCallback() {
                @Override
                public void onSuccess(HTTrip htTrip) {

                    if (htTrip != null && htTrip.getLive() != null && !htTrip.getLive()) {
                        if (tripEndedListener != null) {
                            tripEndedListener.OnCallback();
                        }

                        clearState();
                        return;
                    }

                    hyperTrackTrip = htTrip;
                    onTripRefresh();
                }

                @Override
                public void onError(Exception e) {
                }
            });

            if (handler == null) {
                return;
            }

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    private void onTripRefresh() {
        if (this.tripRefreshedListener != null) {
            this.tripRefreshedListener.OnCallback();
        }
    }

    public boolean isTripActive() {
        return (this.trip != null);
    }

    public void getETA(LatLng origin, LatLng destination, final TripETACallback callback) {
        String originQueryParam = origin.latitude + "," + origin.longitude;
        String destinationQueryParam = destination.latitude + "," + destination.longitude;

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<List<TripETAResponse>> call = sendETAService.getETA(originQueryParam, destinationQueryParam);
        call.enqueue(new Callback<List<TripETAResponse>>() {
            @Override
            public void onResponse(Call<List<TripETAResponse>> call, Response<List<TripETAResponse>> response) {
                List<TripETAResponse> etaResponses = response.body();

                if (etaResponses != null && etaResponses.size() > 0) {
                    if (callback != null) {
                        callback.OnSuccess(etaResponses.get(0));
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TripETAResponse>> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    private void addTrip(final TripManagerCallback callback) {
        HashMap<String, String> tripDetails = new HashMap<>();
        tripDetails.put("hypertrack_trip_id", this.hyperTrackTrip.getId());
        tripDetails.put("hypertrack_task_id", this.hyperTrackTrip.getTaskIDs().get(0));

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<Trip> call = sendETAService.addTrip(tripDetails);
        call.enqueue(new Callback<Trip>() {
            @Override
            public void onResponse(Call<Trip> call, Response<Trip> response) {
                if (response.isSuccessful()) {
                    setTrip(response.body());
                    if (callback != null) {
                        callback.OnSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<Trip> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void sendETA(List<String> phoneNumbers, final TripManagerCallback callback) {
    }

    public void startTrip(int selectedAccountId, final TripManagerCallback callback) {
        if (this.place == null || selectedAccountId <= 0) {
            if (callback != null) {
                callback.OnError();
            }

            return;
        }

        this.selectedAccountId = selectedAccountId;

        UserStore.sharedStore.getTask(this.place, this.selectedAccountId, new UserStoreGetTaskCallback() {
            @Override
            public void OnSuccess(String taskID, String hypertrackDriverID, String publishableKey) {

                // Set PublishableKey fetched for the selectedAccountId
                HyperTrack.setPublishableApiKey(publishableKey, MetaApplication.getInstance().getApplicationContext());

                HTTripParams tripParams = getTripParams(taskID, hypertrackDriverID);
                transmitter.startTrip(tripParams, new HTTripStatusCallback() {
                    @Override
                    public void onSuccess(HTTrip htTrip) {
                        hyperTrackTrip = htTrip;

                        addTrip(new TripManagerCallback() {
                            @Override
                            public void OnSuccess() {
                                if (place == null) {
                                    place = SharedPreferenceManager.getPlace();
                                }

                                onTripStart();
                                if (callback != null) {
                                    callback.OnSuccess();
                                }
                            }

                            @Override
                            public void OnError() {
                                transmitter.clearCurrentTrip();
                                hyperTrackTrip = null;

                                if (callback != null) {
                                    callback.OnError();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.OnError();
                        }
                    }
                });
            }

            @Override
            public void OnError() {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void startTripWithTaskID(int selectedAccountId, Task task, final TripManagerCallback callback) {
        if (selectedAccountId <= 0) {
            if (callback != null) {
                callback.OnError();
            }

            return;
        }

        this.selectedAccountId = selectedAccountId;

        // Set PublishableKey fetched for the selectedAccountId
        HyperTrack.setPublishableApiKey(task.getPublishableKey(), MetaApplication.getInstance().getApplicationContext());

        HTTripParams tripParams = getTripParams(task.getId(), task.getDriverId());
        transmitter.startTrip(tripParams, new HTTripStatusCallback() {
            @Override
            public void onSuccess(HTTrip htTrip) {
                hyperTrackTrip = htTrip;

                addTrip(new TripManagerCallback() {
                    @Override
                    public void OnSuccess() {
                        if (place == null) {
                            place = SharedPreferenceManager.getPlace();
                        }

                        onTripStart();
                        if (callback != null) {
                            callback.OnSuccess();
                        }
                    }

                    @Override
                    public void OnError() {
                        transmitter.clearCurrentTrip();
                        hyperTrackTrip = null;

                        if (callback != null) {
                            callback.OnError();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void endTrip(final TripManagerCallback callback) {
        String taskID = this.trip.getHypertrackTaskID();
        if (taskID == null) {
            if (callback != null) {
                callback.OnSuccess();
            }

            clearState();
        }

        transmitter.completeTask(taskID, new HTCompleteTaskStatusCallback() {
            @Override
            public void onSuccess(String s) {
                transmitter.endTrip(new HTTripStatusCallback() {
                    @Override
                    public void onSuccess(HTTrip htTrip) {
                        callback.OnSuccess();
                        clearState();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.OnError();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void clearState() {
        this.trip = null;
        this.hyperTrackTrip = null;
        this.vehicleType = HTDriverVehicleType.CAR;
        this.stopRefreshingTrip();
        this.stopGeofencing();
        this.clearListeners();
        this.clearPlace();
        this.clearTrip();
        this.transmitter.clearCurrentTrip();
        this.unregisterForTripEndedBroadcast();
        // Remove GeoFencingRequest from SharedPreferences
        SharedPreferenceManager.removeGeofencingRequest();
    }

    private void clearListeners() {
        this.tripEndedListener = null;
        this.tripRefreshedListener = null;
    }

    // Start Trip with a default delay of REFRESH_DELAY
    private void onTripStart() {
        onTripStart(REFRESH_DELAY);
    }

    private void onTripStart(final long delay) {
        this.setupGeofencing();
        this.startRefreshingTrip(delay);
        this.registerForTripEndedBroadcast();
    }

    // Refresh Trip with a default delay of REFRESH_DELAY
    public void startRefreshingTrip() {
        startRefreshingTrip(REFRESH_DELAY);
    }

    public void startRefreshingTrip(final long delay) {
        if (handler == null) {
            handler = new Handler();
        }

        handler.postDelayed(refreshTask, delay);
    }

    public void stopRefreshingTrip() {
        if (this.handler != null) {
            this.handler.removeCallbacksAndMessages(refreshTask);
            this.handler = null;
        }
    }

    private HTTripParams getTripParams(String taskID, String hypertrackDriverID) {
        ArrayList<String> taskIDs = new ArrayList<>();
        taskIDs.add(taskID);

        return new HTTripParamsBuilder()
                .setDriverID(hypertrackDriverID)
                .setTaskIDs(taskIDs)
                .setVehicleType(this.vehicleType)
                .createHTTripParams();
    }

    private HTTaskParams getTaskParams(Task task) {
        return new HTTaskParamsBuilder()
                .setTaskID(task.getId())
                .setDriverID(task.getDriverId())
                .setVehicleType(task.getVehicleType())
                .createHTTaskParams();
    }

    private void stopGeofencing() {
        if (this.mGeofencePendingIntent != null) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleAPIClient, mGeofencePendingIntent);
            mGeofencePendingIntent = null;
        }
    }

    public void setGeofencingRequest(GeofencingRequest request) {
        this.geofencingRequest = request;
    }

    public void setupGeofencing() {
        try {
            geofencingRequest = this.getGeofencingRequest();

            if (geofencingRequest != null) {
                // Save this request to SharedPreferences (to be restored later if removed)
                SharedPreferenceManager.setGeofencingRequest(geofencingRequest);

                // Add Geofencing Request
                addGeofencingRequest();
            }
        } catch (Exception exception) {
            Crashlytics.logException(exception);
            HTLog.e(TAG, "Exception while adding geofence request");
        }
    }

    public void addGeofencingRequest() {
        if (geofencingRequest == null) {
            HTLog.e(TAG, "Error while adding geofence request: geofencingRequest is null");
            return;
        }

        if (this.mGoogleAPIClient == null || !this.mGoogleAPIClient.isConnected()) {
            this.addGeofencingRequest = true;
            setupGoogleAPIClient();
            return;
        }

        try {
            Context context = MetaApplication.getInstance().getAppContext();
            Intent geofencingIntent = new Intent(context, GeofenceTransitionsIntentService.class);
            mGeofencePendingIntent = PendingIntent.getService(context, 0, geofencingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            LocationServices.GeofencingApi.addGeofences(mGoogleAPIClient, geofencingRequest, mGeofencePendingIntent).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        HTLog.i(TAG, "Geofencing added successfully");
                        addGeofencingRequest = false;
                    } else {
                        HTLog.w(TAG, "Geofencing not added. There was an error");
                        addGeofencingRequest = true;
                    }
                }
            });
        } catch (SecurityException exception) {
            Crashlytics.logException(exception);
            HTLog.e(TAG, "Exception for geofence");
        }
    }

    public void OnGeoFenceSuccess() {
        if (this.trip == null) {
            this.getCachedTripData();
            if (this.trip == null) {
                if (tripEndedListener != null) {
                    tripEndedListener.OnCallback();
                }
                clearState();
                return;
            }
        }

        HTLog.i(TAG, "OnGeoFence success: Trip end initiated.");

        this.endTrip(new TripManagerCallback() {
            @Override
            public void OnSuccess() {
                if (tripEndedListener != null) {
                    tripEndedListener.OnCallback();
                }
                clearState();

                AnalyticsStore.getLogger().autoTripEnded(true, null);
                HTLog.i(TAG, "OnGeoFence success: Trip ended (Auto) successfully.");
            }

            @Override
            public void OnError() {
                AnalyticsStore.getLogger().autoTripEnded(false, ErrorMessages.AUTO_END_TRIP_FAILED);
                HTLog.e(TAG, "OnGeoFence success: Trip end (Auto) failed.");
            }
        });
    }

    private GeofencingRequest getGeofencingRequest() {

        if (this.place == null || this.place.getLatitude() == null || this.place.getLongitude() == null) {
            HTLog.e(TAG, "Adding Geofence failed: Either place or Lat,Lng is null");
            return null;
        }

        // called when the transition associated with the Geofence is triggered)
        List<Geofence> geoFenceList = new ArrayList<Geofence>();
        geoFenceList.add(new Geofence.Builder()
                .setRequestId(GEOFENCE_REQUEST_ID)
                .setCircularRegion(
                        this.place.getLatitude(),
                        this.place.getLongitude(),
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLoiteringDelay(LOITERING_DELAY_MS)
                .setNotificationResponsiveness(NOTIFICATION_RESPONSIVENESS_MS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build());

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geoFenceList);
        return builder.build();
    }

    public MetaPlace getPlace() {
        return place;
    }

    public void setPlace(MetaPlace place) {
        this.place = place;
        this.savePlace();
    }

    private void clearPlace() {
        this.deletePlace();
        this.place = null;
    }

    public Trip getTrip() {
        return this.trip;
    }

    public void setTripRefreshedListener(TripManagerListener listener) {
        this.tripRefreshedListener = listener;
    }

    public void setTripEndedListener(TripManagerListener listener) {
        this.tripEndedListener = listener;
    }

    public HTTrip getHyperTrackTrip() {
        return this.hyperTrackTrip;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (this.addGeofencingRequest) {
            addGeofencingRequest();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    private void savePlace() {
        SharedPreferenceManager.setPlace(this.place);
    }

    private void deletePlace() {
        SharedPreferenceManager.deletePlace();
    }

    private void clearTrip() {
        SharedPreferenceManager.deleteTrip();
        this.trip = null;
    }

    private void setTrip(final Trip tripToSave) {
        SharedPreferenceManager.setTrip(tripToSave);
        this.trip = tripToSave;
    }

    private void registerForTripEndedBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TransmitterConstants.HT_ON_TRIP_ENDED_INTENT);

        mTripEndedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (tripEndedListener != null) {
                    tripEndedListener.OnCallback();
                }
                clearState();
            }
        };

        LocalBroadcastManager.getInstance(MetaApplication.getInstance().getApplicationContext()).registerReceiver(mTripEndedReceiver, filter);
    }

    private void unregisterForTripEndedBroadcast() {
        if (this.mTripEndedReceiver != null) {
            LocalBroadcastManager.getInstance(MetaApplication.getInstance().getApplicationContext()).unregisterReceiver(mTripEndedReceiver);
            this.mTripEndedReceiver = null;
        }
    }

    private String getFormattedETA() {
        if (this.hyperTrackTrip == null || this.hyperTrackTrip.getETA() == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        String formattedDate = dateFormat.format(this.hyperTrackTrip.getETA());

        formattedDate = formattedDate.toLowerCase();
        formattedDate = formattedDate.replace("a", "A");
        formattedDate = formattedDate.replace("m", "M");
        formattedDate = formattedDate.replace("p", "P");
        formattedDate = formattedDate.replace(".", "");

        return formattedDate;
    }

    public String getShareMessage() {

        if (this.trip == null) {
            HTLog.e(TAG, "Trip is null. Not able to get shareMessage");
            return null;
        }

        StringBuilder builder = new StringBuilder("I'm on my way. ");

        String formattedETA = this.getFormattedETA();
        String shareURL = this.trip.getShareUrl();

        // Add ETA in ShareMessage if ETA is not null
        if (formattedETA != null) {
            builder.append("Will be there by " + formattedETA + ". ");

            // Add ShareURL in ShareMessage if ShareURL is not null
            if (shareURL != null) {
                builder.append("Track me live " + shareURL);

            } else {
                HTLog.e(TAG, "shareURL is null. Removing ShareURL from shareMessage");
            }

            return builder.toString();
        }

        HTLog.e(TAG, "formattedETA is null. Removing ETA from shareMessage");

        // FormattedETA is null. So, Add ShareURL in ShareMessage if ShareURL is not null
        if (shareURL != null) {
            builder.append("Track me live " + shareURL);
            return builder.toString();
        }

        return null;
    }
}
