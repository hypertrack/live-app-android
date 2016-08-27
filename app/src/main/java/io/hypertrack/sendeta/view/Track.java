package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.consumer.model.TaskListCallBack;
import io.hypertrack.lib.consumer.network.HTConsumerClient;
import io.hypertrack.lib.consumer.view.HTMapAdapter;
import io.hypertrack.lib.consumer.view.HTMapFragment;
import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 27/08/16.
 */
public class Track extends BaseActivity {

    public static final String KEY_TASK_ID_LIST = "task_id_list";
    private ArrayList<String> taskIDList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track);

        // Set HyperTrack PublishableKey
        HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, getApplicationContext());

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(KEY_TASK_ID_LIST)) {
            Toast.makeText(Track.this, "ERROR: No Tasks to be tracked", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taskIDList = intent.getStringArrayListExtra(KEY_TASK_ID_LIST);
        if (taskIDList == null || taskIDList.isEmpty()) {
            Toast.makeText(Track.this, "ERROR: No Tasks to be tracked", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        HTMapFragment htMapFragment = (HTMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        HyperTrackMapAdapter htMapAdapter = new HyperTrackMapAdapter(this);
        htMapFragment.setHTMapAdapter(htMapAdapter);
        HTConsumerClient htConsumerClient = HTConsumerClient.getInstance(Track.this);

        htConsumerClient.trackTask(taskIDList, this, new TaskListCallBack() {
            @Override
            public void onSuccess(List<HTTask> list) {
                Toast.makeText(Track.this, "Tasks tracking initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(Track.this, "Error while tracking the task. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Clear HyperTrack Tasks being tracked currently
        HTConsumerClient.getInstance(Track.this).clearTasks();
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
    }
}
