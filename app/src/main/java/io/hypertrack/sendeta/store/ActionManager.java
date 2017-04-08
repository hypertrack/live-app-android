package io.hypertrack.sendeta.store;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.model.TaskETAResponse;
import io.hypertrack.sendeta.model.UserPlace;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import io.hypertrack.sendeta.service.GeofenceTransitionsIntentService;
import io.hypertrack.sendeta.store.callback.ActionManagerCallback;
import io.hypertrack.sendeta.store.callback.ActionManagerListener;
import io.hypertrack.sendeta.store.callback.TaskETACallback;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 15/08/16.
 */
public class ActionManager implements GoogleApiClient.ConnectionCallbacks {

    public static final int LOITERING_DELAY_MS = 30000;
    private static final String TAG = ActionManager.class.getSimpleName();
    private static final long REFRESH_DELAY = 30000;
    private static final int NOTIFICATION_RESPONSIVENESS_MS = 5000;
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;
    private static final String GEOFENCE_REQUEST_ID = "io.hypertrack.meta:GeoFence";
    private static ActionManager sharedManager;
    private Context mContext;
    private String actionID;
    private Action hyperTrackAction;
    private UserPlace lastUpdatedDestination = null;
    private UserPlace place;
    private GoogleApiClient mGoogleAPIClient;
    private GeofencingRequest geofencingRequest;
    private PendingIntent mGeofencePendingIntent;
    private boolean addGeofencingRequest;
    private ActionManagerListener actionRefreshedListener, actionCompletedListener;
    private Handler handler;
    private final Runnable refreshAction = new Runnable() {
        @Override
        public void run() {

            final Action action = ActionManager.getSharedManager(mContext).getHyperTrackAction();
            if (!isActionLive())
                return;

            HyperTrack.getAction(action.getId(), new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    if (!isActionLive())
                        return;
                    Action actionResponse = (Action) response.getResponseObject();
                    HTLog.i(TAG, "Get Action Response : " + actionResponse.toString());
                    if (actionResponse == null || TextUtils.isEmpty(actionResponse.getId())) {
                        HTLog.i(TAG, "HyperTrack-Live Action Not Live, Calling completeAction() to complete the action");

                        if (actionCompletedListener != null) {
                            actionCompletedListener.OnCallback();
                        }
                        return;
                    }
                    hyperTrackAction = actionResponse;
                    onActionRefresh();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {

                }
            });
            if (handler == null) {
                return;
            }

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    private ActionManager(Context mContext) {
        this.mContext = mContext;
    }

    public static ActionManager getSharedManager(Context context) {
        if (sharedManager == null) {
            sharedManager = new ActionManager(context);
        }

        return sharedManager;
    }

    // Method to setup GoogleApiClient to add geofence request
    private void setupGoogleAPIClient() {
        if (this.mGoogleAPIClient == null) {
            this.mGoogleAPIClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
        }

        this.mGoogleAPIClient.connect();
    }

    // Method to get saved ActionData
    private void getSavedActionData() {
        this.hyperTrackAction = SharedPreferenceManager.getAction(mContext);
        this.actionID = SharedPreferenceManager.getActionID(mContext);
        this.place = SharedPreferenceManager.getActionPlace();
    }

    public boolean shouldRestoreState() {

        // Restore the current task with locally cached data
        this.getSavedActionData();

        // Check if current Task exists in Shared Preference or not
        if (this.hyperTrackAction != null) {
            // Start Refreshing the task without any delay
            if (this.place != null) {

                return true;
            }
            HTLog.e(TAG, "HyperTrack-Live: Error occurred while shouldRestoreState: Driver is Active & UserPlace is NULL");
        }

        if (actionCompletedListener != null)
            actionCompletedListener.OnCallback();
        return false;
    }


    public boolean isActionLive() {
        return hyperTrackAction != null && !TextUtils.isEmpty(hyperTrackAction.getId());
    }

    private void onActionRefresh() {
        SharedPreferenceManager.setAction(this.hyperTrackAction);

        // Update TaskID if Task updated is not null
        if (this.hyperTrackAction != null && !TextUtils.isEmpty(this.hyperTrackAction.getId())) {
            this.actionID = hyperTrackAction.getId();
            SharedPreferenceManager.setActionID(hyperTrackAction.getId());
        }

        if (this.actionRefreshedListener != null) {
            this.actionRefreshedListener.OnCallback();
        }
    }

    public void getETA(LatLng origin, LatLng destination, String vehicleType, final TaskETACallback callback) {
        String originQueryParam = origin.latitude + "," + origin.longitude;
        String destinationQueryParam = destination.latitude + "," + destination.longitude;

        HyperTrackService sendETAService = HyperTrackServiceGenerator.createService(HyperTrackService.class, SharedPreferenceManager.getUserAuthToken());

        Call<List<TaskETAResponse>> call = sendETAService.getTaskETA(originQueryParam, destinationQueryParam, vehicleType);
        call.enqueue(new Callback<List<TaskETAResponse>>() {
            @Override
            public void onResponse(Call<List<TaskETAResponse>> call, Response<List<TaskETAResponse>> response) {
                List<TaskETAResponse> etaResponses = response.body();

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
            public void onFailure(Call<List<TaskETAResponse>> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void completeAction(final ActionManagerCallback callback) {
        if (TextUtils.isEmpty(this.getHyperTrackActionId())) {
            if (callback != null) {
                callback.OnError();
            }
            return;
        }

        if (this.actionID == null) {
            if (callback != null) {
                callback.OnError();
            }
            return;
        }

        HyperTrack.completeAction(actionID);
        clearState();

       /* if (actionCompletedListener != null)
            actionCompletedListener.OnCallback();*/

        if (callback != null)
            callback.OnSuccess();
    }

    /**
     * Call this method once the task has been completed successfully on the SDK.
     */
    public void clearState() {
        HTLog.i(TAG, "Calling clearState to reset HyperTrack-Live task state");
        this.stopRefreshingAction();
        this.stopGeofencing();
        this.clearListeners();
        this.clearPlace();
        this.clearAction();
        // Remove GeoFencingRequest from SharedPreferences
        SharedPreferenceManager.removeGeofencingRequest();
    }

    private void clearListeners() {
        this.actionCompletedListener = null;
        this.actionRefreshedListener = null;
    }

    public void onActionStart() {
        this.setupGeofencing();
    }

    // Refresh Task with a default delay of REFRESH_DELAY
    public void startRefreshingAction() {
        startRefreshingAction(REFRESH_DELAY);
    }

    public void startRefreshingAction(final long delay) {
        if (handler == null) {
            handler = new Handler();
        } else {
            handler.removeCallbacksAndMessages(refreshAction);
        }

        handler.postDelayed(refreshAction, delay);
    }

    public void stopRefreshingAction() {
        if (this.handler != null) {
            this.handler.removeCallbacks(refreshAction);
            this.handler = null;
        }
    }

    private void stopGeofencing() {
        if (this.mGeofencePendingIntent != null) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleAPIClient, mGeofencePendingIntent);
            mGeofencePendingIntent = null;
        }
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
            Intent geofencingIntent = new Intent(mContext, GeofenceTransitionsIntentService.class);
            mGeofencePendingIntent = PendingIntent.getService(mContext, 0, geofencingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        } catch (SecurityException | IllegalArgumentException exception) {
            Crashlytics.logException(exception);
            HTLog.e(TAG, "Exception for geofence");
        }
    }

    public void OnGeoFenceSuccess() {
        if (actionCompletedListener != null) {
            actionCompletedListener.OnCallback();
            HTLog.i(TAG, "OnGeoFence success: Action completed initiated.");
        } else {
            HTLog.i(TAG, "Action Completed Listener was null.");
        }

       /* if (this.hyperTrackAction == null) {
            this.getSavedActionData();
            if (this.hyperTrackAction == null) {
                if (actionCompletedListener != null) {
                    actionCompletedListener.OnCallback();
                }

                HTLog.e(TAG, "HyperTrack-Live: Error occurred while OnGeoFenceSuccess: HypertrackTask is NULL");
                return;
            }
        }

        HTLog.i(TAG, "OnGeoFence success: Task end initiated.");

        this.completeAction(new ActionManagerCallback() {
            @Override
            public void OnSuccess() {
                if (actionCompletedListener != null) {
                    actionCompletedListener.OnCallback();
                }

                AnalyticsStore.getLogger().autoTripEnded(true, null);
                HTLog.i(TAG, "OnGeoFence success: Task ended (Auto) successfully.");
            }

            @Override
            public void OnError() {
                AnalyticsStore.getLogger().autoTripEnded(false, ErrorMessages.AUTO_END_TRIP_FAILED);
                HTLog.e(TAG, "OnGeoFence success: Task end (Auto) failed.");
            }
        });*/
    }

    private GeofencingRequest getGeofencingRequest() {

        if (this.place == null || this.place.getLocation() == null) {
            HTLog.e(TAG, "Adding Geofence failed: Either place or Lat,Lng is null");
            return null;
        }

        // called when the transition associated with the Geofence is triggered)
        List<Geofence> geoFenceList = new ArrayList<Geofence>();
        geoFenceList.add(new Geofence.Builder()
                .setRequestId(GEOFENCE_REQUEST_ID)
                .setCircularRegion(this.place.getLocation().getLatitude(),
                        this.place.getLocation().getLongitude(),
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

    public void setGeofencingRequest(GeofencingRequest request) {
        this.geofencingRequest = request;
    }

    public UserPlace getPlace() {
        return place;
    }

    public void setPlace(UserPlace place) {
        this.place = place;
        this.savePlace();
    }


    private void clearPlace() {
        this.deletePlace();
        this.place = null;
    }

    public void setActionRefreshedListener(ActionManagerListener listener) {
        this.actionRefreshedListener = listener;
    }

    public void setActionComletedListener(ActionManagerListener listener) {
        this.actionCompletedListener = listener;
    }

    public Action getHyperTrackAction() {
        if (this.hyperTrackAction == null) {
            this.hyperTrackAction = SharedPreferenceManager.getAction(mContext);
        }

        return this.hyperTrackAction;
    }

    public void setHyperTrackAction(Action action) {
        this.hyperTrackAction = action;
        this.actionID = action.getId();
        SharedPreferenceManager.setAction(action);
        SharedPreferenceManager.setActionID(actionID);
    }

    private String getHyperTrackActionId() {
        if (this.actionID == null) {
            this.actionID = SharedPreferenceManager.getActionID(mContext);
        }

        // For Backward compatibility of running trips on app-upgrade
        if (this.actionID == null && getHyperTrackAction() != null) {
            this.actionID = getHyperTrackAction().getId();
            SharedPreferenceManager.setActionID(this.actionID);
        }

        return this.actionID;
    }


    public UserPlace getLastUpdatedDestination() {
        return lastUpdatedDestination;
    }

    public void setLastUpdatedDestination(UserPlace lastUpdatedDestination) {
        this.lastUpdatedDestination = lastUpdatedDestination;
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

    private void clearAction() {
        SharedPreferenceManager.deleteAction();
        SharedPreferenceManager.deleteActionID();
        this.actionID = null;
        this.hyperTrackAction = null;
    }

    private String getFormattedETA() {
        if (this.hyperTrackAction == null || this.hyperTrackAction.getETA() == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        String formattedDate = dateFormat.format(this.hyperTrackAction.getETA());

        formattedDate = formattedDate.toLowerCase();
        formattedDate = formattedDate.replace("a", "A");
        formattedDate = formattedDate.replace("m", "M");
        formattedDate = formattedDate.replace("p", "P");
        formattedDate = formattedDate.replace(".", "");

        return formattedDate;
    }

    public String getShareMessage() {

        if (this.hyperTrackAction == null) {
            HTLog.e(TAG, "Task is null. Not able to get shareMessage");
            return null;
        }

        StringBuilder builder = new StringBuilder("I'm on my way. ");

        String formattedETA = this.getFormattedETA();
        String shareURL = this.hyperTrackAction.getTrackingURL();

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
