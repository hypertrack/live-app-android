package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.consumer.model.TaskListCallBack;
import io.hypertrack.lib.consumer.network.HTConsumerClient;
import io.hypertrack.lib.consumer.view.HTMapAdapter;
import io.hypertrack.lib.consumer.view.HTMapFragment;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AddTaskToTrackDTO;
import io.hypertrack.sendeta.model.TrackTaskResponse;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.UserStore;
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
    private ArrayList<String> taskIDList = new ArrayList<>();
    private Call<TrackTaskResponse> addTaskForTrackingCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track);

        // Initialize UI elements
        retryButton = (Button) findViewById(R.id.retryButton);

        // Set HyperTrack PublishableKey
//        HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, getApplicationContext());

        // Initialize HyperTrack MapFragment & ConsumerClient
        HTMapFragment htMapFragment = (HTMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        htMapAdapter = new HyperTrackMapAdapter(this);
        htMapFragment.setHTMapAdapter(htMapAdapter);
        htConsumerClient = HTConsumerClient.getInstance(Track.this);

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
                taskIDList = intent.getStringArrayListExtra(KEY_TASK_ID_LIST);

                // Check if a valid TASK_ID_LIST is available
                if (taskIDList != null && !taskIDList.isEmpty()) {

                    // Fetch TaskDetails for TaskIDList received from Activities Screen
                    startTaskTracking();
                    return;
                }
            }
        }

        Toast.makeText(Track.this, "ERROR: No Tasks to be tracked", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startTaskTracking() {
        htConsumerClient.trackTask(taskIDList, this, new TaskListCallBack() {
            @Override
            public void onSuccess(List<HTTask> list) {
                // Hide RetryButton
                showRetryButton(false);

                Toast.makeText(Track.this, "Tasks tracking initiated", Toast.LENGTH_SHORT).show();

                htMapAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // Show RetryButton
                showRetryButton(true);

                Toast.makeText(Track.this, "Error while tracking the task. Please try again.", Toast.LENGTH_SHORT).show();
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

                            ArrayList<String> groupTaskIDList = trackTaskResponse.getGroupTaskIDList();
                            if (groupTaskIDList != null && !groupTaskIDList.isEmpty()) {
                                taskIDList.addAll(groupTaskIDList);
                            }

                            if (!TextUtils.isEmpty(trackTaskResponse.getPublishableKey())) {
                                // Set HyperTrack PublishableKey
                                HyperTrack.setPublishableApiKey(trackTaskResponse.getPublishableKey(), getApplicationContext());
                            }
                        }

                        if (taskIDList != null && !taskIDList.isEmpty()) {
                            startTaskTracking();
                            return;
                        }
                    }

                    // Show RetryButton
                    showRetryButton(true);
                }

                @Override
                public void onFailure(Call<TrackTaskResponse> call, Throwable t) {
                    // Show RetryButton
                    showRetryButton(true);
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

                    } else if (taskIDList != null && !taskIDList.isEmpty()) {
                        // Fetch TaskDetails for TaskIDList received from Activities Screen
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

    @Override
    protected void onDestroy() {
        // Clear HyperTrack Tasks being tracked currently
        HTConsumerClient.getInstance(Track.this).clearTasks();

        if (addTaskForTrackingCall != null) {
            addTaskForTrackingCall.cancel();
        }

        super.onDestroy();
    }

    private class HyperTrackMapAdapter extends HTMapAdapter {

        public HyperTrackMapAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        public List<String> getTaskIDsToTrack(HTMapFragment mapFragment) {
            if (taskIDList != null && !taskIDList.isEmpty()) {
                return taskIDList;
            }

            return super.getTaskIDsToTrack(mapFragment);
        }

        @Override
        public String getOrderStatusToolbarDefaultTitle(HTMapFragment mapFragment) {
            return getResources().getString(R.string.app_name);
        }
    }
}
