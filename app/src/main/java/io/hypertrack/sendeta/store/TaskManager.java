package io.hypertrack.sendeta.store;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.ServiceNotificationParams;
import com.hypertrack.lib.models.ServiceNotificationParamsBuilder;

import java.text.SimpleDateFormat;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Task;
import io.hypertrack.sendeta.model.TaskETAResponse;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import io.hypertrack.sendeta.service.GeofenceTransitionsIntentService;
import io.hypertrack.sendeta.store.callback.TaskETACallback;
import io.hypertrack.sendeta.store.callback.TaskManagerCallback;
import io.hypertrack.sendeta.store.callback.TaskManagerListener;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.view.SplashScreen;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 15/08/16.
 */
public class TaskManager implements GoogleApiClient.ConnectionCallbacks {

    public static final int LOITERING_DELAY_MS = 30000;
    private static final String TAG = TaskManager.class.getSimpleName();
    private static final long REFRESH_DELAY = 30000;
    private static final int NOTIFICATION_RESPONSIVENESS_MS = 5000;
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;
    private static final String GEOFENCE_REQUEST_ID = "io.hypertrack.meta:GeoFence";
    private static TaskManager sharedManager;
    //private HTTransmitterService transmitter;
    private Context mContext;
    private int selectedAccountId;
    private String hyperTrackTaskId;
    private Task hyperTrackTask;
    private String actionID;
    private Action action;
    private Place lastUpdatedDestination = null;
    private Place place;
    private HTUserVehicleType vehicleType = HTUserVehicleType.CAR;
    private GoogleApiClient mGoogleAPIClient;
    private GeofencingRequest geofencingRequest;
    private PendingIntent mGeofencePendingIntent;
    private boolean addGeofencingRequest;
    private TaskManagerListener taskRefreshedListener, taskCompletedListener;
    private BroadcastReceiver mDriverNotLiveBroadcastReceiver;
    private Handler handler;


    private TaskManager(Context mContext) {
        this.mContext = mContext;
//        transmitter = HTTransmitterService.getInstance(mContext);

        initializeTaskManager();
    }

    public static TaskManager getSharedManager(Context context) {
        if (sharedManager == null) {
            sharedManager = new TaskManager(context);
        }

        return sharedManager;
    }

    // Method to initialize TaskManager instance
    private void initializeTaskManager() {
      //  this.setupGoogleAPIClient();
        this.setServiceNotification();
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

    // Method to set TransmitterSDK ServiceNotification
    private void setServiceNotification() {
        //Customize Notification Settings
        ServiceNotificationParamsBuilder builder = new ServiceNotificationParamsBuilder();
        ServiceNotificationParams notificationParams = builder
                .setSmallIconBGColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .setContentIntentActivityClass(SplashScreen.class)
                .build();
     //   transmitter.setServiceNotificationParams(notificationParams);
    }

    // Method to get saved TaskData
    private void getSavedTaskData() {
        this.hyperTrackTask = SharedPreferenceManager.getTask(mContext);
        this.hyperTrackTaskId = SharedPreferenceManager.getTaskID(mContext);
      //  this.place = SharedPreferenceManager.getPlace();
    }

    private void getSavedActionData(){
        this.action = SharedPreferenceManager.getAction(mContext);
        this.actionID = SharedPreferenceManager.getActionID(mContext);
        this.place = SharedPreferenceManager.getActionPlace();
    }

  /*  public boolean shouldRestoreState() {
       *//* if (transmitter == null)
            transmitter = HTTransmitterService.getInstance(mContext);*//*

        // Restore the current task with locally cached data
        this.getSavedTaskData();

        // Check if current Task exists in Shared Preference or not
        if (this.hyperTrackTask != null) {
            // Start Refreshing the task without any delay
            if (this.place != null) {
                onTaskStart(0);
                return true;
            }
            HTLog.e(TAG, "SendETA: Error occurred while shouldRestoreState: Driver is Active & Place is NULL");
        }

        completeTask(null);
        return false;
    }*/

  /*  final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {

            Task task = TaskManager.getSharedManager(mContext).getHyperTrackTask();
            if (!isTaskLive(task))
                return;

            transmitter.refreshTask(task.getId(), new TaskStatusCallback() {
                @Override
                public void onSuccess(Task Task) {
                    if (!isTaskLive(Task)) {
                        HTLog.i(TAG, "SendETA Task Not Live, Calling completeTask() to complete the task");

                        // Call completeTask when the Task object is null or task is not live
                        TaskManager.this.completeTask(new TaskManagerCallback() {
                            @Override
                            public void OnSuccess() {
                                if (taskCompletedListener != null) {
                                    taskCompletedListener.OnCallback();
                                }
                            }

                            @Override
                            public void OnError() {
                            }
                        });
                        return;
                    }

                    hyperTrackTask = Task;
                    onTaskRefresh();
                }

                @Override
                public void onOfflineSuccess() {
                    // Do nothing
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
    };*/

    private boolean isTaskLive(Task task) {
        return task != null && !TextUtils.isEmpty(task.getId());
    }

    private void onTaskRefresh() {
        SharedPreferenceManager.setTask(this.hyperTrackTask);

        // Update TaskID if Task updated is not null
        if (this.hyperTrackTask != null && !TextUtils.isEmpty(this.hyperTrackTask.getId())) {
            this.hyperTrackTaskId = hyperTrackTask.getId();
            SharedPreferenceManager.setTaskID(hyperTrackTask.getId());
        }

        if (this.taskRefreshedListener != null) {
            this.taskRefreshedListener.OnCallback();
        }
    }

    public boolean isTaskActive() {
        return isTaskLive(this.getHyperTrackTask());
    }

    public void getETA(LatLng origin, LatLng destination,String vehicleType, final TaskETACallback callback) {
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

   /* public void startTask(final String taskID, LatLng location, int selectedAccountId, HTUserVehicleType vehicleType,
                          final TaskManagerCallback callback) {
        if (this.place == null || selectedAccountId <= 0) {
            callback.OnError();
            return;
        }

        this.selectedAccountId = selectedAccountId;

        final ExpandedLocation startLocation = new ExpandedLocation();

        this.startTaskOnServer(taskID, startLocation, vehicleType, callback);
    }

    private void startTaskOnServer(final String taskID, final HyperTrackLocation startLocation, final HTUserVehicleType vehicleType,
                                   final TaskManagerCallback callback) {

        this.vehicleType = vehicleType;

        UserStore.sharedStore.startTaskOnServer(taskID, this.place, this.selectedAccountId,
                startLocation, vehicleType, new UserStoreGetTaskCallback() {
                    @Override
                    public void OnSuccess(final Map<String, Object> response) {
                        final String hypertrackDriverID = (String) response.get("hypertrack_driver_id");

                        // Set HyperTrack DriverID
                        if (!TextUtils.isEmpty(hypertrackDriverID)) {
                            SharedPreferenceManager.setHyperTrackDriverID(mContext, hypertrackDriverID);
                        }

                        // Parse Response to fetch Task Data
                        if (TaskManager.this.setTask(response)) {

                            // Set lastUpdatedDestination
                            Place destination = new Place();
                            destination.setId((String) response.get("destination_id"));
                            TaskManager.this.setLastUpdatedDestination(destination);

                            if (place == null) {
                                place = SharedPreferenceManager.getPlace();
                            }

                            onTaskStart();
                            callback.OnSuccess();
                            return;
                        }

                        callback.OnError();
                    }

                    @Override
                    public void OnError() {
                        hyperTrackTask = null;
                        callback.OnError();
                    }
                });
    }*/

    public void completeTask(final TaskManagerCallback callback) {
        if (TextUtils.isEmpty(this.getHyperTrackTaskId())) {
            if (callback != null) {
                callback.OnError();
            }
            return;
        }

        String taskID = this.hyperTrackTaskId;
        if (taskID == null) {
            if (callback != null) {
                callback.OnError();
            }
            return;
        }

       /* transmitter.completeTask(taskID, new HTCompleteTaskStatusCallback() {
            @Override
            public void onSuccess(String s) {
                if (callback != null)
                    callback.OnSuccess();

                clearState();
            }

            @Override
            public void onOfflineSuccess() {
                if (callback != null)
                    callback.OnSuccess();

                clearState();

                // Call endTrip when Task was completed offline
                // to end the service offline
                endTrip();
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });*/
    }



   /* public void endTrip() {
        transmitter.endTrip(new HTTripStatusCallback() {
            @Override
            public void onSuccess(HTTrip trip) {
                // do nothing
            }

            @Override
            public void onError(Exception exception) {
                // do nothing
            }

            @Override
            public void onOfflineSuccess() {
                // do nothing
            }
        });
    }
*/
    /**
     * Call this method once the task has been completed successfully on the SDK.
     */
    public void clearState() {
        HTLog.i(TAG, "Calling clearState to reset SendETA task state");
        this.vehicleType = HTUserVehicleType.CAR;
       // this.stopRefreshingTask();
        this.stopGeofencing();
        this.clearListeners();
        this.clearPlace();
        this.clearTask();
       // this.unregisterForDriverNotLiveBroadcast();
        // Remove GeoFencingRequest from SharedPreferences
        SharedPreferenceManager.removeGeofencingRequest();
    }

    private void clearListeners() {
        this.taskCompletedListener = null;
        this.taskRefreshedListener = null;
    }

    // Start Task with a default delay of REFRESH_DELAY
  /*  private void onTaskStart() {
        onTaskStart(REFRESH_DELAY);
    }
*/
  /*  private void onTaskStart(final long delay) {
      //  this.setupGeofencing();
        this.startRefreshingTask();
        this.registerForDriverNotLiveBroadcast();
    }*/

    // Refresh Task with a default delay of REFRESH_DELAY
   /* public void startRefreshingTask() {
        startRefreshingTask();
    }*/

   /* public void startRefreshingTask(final long delay) {
        if (handler == null) {
            handler = new Handler();
        } else {
            if (refreshTask != null)
                handler.removeCallbacksAndMessages(refreshTask);
        }

        handler.postDelayed(refreshTask, delay);
    }
*/
    /*public void stopRefreshingTask() {
        if (this.handler != null) {
            this.handler.removeCallbacksAndMessages(refreshTask);
            this.handler = null;
        }
    }*/

    private void stopGeofencing() {
        if (this.mGeofencePendingIntent != null) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleAPIClient, mGeofencePendingIntent);
            mGeofencePendingIntent = null;
        }
    }

    public void setGeofencingRequest(GeofencingRequest request) {
        this.geofencingRequest = request;
    }

    /*public void setupGeofencing() {
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
*/
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
        if (this.hyperTrackTask == null) {
            this.getSavedTaskData();
            if (this.hyperTrackTask == null) {
                if (taskCompletedListener != null) {
                    taskCompletedListener.OnCallback();
                }

                HTLog.e(TAG, "SendETA: Error occurred while OnGeoFenceSuccess: HypertrackTask is NULL");
                return;
            }
        }

        HTLog.i(TAG, "OnGeoFence success: Task end initiated.");

        this.completeTask(new TaskManagerCallback() {
            @Override
            public void OnSuccess() {
                if (taskCompletedListener != null) {
                    taskCompletedListener.OnCallback();
                }

                AnalyticsStore.getLogger().autoTripEnded(true, null);
                HTLog.i(TAG, "OnGeoFence success: Task ended (Auto) successfully.");
            }

            @Override
            public void OnError() {
                AnalyticsStore.getLogger().autoTripEnded(false, ErrorMessages.AUTO_END_TRIP_FAILED);
                HTLog.e(TAG, "OnGeoFence success: Task end (Auto) failed.");
            }
        });
    }

   /* private GeofencingRequest getGeofencingRequest() {

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
    }*/

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
        this.savePlace();
    }


    private void clearPlace() {
        this.deletePlace();
        this.place = null;
    }

    public void setTaskRefreshedListener(TaskManagerListener listener) {
        this.taskRefreshedListener = listener;
    }

    public void setTaskCompletedListener(TaskManagerListener listener) {
        this.taskCompletedListener = listener;
    }


    public Task getHyperTrackTask() {
        if (this.hyperTrackTask == null) {
            this.hyperTrackTask = SharedPreferenceManager.getTask(mContext);
        }

        return this.hyperTrackTask;
    }

    public Action getHyperTrackAction() {
        if (this.action == null) {
            this.action = SharedPreferenceManager.getAction(mContext);
        }

        return this.action;
    }

    public void setHyperTrackAction(Action action) {
        this.action = action;
        this.actionID = action.getId();
    }

    public String getHyperTrackTaskId() {
        if (this.hyperTrackTaskId == null) {
            this.hyperTrackTaskId = SharedPreferenceManager.getTaskID(mContext);
        }

        // For Backward compatibility of running trips on app-upgrade
        if (this.hyperTrackTaskId == null && getHyperTrackTask() != null) {
            this.hyperTrackTaskId = getHyperTrackTask().getId();
            SharedPreferenceManager.setTaskID(this.hyperTrackTaskId);
        }

        return this.hyperTrackTaskId;
    }

    public Place getLastUpdatedDestination() {
        return lastUpdatedDestination;
    }

    public void setLastUpdatedDestination(Place lastUpdatedDestination) {
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

    private void clearTask() {
        SharedPreferenceManager.deleteTask();
        SharedPreferenceManager.deleteTaskID();
        this.hyperTrackTask = null;
        this.hyperTrackTaskId = null;
    }




   /* private void registerForDriverNotLiveBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TransmitterConstants.HT_ON_DRIVER_NOT_ACTIVE_INTENT);

        mDriverNotLiveBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (taskCompletedListener != null) {
                    taskCompletedListener.OnCallback();
                }
                completeTask(null);
                clearState();
            }
        };

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDriverNotLiveBroadcastReceiver, filter);
    }

    private void unregisterForDriverNotLiveBroadcast() {
        if (this.mDriverNotLiveBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDriverNotLiveBroadcastReceiver);
            this.mDriverNotLiveBroadcastReceiver = null;
        }
    }*/

    private String getFormattedETA() {
        if (this.action == null || this.action.getETA() == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        String formattedDate = dateFormat.format(this.action.getETA());

        formattedDate = formattedDate.toLowerCase();
        formattedDate = formattedDate.replace("a", "A");
        formattedDate = formattedDate.replace("m", "M");
        formattedDate = formattedDate.replace("p", "P");
        formattedDate = formattedDate.replace(".", "");

        return formattedDate;
    }

    public String getShareMessage() {

        if (this.action == null) {
            HTLog.e(TAG, "Task is null. Not able to get shareMessage");
            return null;
        }

        StringBuilder builder = new StringBuilder("I'm on my way. ");

        String formattedETA = this.getFormattedETA();
        String shareURL = this.action.getTrackingURL();

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
