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
import android.text.TextUtils;

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
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.lib.transmitter.model.HTTaskParams;
import io.hypertrack.lib.transmitter.model.HTTaskParamsBuilder;
import io.hypertrack.lib.transmitter.model.ServiceNotificationParams;
import io.hypertrack.lib.transmitter.model.ServiceNotificationParamsBuilder;
import io.hypertrack.lib.transmitter.model.TransmitterConstants;
import io.hypertrack.lib.transmitter.model.callback.HTCompleteTaskStatusCallback;
import io.hypertrack.lib.transmitter.model.callback.HTTaskStatusCallback;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.Task;
import io.hypertrack.sendeta.model.TaskETAResponse;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.service.GeofenceTransitionsIntentService;
import io.hypertrack.sendeta.store.callback.TaskETACallback;
import io.hypertrack.sendeta.store.callback.TaskManagerCallback;
import io.hypertrack.sendeta.store.callback.TaskManagerListener;
import io.hypertrack.sendeta.store.callback.UserStoreGetTaskCallback;
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

    private static final String TAG = TaskManager.class.getSimpleName();
    private static final long REFRESH_DELAY = 30000;
    public static final int LOITERING_DELAY_MS = 30000;
    private static final int NOTIFICATION_RESPONSIVENESS_MS = 5000;
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;
    private static final String GEOFENCE_REQUEST_ID = "io.hypertrack.meta:GeoFence";

    private Context mContext;
    private HTTransmitterService transmitter;

    private int selectedAccountId;
    private HTTask hyperTrackTask;
    private MetaPlace place;
    private HTDriverVehicleType vehicleType = HTDriverVehicleType.CAR;

    private GoogleApiClient mGoogleAPIClient;
    private GeofencingRequest geofencingRequest;
    private PendingIntent mGeofencePendingIntent;
    private boolean addGeofencingRequest;

    private TaskManagerListener taskRefreshedListener, taskCompletedListener;
    private BroadcastReceiver mTaskCompletedReceiver;
    private Handler handler;

    private static TaskManager sharedManager;

    private TaskManager(Context mContext) {
        this.mContext = mContext;
        transmitter = HTTransmitterService.getInstance(mContext);

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
        this.setupGoogleAPIClient();
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
        transmitter.setServiceNotificationParams(notificationParams);
    }

    // Method to get saved TaskData
    private void getSavedTaskData() {
        this.hyperTrackTask = SharedPreferenceManager.getTask(mContext);
        this.place = SharedPreferenceManager.getPlace();
    }

    public boolean shouldRestoreState() {
        this.getSavedTaskData();

        // Check if current Task exists in Shared Preference or not
        if (this.hyperTrackTask != null && this.place != null) {
            // Restore the current task with locally cached data
            // Start Refreshing the task without any delay

            // Added a delay to initiate RestoreTaskStart Call (to account for delay in onMapLoadedCallback)
            onTaskStart(4000);
            return true;

        } else {
            this.clearState();
            return false;
        }
    }

    final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {

            HTTask task = TaskManager.getSharedManager(mContext).getHyperTrackTask();

            if (task == null || TextUtils.isEmpty(task.getId())) {
                return;
            }

            transmitter.refreshTask(task.getId(), new HTTaskStatusCallback() {
                @Override
                public void onSuccess(boolean isOffline, HTTask htTask) {

                    if (htTask != null && !isTaskLive(htTask)) {
                        if (taskCompletedListener != null) {
                            taskCompletedListener.OnCallback();
                        }

                        clearState();
                        return;
                    }

                    hyperTrackTask = htTask;
                    onTaskRefresh();
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

    private boolean isTaskLive(HTTask task) {

        if (task == null || TextUtils.isEmpty(task.getStatus()))
            return false;

        String taskStatus = task.getStatus();

        if (HTTask.TASK_STATUS_DISPATCHING.equalsIgnoreCase(taskStatus)
                || HTTask.TASK_STATUS_DRIVER_ON_THE_WAY.equalsIgnoreCase(taskStatus)
                || HTTask.TASK_STATUS_DRIVER_ARRIVING.equalsIgnoreCase(taskStatus)
                || HTTask.TASK_STATUS_DRIVER_ARRIVED.equalsIgnoreCase(taskStatus)
                || HTTask.TASK_STATUS_COMPLETED.equalsIgnoreCase(taskStatus)) {
            return true;
        }

        return false;
    }

    private void onTaskRefresh() {
        if (this.taskRefreshedListener != null) {
            this.taskRefreshedListener.OnCallback();
        }
    }

    public boolean isTaskActive() {
        return (this.hyperTrackTask != null);
    }

    public void getETA(LatLng origin, LatLng destination, final TaskETACallback callback) {
        String originQueryParam = origin.latitude + "," + origin.longitude;
        String destinationQueryParam = destination.latitude + "," + destination.longitude;

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<List<TaskETAResponse>> call = sendETAService.getTaskETA(originQueryParam, destinationQueryParam);
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

    // TODO: 15/08/16 Check for Meta API Call to addTask
    private void addTask(final String taskID, final TaskManagerCallback callback) {
        HashMap<String, String> taskDetails = new HashMap<>();
        taskDetails.put("hypertrack_task_id", taskID);

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class,
                SharedPreferenceManager.getUserAuthToken());

        Call<HTTask> call = sendETAService.addTask(taskDetails);
        call.enqueue(new Callback<HTTask>() {
            @Override
            public void onResponse(Call<HTTask> call, Response<HTTask> response) {
                if (response.isSuccessful()) {
                    setTask(response.body());
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
            public void onFailure(Call<HTTask> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void startTask(int selectedAccountId, final TaskManagerCallback callback) {
        if (this.place == null || selectedAccountId <= 0) {
            if (callback != null) {
                callback.OnError();
            }

            return;
        }

        this.selectedAccountId = selectedAccountId;

        // Fetch Task Details for selectedPlace & selectedAccountId
        UserStore.sharedStore.getTask(this.place, this.selectedAccountId, new UserStoreGetTaskCallback() {
            @Override
            public void OnSuccess(final String taskID, final String hypertrackDriverID, String publishableKey) {

                // Set PublishableKey fetched for the selectedAccountId
                HyperTrack.setPublishableApiKey(publishableKey, mContext);

                TaskManager.this.addTask(taskID, new TaskManagerCallback() {
                    @Override
                    public void OnSuccess() {
                        // Start Task in TransmitterSDK for fetched taskID & hypertrackDriverID
                        HTTaskParams taskParams = getTaskParams(taskID, hypertrackDriverID);
                        transmitter.startTask(taskParams, new HTTaskStatusCallback() {
                            @Override
                            public void onSuccess(boolean isOffline, HTTask htTask) {

                                if (place == null) {
                                    place = SharedPreferenceManager.getPlace();
                                }

                                onTaskStart();
                                if (callback != null) {
                                    callback.OnSuccess();
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                hyperTrackTask = null;

                                if (callback != null) {
                                    callback.OnError();
                                }
                            }
                        });
                    }

                    @Override
                    public void OnError() {
                        hyperTrackTask = null;

                        if (callback != null) {
                            callback.OnError();
                        }
                    }
                });
            }

            @Override
            public void OnError() {
                hyperTrackTask = null;

                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void startTaskWithTaskID(final int selectedAccountId, final Task task, final TaskManagerCallback callback) {
        if (selectedAccountId <= 0) {
            if (callback != null) {
                callback.OnError();
            }

            return;
        }

        // Set PublishableKey fetched for the selectedAccountId
        this.selectedAccountId = selectedAccountId;
        HyperTrack.setPublishableApiKey(task.getPublishableKey(), mContext);

        TaskManager.this.addTask(task.getId(), new TaskManagerCallback() {
            @Override
            public void OnSuccess() {
                // Start Task in TransmitterSDK
                HTTaskParams taskParams = getTaskParams(task.getId(), task.getDriverId());
                transmitter.startTask(taskParams, new HTTaskStatusCallback() {
                    @Override
                    public void onSuccess(boolean isOffline, HTTask htTask) {

                        if (place == null) {
                            place = SharedPreferenceManager.getPlace();
                        }

                        onTaskStart();
                        if (callback != null) {
                            callback.OnSuccess();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        hyperTrackTask = null;

                        if (callback != null) {
                            callback.OnError();
                        }
                    }
                });
            }

            @Override
            public void OnError() {
                hyperTrackTask = null;

                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void completeTask(final TaskManagerCallback callback) {
        String taskID = this.hyperTrackTask.getId();
        if (taskID == null) {
            if (callback != null) {
                callback.OnSuccess();
            }

            clearState();
        }

        transmitter.completeTask(taskID, new HTCompleteTaskStatusCallback() {
            @Override
            public void onSuccess(String s) {
                if (callback != null)
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

    public void clearState() {
//        this.transmitter.clearCurrentTrip();
        // TODO: 15/08/16 Check what to do for Task similar to clearCurrentTrip()
//        this.transmitter.completeTask(this.hyperTrackTask.getId(), new HTCompleteTaskStatusCallback() {
//            @Override
//            public void onSuccess(String s) {
//                HTLog.i(TAG, "Task completed successfully");
//            }
//
//            @Override
//            public void onError(Exception e) {
//                HTLog.e(TAG, "Error in Task completion: " + e.getMessage());
//            }
//        });

        this.hyperTrackTask = null;
        this.vehicleType = HTDriverVehicleType.CAR;
        this.stopRefreshingTask();
        this.stopGeofencing();
        this.clearListeners();
        this.clearPlace();
        this.clearTask();
        this.unregisterForTaskCompletedBroadcast();
        // Remove GeoFencingRequest from SharedPreferences
        SharedPreferenceManager.removeGeofencingRequest();
    }

    private void clearListeners() {
        this.taskCompletedListener = null;
        this.taskRefreshedListener = null;
    }

    // Start Task with a default delay of REFRESH_DELAY
    private void onTaskStart() {
        onTaskStart(REFRESH_DELAY);
    }

    private void onTaskStart(final long delay) {
        this.setupGeofencing();
        this.startRefreshingTask(delay);
        this.registerForTaskCompletedBroadcast();
    }

    // Refresh Task with a default delay of REFRESH_DELAY
    public void startRefreshingTask() {
        startRefreshingTask(REFRESH_DELAY);
    }

    public void startRefreshingTask(final long delay) {
        if (handler == null) {
            handler = new Handler();
        }

        handler.postDelayed(refreshTask, delay);
    }

    public void stopRefreshingTask() {
        if (this.handler != null) {
            this.handler.removeCallbacksAndMessages(refreshTask);
            this.handler = null;
        }
    }

    private HTTaskParams getTaskParams(String taskID, String hypertrackDriverID) {
        ArrayList<String> taskIDs = new ArrayList<>();
        taskIDs.add(taskID);

        return new HTTaskParamsBuilder()
                .setDriverID(hypertrackDriverID)
                .setTaskID(taskID)
                .setVehicleType(this.vehicleType)
                .createHTTaskParams();
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
        } catch (SecurityException exception) {
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
                clearState();
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
                clearState();

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

    public void setTaskRefreshedListener(TaskManagerListener listener) {
        this.taskRefreshedListener = listener;
    }

    public void setTaskCompletedListener(TaskManagerListener listener) {
        this.taskCompletedListener = listener;
    }

    public HTTask getHyperTrackTask() {
        return this.hyperTrackTask;
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
        SharedPreferenceManager.deleteTask(mContext);
        this.hyperTrackTask = null;
    }

    private void setTask(final HTTask taskToSave) {
        SharedPreferenceManager.setTask(taskToSave);
        this.hyperTrackTask = taskToSave;
    }


    private void registerForTaskCompletedBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TransmitterConstants.HT_ON_TASK_COMPLETED_INTENT);

        mTaskCompletedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (taskCompletedListener != null) {
                    taskCompletedListener.OnCallback();
                }
                clearState();
            }
        };

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mTaskCompletedReceiver, filter);
    }

    private void unregisterForTaskCompletedBroadcast() {
        if (this.mTaskCompletedReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mTaskCompletedReceiver);
            this.mTaskCompletedReceiver = null;
        }
    }

    private String getFormattedETA() {
        if (this.hyperTrackTask == null || this.hyperTrackTask.getETA() == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        String formattedDate = dateFormat.format(this.hyperTrackTask.getETA());

        formattedDate = formattedDate.toLowerCase();
        formattedDate = formattedDate.replace("a", "A");
        formattedDate = formattedDate.replace("m", "M");
        formattedDate = formattedDate.replace("p", "P");
        formattedDate = formattedDate.replace(".", "");

        return formattedDate;
    }

    public String getShareMessage() {

        if (this.hyperTrackTask == null) {
            HTLog.e(TAG, "Task is null. Not able to get shareMessage");
            return null;
        }

        StringBuilder builder = new StringBuilder("I'm on my way. ");

        String formattedETA = this.getFormattedETA();
        String shareURL = this.hyperTrackTask.getTrackingURL();

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
