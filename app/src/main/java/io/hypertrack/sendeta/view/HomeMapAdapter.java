package io.hypertrack.sendeta.view;

import android.content.Context;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 03/05/17.
 */

public class HomeMapAdapter extends HyperTrackMapAdapter {

    public Context mContext;

    public HomeMapAdapter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public boolean showOrderStatusToolbar(HyperTrackMapFragment hyperTrackMapFragment) {
        return false;
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
    public boolean showTrailingPolyline(String actionID) {
        return true;
    }

    @Override
    public boolean showTrafficLayer(HyperTrackMapFragment hyperTrackMapFragment) {
        return false;
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
}
