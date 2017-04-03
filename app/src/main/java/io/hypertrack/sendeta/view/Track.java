package io.hypertrack.sendeta.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.util.SharedPreferenceManager;


public class Track extends BaseActivity {

    public static final String KEY_TRACK_DEEPLINK = "track_deeplink";
    public static final String KEY_TASK_ID_LIST = "task_id_list";

    private Button retryButton;

    private HyperTrackMapFragment hyperTrackMapFragment;
    private MyMapAdapter mapAdapter;
    private Intent intent;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        //Intialize Map Fragment
        hyperTrackMapFragment = (HyperTrackMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        displayLoader(true);

        // Initialize UI elements
        retryButton = (Button) findViewById(R.id.retryButton);

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

            ArrayList<String> actionIDList = intent.getStringArrayListExtra(KEY_TASK_ID_LIST);

            // Check if a valid TASK_ID_LIST is available
            if (actionIDList != null && !actionIDList.isEmpty()) {

                if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {

                    //Remove any previous action if currently being tracked.
                    if (!TextUtils.isEmpty(SharedPreferenceManager.getCurrentTrackingAction())) {
                        HyperTrack.removeActions(new ArrayList<String>() {{
                            add(SharedPreferenceManager.getCurrentTrackingAction());
                        }});
                    }

                    //Set the current tracking action
                    SharedPreferenceManager.setCurrentTrackingAction(actionIDList.get(0));

                    // Add TaskId being tracked by this user
                    addTaskForTracking(actionIDList);
                }
                return true;
            }
        }
        return false;
    }

    private void addTaskForTracking(List<String> actionsIDList) {
        displayLoader(true);

        HyperTrack.trackActionsForUser(actionsIDList, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                mapAdapter = new MyMapAdapter(Track.this);
                hyperTrackMapFragment.setHTMapAdapter(mapAdapter);
                displayLoader(false);
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                displayLoader(false);
                Toast.makeText(Track.this, "Error Occured", Toast.LENGTH_LONG).show();
                showRetryButton(true);
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class MyMapAdapter extends HyperTrackMapAdapter {
        private Context mContext;

        public MyMapAdapter(Context mContext) {
            super(mContext);
            this.mContext = mContext;
        }

        @Override
        public boolean showHeroMarkerForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
            return true;
        }
    }
}
