package io.hypertrack.sendeta.view;

import android.content.BroadcastReceiver;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.consumer.model.TaskListCallBack;
import io.hypertrack.lib.consumer.network.HTConsumerClient;
import io.hypertrack.lib.consumer.view.HTMapAdapter;
import io.hypertrack.lib.consumer.view.HTMapFragment;
import io.hypertrack.lib.consumer.view.HTMapFragmentCallback;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AddTaskToTrackDTO;
import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.model.TrackTaskResponse;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.LocationStore;
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

    public static final String KEY_TRACK_DEEPLINK = "track_deeplink";
    public static final String KEY_TASK_ID_LIST = "task_id_list";

    private HTConsumerClient htConsumerClient;
    private HyperTrackMapAdapter htMapAdapter;
    private Set<String> taskIDsToTrack = new HashSet<>();
    private Call<TrackTaskResponse> addTaskForTrackingCall;

    private Intent intent;
    private Button retryButton;

    private BroadcastReceiver mTaskStatusChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null && intent.hasExtra(HTConsumerClient.INTENT_EXTRA_TASK_ID_LIST)) {
                ArrayList<String> taskIDList = intent.getStringArrayListExtra(HTConsumerClient.INTENT_EXTRA_TASK_ID_LIST);

                // Remove Completed Tasks from the map view except in the case of only one task being tracked
                if (taskIDList != null && htConsumerClient != null) {
                    for (String taskID : taskIDList) {

                        HTTask task = htConsumerClient.taskForTaskID(taskID);
                        if (task != null && task.isCompleted()) {
                            if (taskIDsToTrack != null && taskIDsToTrack.size() > 1) {
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

        displayLoader(true);

        // Initialize UI elements
        retryButton = (Button) findViewById(R.id.retryButton);

        // Initialize HyperTrack MapFragment & ConsumerClient
        htConsumerClient = HTConsumerClient.getInstance(Track.this);

        HTMapFragment htMapFragment = (HTMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        htMapAdapter = new HyperTrackMapAdapter(this);
        htMapFragment.setHTMapAdapter(htMapAdapter);
        htMapFragment.setMapFragmentCallback(htMapFragmentCallback);

        intent = getIntent();
        if (processIntentParams())
            return;

        displayLoader(false);

        Toast.makeText(Track.this, R.string.error_tracking_trip_message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean processIntentParams() {
        // Check if Intent has a valid TASK_ID_LIST extra
        if (intent != null && intent.hasExtra(KEY_TASK_ID_LIST)) {

            ArrayList<String> taskIDList = intent.getStringArrayListExtra(KEY_TASK_ID_LIST);

            // Check if a valid TASK_ID_LIST is available
            if (taskIDList != null && !taskIDList.isEmpty()) {

                if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {
                    // Add TaskId being tracked by this user
                    addTaskForTracking(taskIDList);
                } else {

                    // Add all tasks for tracking
                    taskIDsToTrack.addAll(taskIDList);

                    // Fetch TaskDetails for TaskIDList received from UserActivities Screen
                    startTaskTracking();
                }
                return true;
            }
        }

        return false;
    }

    private void startTaskTracking() {

        ArrayList<String> taskIDsBeingTracked = htConsumerClient.getTaskIDList();
        if (taskIDsBeingTracked != null && taskIDsBeingTracked.containsAll(taskIDsToTrack)) {
            // Hide RetryButton & Loader
            showRetryButton(false);
            displayLoader(false);

            htMapAdapter.notifyDataSetChanged();
            return;
        }

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

    private void addTaskForTracking(List<String> taskIDList) {
        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            AddTaskToTrackDTO addTaskToTrackDTO = new AddTaskToTrackDTO(taskIDList.get(0));

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
                    displayLoader(true);
                    processIntentParams();
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
            if (retryButton.getVisibility() != View.VISIBLE) {
                if (!taskIDsToTrack.isEmpty()) {
                    return new ArrayList<>(taskIDsToTrack);
                }

                return super.getTaskIDsToTrack(mapFragment);
            }

            return null;
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
    };

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(HTConsumerClient.TASK_STATUS_CHANGED_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mTaskStatusChangedReceiver, filter);

        // Remove Completed Tasks from the map view except in the case of only one task being tracked
        if (htConsumerClient != null && taskIDsToTrack != null) {
            for (String taskID : taskIDsToTrack) {

                HTTask task = htConsumerClient.taskForTaskID(taskID);
                if (task != null && task.isCompleted()) {

                    if (taskIDsToTrack != null && taskIDsToTrack.size() > 1) {
                        taskIDsToTrack.remove(taskID);
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

    private void resetTrackState() {
        if (addTaskForTrackingCall != null) {
            addTaskForTrackingCall.cancel();
        }
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
