package io.hypertrack.sendeta.view;

import android.content.Context;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.util.SharedPreferenceManager;

/**
 * Created by piyush on 03/05/17.
 */

public class HomeMapAdapter extends HyperTrackMapAdapter {

    public Context mContext;
    private Toolbar toolbar;

    public HomeMapAdapter(Context mContext, Toolbar toolbar) {
        super(mContext);
        this.mContext = mContext;
        this.toolbar = toolbar;
    }

    @Override
    public Toolbar getToolbar(HyperTrackMapFragment hyperTrackMapFragment) {
        return toolbar;
    }

    @Override
    public String getOrderStatusToolbarDefaultTitle(HyperTrackMapFragment hyperTrackMapFragment) {
        return BuildConfig.TOOLBAR_TITLE;
    }

    @Override
    public CameraUpdate getMapFragmentInitialState(HyperTrackMapFragment hyperTrackMapFragment) {
        if (SharedPreferenceManager.getLastKnownLocation() != null) {
            LatLng latLng = new LatLng(SharedPreferenceManager.getLastKnownLocation().getLatitude(), SharedPreferenceManager.getLastKnownLocation().getLongitude());
            return CameraUpdateFactory.newLatLng(latLng);
        }
        return super.getMapFragmentInitialState(hyperTrackMapFragment);
    }

    @Override
    public boolean setMyLocationEnabled(HyperTrackMapFragment hyperTrackMapFragment) {
        return HyperTrack.getConsumerClient().getActionIDs() == null ||
                HyperTrack.getConsumerClient().getActionIDs().isEmpty();
    }

    @Override
    public boolean setMyLocationButtonEnabled(HyperTrackMapFragment hyperTrackMapFragment) {
        return HyperTrack.getConsumerClient().getActionIDs() == null ||
                HyperTrack.getConsumerClient().getActionIDs().isEmpty();
    }

    @Override
    public boolean showUserInfoForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return false;
    }

    @Override
    public boolean showSelectExpectedPlace() {
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

    @Override
    public boolean showCompletedAction() {
        return true;
    }

    @Override
    public int[] getMapPadding(HyperTrackMapFragment hyperTrackMapFragment) {
        int bottom = mContext.getResources().getDimensionPixelSize(R.dimen.live_tracking_map_bottom_padding);
        int right = mContext.getResources().getDimensionPixelSize(R.dimen.map_side_padding);

        if (HyperTrack.getConsumerClient().getActionIDs() == null || HyperTrack.getConsumerClient().getActionIDs().isEmpty()) {
            bottom = mContext.getResources().getDimensionPixelSize(R.dimen.home_map_bottom_padding);
            return new int[]{0, 0, 0, bottom};
        }

        return new int[]{0, 0, right, bottom};
    }

    @Override
    public int getResetBoundsButtonIcon(HyperTrackMapFragment hyperTrackMapFragment) {
        return R.drawable.ic_reset_bounds_button;
    }

    @Override
    public int showETAOn() {
        return HyperTrackMapAdapter.ETA_ON_HERO_MARKER;
    }

    @Override
    public int getExpectedPlaceMarkerIconForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return R.drawable.ic_ht_destination_marker_default;
    }

    @Override
    public boolean showActionSummaryForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return HyperTrack.getConsumerClient().getActionIDs() != null && HyperTrack.getConsumerClient().getActionIDs().size() == 1;
    }
}
