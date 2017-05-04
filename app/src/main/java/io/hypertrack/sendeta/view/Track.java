package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.internal.common.util.TextUtils;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.ITrackPresenter;
import io.hypertrack.sendeta.presenter.TrackPresenter;

public class Track extends BaseActivity implements TrackView {

    public static final String KEY_TRACK_DEEPLINK = "track_deeplink";
    public static final String KEY_ACTION_ID_LIST = "action_id_list";
    public static final String KEY_LOOKUP_ID = "lookup_id";

    private Intent intent;
    private Button retryButton;
    private TrackPresenter trackPresenter;

    class TrackMapAdapter extends HyperTrackMapAdapter {
        public TrackMapAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        public boolean showUserInfoForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
            return true;
        }

        @Override
        public boolean showTrailingPolyline() {
            return true;
        }

        @Override
        public boolean showTrafficLayer(HyperTrackMapFragment hyperTrackMapFragment) {
            return false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        trackPresenter = new ITrackPresenter(this);

        initializeUI();

        intent = getIntent();
        if (!processIntentParams(intent)) {
            displayLoader(false);
            finish();
        }
    }

    private void initializeUI() {
        // Initialize HyperTrackMapFragment, adapter and callback
        HyperTrackMapFragment hyperTrackMapFragment = (HyperTrackMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        hyperTrackMapFragment.setHTMapAdapter(new TrackMapAdapter(this));

        // Initialize UI buttons
        retryButton = (Button) findViewById(R.id.retryButton);
    }

    private boolean processIntentParams(Intent intent) {
        // Check if Intent has a valid TASK_ID_LIST extra
        if (intent != null) {
            String lookupId = intent.getStringExtra(KEY_LOOKUP_ID);
            if (!TextUtils.isEmpty(lookupId)) {
                if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {
                    // Add lookupId being tracked by this user
                    trackPresenter.trackAction(lookupId);
                }
                return true;

            } else if (intent.hasExtra(KEY_ACTION_ID_LIST)) {
                List<String> actionIdList = intent.getStringArrayListExtra(KEY_ACTION_ID_LIST);

                // Check if a valid TASK_ID_LIST is available
                if (actionIdList != null && !actionIdList.isEmpty()) {
                    if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {
                        // Add TaskId being tracked by this user
                        trackPresenter.trackAction(actionIdList);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private void showRetryButton(boolean showRetryButton) {
        if (showRetryButton) {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayLoader(true);
                    processIntentParams(intent);
                }
            });
            retryButton.setEnabled(true);
            retryButton.setVisibility(View.VISIBLE);

        } else {
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLoader(boolean toggle) {
        displayLoader(toggle);
    }

    @Override
    public void showTrackingDetail() {
        displayLoader(false);
    }

    @Override
    public void showError() {
        showRetryButton(true);
        displayLoader(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        trackPresenter.removeTrackingAction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        trackPresenter.destroy();
    }
}
