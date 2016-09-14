package io.hypertrack.sendeta.view;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.consumer.model.TaskListCallBack;
import io.hypertrack.lib.consumer.network.HTConsumerClient;
import io.hypertrack.lib.consumer.view.HTMapAdapter;
import io.hypertrack.lib.consumer.view.HTMapFragment;
import io.hypertrack.lib.consumer.view.HTMapFragmentCallback;
import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AddTaskToTrackDTO;
import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.model.TrackTaskResponse;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.TaskManager;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 27/08/16.
 */
public class Track extends BaseActivity {

    public static final String KEY_TASK_ID_LIST = "task_id_list";
    public static final String KEY_SHORT_CODE = "short_code";

    private Button retryButton;

    private HTConsumerClient htConsumerClient;
    private HyperTrackMapAdapter htMapAdapter;

    private String shortCode;
    private Set<String> taskIDsToTrack = new HashSet<>();
    private Call<TrackTaskResponse> addTaskForTrackingCall;

    private String currentPublishableKey;

    private BroadcastReceiver mTaskStatusChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null && intent.hasExtra(HTConsumerClient.INTENT_EXTRA_TASK_ID_LIST)) {
                ArrayList<String> taskIDList = intent.getStringArrayListExtra(HTConsumerClient.INTENT_EXTRA_TASK_ID_LIST);

                if (taskIDList != null) {

                    for (String taskID : taskIDList) {
                        if (htConsumerClient != null && htConsumerClient.taskForTaskID(taskID) != null
                                && htConsumerClient.taskForTaskID(taskID).isCompleted()) {

                            htConsumerClient.removeTaskID(taskID);
                            if (taskIDsToTrack != null && taskIDsToTrack.contains(taskID)) {
                                taskIDsToTrack.remove(taskID);
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track);

        currentPublishableKey = HyperTrack.getPublishableKey(this);
        // Check if Current Selected key is for a Business Account
        if (!currentPublishableKey.equalsIgnoreCase(BuildConfig.API_KEY)) {

            // Check if a business trip is active and show an error
            if (TaskManager.getSharedManager(this).isTaskActive()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.error_tracking_while_on_business_trip);
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
                return;

            } else {

                // Change Publishable Key to SendETA Personal Account
                HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, getApplicationContext());
            }
        }

        displayLoader(true);

        // Initialize UI elements
        retryButton = (Button) findViewById(R.id.retryButton);

        // Initialize HyperTrack MapFragment & ConsumerClient
        htConsumerClient = HTConsumerClient.getInstance(Track.this);
        htConsumerClient.clearTasks();

        HTMapFragment htMapFragment = (HTMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        htMapAdapter = new HyperTrackMapAdapter(this);
        htMapFragment.setHTMapAdapter(htMapAdapter);
        htMapFragment.setMapFragmentCallback(htMapFragmentCallback);

        Intent intent = getIntent();
        if (intent != null) {
            // Check if Intent has a valid SHORT_CODE extra
            if (intent.hasExtra(KEY_SHORT_CODE)) {
                shortCode = intent.getStringExtra(KEY_SHORT_CODE);

                // Check if a valid SHORT_CODE is available
                if (!TextUtils.isEmpty(shortCode)) {

                    // Fetch TaskDetails for shortCode parsed from eta.fyi link
                    addTaskForTracking();
                    return;
                }

                // Check if Intent has a valid TASK_ID_LIST extra
            } else if (intent.hasExtra(KEY_TASK_ID_LIST)) {

                ArrayList<String> taskIDList = intent.getStringArrayListExtra(KEY_TASK_ID_LIST);
                if (taskIDList != null && !taskIDList.isEmpty())
                    taskIDsToTrack.addAll(taskIDList);

                // Check if a valid TASK_ID_LIST is available
                if (!taskIDsToTrack.isEmpty()) {
                    // Fetch TaskDetails for TaskIDList received from UserActivities Screen
                    startTaskTracking();
                    return;
                }
            }
        }

        displayLoader(false);

        Toast.makeText(Track.this, R.string.error_tracking_trip_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startTaskTracking() {
        htConsumerClient.trackTask(new ArrayList<>(taskIDsToTrack), this, new TaskListCallBack() {
            @Override
            public void onSuccess(List<HTTask> list) {
                if (Track.this.isFinishing()) {
                    return;
                }

                // Hide RetryButton & Loader
                showRetryButton(false);
                displayLoader(false);

                htMapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (Track.this.isFinishing()) {
                    return;
                }

                // Show RetryButton
                showRetryButton(true);
                displayLoader(false);

                Toast.makeText(Track.this, R.string.error_tracking_trip_message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTaskForTracking() {
        User user = UserStore.sharedStore.getUser();
        if (user != null) {

            AddTaskToTrackDTO addTaskToTrackDTO = new AddTaskToTrackDTO(shortCode);

            SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class,
                    SharedPreferenceManager.getUserAuthToken());
            addTaskForTrackingCall = sendETAService.addTaskForTracking(user.getId(), addTaskToTrackDTO);
            addTaskForTrackingCall.enqueue(new Callback<TrackTaskResponse>() {
                @Override
                public void onResponse(Call<TrackTaskResponse> call, Response<TrackTaskResponse> response) {
                    if (response.isSuccessful()) {

                        TrackTaskResponse trackTaskResponse = response.body();
                        if (trackTaskResponse != null) {

                            String taskID = trackTaskResponse.getTaskID();
//                            if (!TextUtils.isEmpty(taskID)) {
//                                taskIDsToTrack.add(taskID);
//                            }

                            ArrayList<String> activeTaskIDList = trackTaskResponse.getActiveTaskIDList();
                            // Check if clicked taskID exists in activeTaskIDList or not
                            if (!TextUtils.isEmpty(taskID) && (activeTaskIDList == null || activeTaskIDList.isEmpty() || !activeTaskIDList.contains(taskID))) {
                                taskIDsToTrack.add(taskID);
                            } else if (activeTaskIDList != null && !activeTaskIDList.isEmpty()) {
                                taskIDsToTrack.addAll(activeTaskIDList);
                            }
                        }

                        // Successful case where valid taskIDs were received
                        if (!taskIDsToTrack.isEmpty()) {
                            Toast.makeText(Track.this, R.string.fetching_trip_info_message, Toast.LENGTH_SHORT).show();

                            startTaskTracking();

                            // Hide RetryButton
                            showRetryButton(false);
                            return;
                        }
                    }

                    if (Track.this.isFinishing()) {
                        return;
                    }

                    // Show RetryButton
                    showRetryButton(true);
                    displayLoader(false);

                    Toast.makeText(Track.this, R.string.error_fetching_trip_info_message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<TrackTaskResponse> call, Throwable t) {
                    ErrorData errorData = new ErrorData();
                    try {
                        errorData = NetworkUtils.processFailure(t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showErrorMessage(errorData);
                }

                private void showErrorMessage(ErrorData errorData) {
                    if (Track.this.isFinishing()) {
                        return;
                    }

                    // Show RetryButton
                    showRetryButton(true);
                    displayLoader(false);

                    if (ErrorCodes.NO_INTERNET.equalsIgnoreCase(errorData.getCode()) ||
                            ErrorCodes.REQUEST_TIMED_OUT.equalsIgnoreCase(errorData.getCode())) {
                        Toast.makeText(Track.this, R.string.network_issue, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Track.this, R.string.generic_error_message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showRetryButton(boolean showRetryButton) {
        if (showRetryButton) {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(shortCode)) {
                        // Fetch TaskDetails for shortCode parsed from eta.fyi link
                        addTaskForTracking();
                        retryButton.setEnabled(false);

                    } else if (!taskIDsToTrack.isEmpty()) {
                        // Fetch TaskDetails for TaskIDList received from UserActivities Screen
                        startTaskTracking();
                        retryButton.setEnabled(false);
                    }
                }
            });
            retryButton.setEnabled(true);
            retryButton.setVisibility(View.VISIBLE);

        } else {
            retryButton.setVisibility(View.GONE);
        }
    }

    private class HyperTrackMapAdapter extends HTMapAdapter {

        public HyperTrackMapAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        public List<String> getTaskIDsToTrack(HTMapFragment mapFragment) {
            if (!taskIDsToTrack.isEmpty()) {
                return new ArrayList<>(taskIDsToTrack);
            }

            return super.getTaskIDsToTrack(mapFragment);
        }

        @Override
        public String getOrderStatusToolbarDefaultTitle(HTMapFragment mapFragment) {
            return getResources().getString(R.string.app_name);
        }
    }

    private HTMapFragmentCallback htMapFragmentCallback = new HTMapFragmentCallback() {
        @Override
        public void onMapReadyCallback(HTMapFragment mapFragment, GoogleMap map) {
            if (map == null)
                return;

            // Set Default View for map according to User's LastKnownLocation
            Location lastKnownCachedLocation = LocationStore.sharedStore().getLastKnownUserLocation();
            if (lastKnownCachedLocation != null && lastKnownCachedLocation.getLatitude() != 0.0
                    && lastKnownCachedLocation.getLongitude() != 0.0) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownCachedLocation.getLatitude(),
                        lastKnownCachedLocation.getLongitude()), 13.0f));
            }
        }

        @Override
        public void onMapLoadedCallback(HTMapFragment mapFragment, GoogleMap map) {
        }

        @Override
        public void onMapWillClear(HTMapFragment mapFragment, GoogleMap map) {
        }

        @Override
        public void onHeroMarkerClicked(HTMapFragment mapFragment, String taskID, Marker heroMarker) {
        }

        @Override
        public void onHeroMarkerWillMove(HTMapFragment mapFragment, String taskID, Marker heroMarker, LatLng toLocation) {
        }

        @Override
        public void onHeroMarkerAdded(HTMapFragment mapFragment, String taskID, Marker heroMarker) {
        }

        @Override
        public void onHeroMarkerRemoved(HTMapFragment mapFragment, String taskID, Marker heroMarker) {
        }

        @Override
        public void onSourceMarkerClicked(HTMapFragment mapFragment, String taskID, Marker sourceMarker) {
        }

        @Override
        public void onSourceMarkerAdded(HTMapFragment mapFragment, String taskID, Marker sourceMarker) {
        }

        @Override
        public void onSourceMarkerRemoved(HTMapFragment mapFragment, String taskID, Marker sourceMarker) {
        }

        @Override
        public void onDestinationMarkerClicked(HTMapFragment mapFragment, String taskID, Marker destinationMarker) {
        }

        @Override
        public void onDestinationMarkerAdded(HTMapFragment mapFragment, String taskID, Marker destinationMarker) {
        }

        @Override
        public void onDestinationMarkerRemoved(HTMapFragment mapFragment, String taskID, Marker destinationMarker) {
        }

        @Override
        public void onMultipleTasksDestinationMarkerClicked(HTMapFragment mapFragment, Marker destinationMarker) {
        }

        @Override
        public void onMultipleTasksDestinationMarkerAdded(HTMapFragment mapFragment, Marker destinationMarker) {
        }

        @Override
        public void onMultipleTasksDestinationMarkerRemoved(HTMapFragment mapFragment, Marker destinationMarker) {
        }

        @Override
        public void onCallButtonClicked(HTMapFragment mapFragment, String taskID) {
        }

        @Override
        public void onOrderDetailsButtonClicked(HTMapFragment mapFragment, String taskID) {
        }

        @Override
        public void onMapFragmentSucceed(HTMapFragment mapFragment, List<String> taskID) {
        }

        @Override
        public void onMapFragmentFailed(HTMapFragment mapFragment, List<String> taskID, String errorMessage) {
        }

        @Override
        public void onBeginEditingDestination(HTMapFragment mapFragment, String taskID) {
        }

        @Override
        public void onCanceledEditingDestination(HTMapFragment mapFragment, String taskID) {
        }

        @Override
        public void onEndEditingDestination(HTMapFragment mapFragment, String taskID) {
        }

        @Override
        public void onReceiveUserLocationMissingError(HTMapFragment mapFragment, String taskID, String errorMessage) {
        }

        @Override
        public void onReceiveEditDestinationError(HTMapFragment mapFragment, String taskID, String errorMessage) {
        }
    };

    private void resetTrackState() {
        // Clear HyperTrack Tasks being tracked currently
        HTConsumerClient.getInstance(Track.this).clearTasks();

        // Reset Publishable Key if applicable
        String publishableKey = HyperTrack.getPublishableKey(getApplicationContext());
        if (!publishableKey.equalsIgnoreCase(currentPublishableKey)) {
            HyperTrack.setPublishableApiKey(currentPublishableKey, getApplicationContext());
        }

        if (addTaskForTrackingCall != null) {
            addTaskForTrackingCall.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(HTConsumerClient.TASK_STATUS_CHANGED_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mTaskStatusChangedReceiver, filter);

        if (htConsumerClient != null) {

            ArrayList<String> taskIDList = htConsumerClient.getTaskIDList();
            if (taskIDList != null && !taskIDList.isEmpty()) {

                for (String taskID : taskIDList) {
                    if (htConsumerClient.taskForTaskID(taskID) != null && htConsumerClient.taskForTaskID(taskID).isCompleted()) {
                        htConsumerClient.removeTaskID(taskID);
                        if (taskIDsToTrack != null && taskIDsToTrack.contains(taskID)) {
                            taskIDsToTrack.remove(taskID);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTaskStatusChangedReceiver);
    }

    @Override
    protected void onDestroy() {
        resetTrackState();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        resetTrackState();

        super.onBackPressed();
    }
}
