package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.internal.common.util.TextUtils;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.ITrackPresenter;
import io.hypertrack.sendeta.presenter.TrackPresenter;
import io.hypertrack.sendeta.store.SharedPreferenceManager;

public class Track extends BaseActivity implements TrackView {

    public static final String KEY_TRACK_DEEPLINK = "track_deeplink";
    public static final String KEY_ACTION_ID_LIST = "action_id_list";
    public static final String KEY_LOOKUP_ID = "lookup_id";

    private Intent intent;
    private Button retryButton;
    private TrackPresenter trackPresenter;

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

    /**
     * Implementation for HyperTrackMapAdapter specifying the UI customizations for Live-tracking view
     */
    class TrackMapAdapter extends HyperTrackMapAdapter {
        TrackMapAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        public boolean showUserInfoForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
            return true;
        }

        @Override
        public String getOrderStatusToolbarDefaultTitle(HyperTrackMapFragment hyperTrackMapFragment) {
            return Track.this.getString(R.string.app_name);
        }

        @Override
        public Toolbar getToolbar(HyperTrackMapFragment hyperTrackMapFragment) {
            return null;
        }

        @Override
        public CameraUpdate getMapFragmentInitialState(HyperTrackMapFragment hyperTrackMapFragment) {
            if (SharedPreferenceManager.getLastKnownLocation() != null) {
                LatLng latLng = new LatLng(SharedPreferenceManager.getLastKnownLocation().getLatitude(),
                        SharedPreferenceManager.getLastKnownLocation().getLongitude());
                return CameraUpdateFactory.newLatLng(latLng);
            }
            return super.getMapFragmentInitialState(hyperTrackMapFragment);
        }

        @Override
        public boolean showTrailingPolyline() {
            return true;
        }

        @Override
        public boolean showTrafficLayer(HyperTrackMapFragment hyperTrackMapFragment) {
            return false;
        }

        @Override
        public boolean enableLiveLocationSharingView() {
            return false;
        }

        @Override
        public boolean showSourceMarkerForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
            return false;
        }

        @Override
        public int getResetBoundsButtonIcon(HyperTrackMapFragment hyperTrackMapFragment) {
            return R.drawable.ic_reset_bounds_button;
        }
    }
}
