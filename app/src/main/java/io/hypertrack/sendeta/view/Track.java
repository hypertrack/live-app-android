package io.hypertrack.sendeta.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.Button;

import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;

import java.util.ArrayList;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.ITrackPresenter;
import io.hypertrack.sendeta.presenter.TrackPresenter;


public class Track extends BaseActivity implements TrackView {

    public static final String KEY_TRACK_DEEPLINK = "track_deeplink";
    public static final String KEY_TASK_ID_LIST = "task_id_list";

    private Button retryButton;

    private HyperTrackMapFragment hyperTrackMapFragment;
    private MyMapAdapter mapAdapter;
    private Intent intent;
    private ProgressDialog progressDialog;
    private TrackPresenter trackPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        trackPresenter = new ITrackPresenter(this);

        initUI();

        intent = getIntent();
        if (processIntentParams())
            return;

        displayLoader(false);

        finish();
    }

    private void initUI() {
        //Intialize Map Fragment
        hyperTrackMapFragment = (HyperTrackMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapAdapter = new MyMapAdapter(Track.this);
        hyperTrackMapFragment.setHTMapAdapter(mapAdapter);
        retryButton = (Button) findViewById(R.id.retryButton);
    }

    private boolean processIntentParams() {
        // Check if Intent has a valid TASK_ID_LIST extra
        if (intent != null && intent.hasExtra(KEY_TASK_ID_LIST)) {

            ArrayList<String> actionIDList = intent.getStringArrayListExtra(KEY_TASK_ID_LIST);

            // Check if a valid TASK_ID_LIST is available
            if (actionIDList != null && !actionIDList.isEmpty()) {

                if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {

                    //Remove any previous action if currently being tracked.
                    trackPresenter.removeTrackingAction();

                    //Set the current tracking action
                    trackPresenter.addTrackingAction(actionIDList.get(0));

                    // Add TaskId being tracked by this user
                    trackPresenter.trackAction(actionIDList);
                }
                return true;
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
    protected void onDestroy() {
        trackPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(new Intent(this, Home.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .startActivities();
        finish();

    }

    @Override
    public void showLoader(boolean toggle) {

        displayLoader(toggle);
    }

    @Override
    public void showTrackingDetail() {
        mapAdapter.notifyDataSetChanged();
        displayLoader(false);
    }

    @Override
    public void showError() {
        showRetryButton(true);
        displayLoader(false);
    }

    private class MyMapAdapter extends HyperTrackMapAdapter {

        public MyMapAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        public boolean showHeroMarkerForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
            return true;
        }
    }
}
