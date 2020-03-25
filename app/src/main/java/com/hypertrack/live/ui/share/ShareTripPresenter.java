package com.hypertrack.live.ui.share;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.hypertrack.live.App;
import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.live.utils.MapUtils;
import com.hypertrack.maps.google.widget.GoogleMapAdapter;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.views.DeviceUpdatesHandler;
import com.hypertrack.sdk.views.HyperTrackViews;
import com.hypertrack.sdk.views.dao.Location;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.HyperTrackMap;
import com.hypertrack.trips.ResultHandler;
import com.hypertrack.trips.TripsManager;

class ShareTripPresenter implements DeviceUpdatesHandler {
    private static final String TAG = App.TAG + "ShTripPresenter";

    private final Context context;
    private final View view;
    private final ShareTripState state;

    private final HyperTrack hyperTrack;
    private final HyperTrackViews hyperTrackViews;
    private final TripsManager tripsManager;
    private HyperTrackMap hyperTrackMap;

    public ShareTripPresenter(Context context, View view, String shareUrl) {
        this.context = context.getApplicationContext() == null ? context : context.getApplicationContext();
        this.view = view;
        this.state = new ShareTripState(context);
        state.setShareUrl(shareUrl);

        hyperTrack = HyperTrack.getInstance(context, state.getHyperTrackPubKey());
        hyperTrackViews = HyperTrackViews.getInstance(context, state.getHyperTrackPubKey());
        tripsManager = HTMobileClient.getTripsManager(context);
    }

    public void subscribeTripUpdates(GoogleMap googleMap, String tripId) {
        if (hyperTrackMap == null) {
            GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, MapUtils.getBuilder(context).build());
            hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter);
            hyperTrackMap.setMyLocationEnabled(false);
        }

        state.setCurrentTripId(tripId);
        hyperTrackViews.subscribeToDeviceUpdates(hyperTrack.getDeviceID(), tripId, this);
        hyperTrackMap.bind(hyperTrackViews, hyperTrack.getDeviceID())
                .subscribeTrip(tripId);
    }

    public void pause() {
        hyperTrackViews.unsubscribeFromDeviceUpdates(this);
        hyperTrackMap.unbindHyperTrackViews();
    }

    public void shareTrackMessage() {
        AppUtils.shareTrackMessage(context, state.getShareMessage());
    }

    public void endTrip() {
        if (!TextUtils.isEmpty(state.getCurrentTripId())) {

            tripsManager.completeTrip(state.getCurrentTripId(), new ResultHandler<String>() {

                @Override
                public void onResult(@NonNull String result) {
                    Log.d(TAG, "trip is ended: " + result);
                    state.setCurrentTripId(null);
                }

                @Override
                public void onError(@NonNull Exception error) {
                    Log.e(TAG, "trip completion failure", error);
                    Toast.makeText(context, "Trip completion failure", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void destroy() {
        hyperTrackViews.unsubscribeFromDeviceUpdates(this);
        if (hyperTrackMap != null) {
            hyperTrackMap.destroy();
            hyperTrackMap = null;
        }
    }

    @Override
    public void onLocationUpdateReceived(@NonNull Location location) {
    }

    @Override
    public void onBatteryStateUpdateReceived(int i) {
    }

    @Override
    public void onStatusUpdateReceived(@NonNull StatusUpdate statusUpdate) {
    }

    @Override
    public void onTripUpdateReceived(@NonNull Trip trip) {
        hyperTrackMap.moveToTrip(trip);
        state.updateTrip(trip);
        view.onTripUpdate(trip);
    }

    @Override
    public void onError(@NonNull Exception e, @NonNull String s) {
    }

    @Override
    public void onCompleted(@NonNull String s) {
    }

    public interface View {

        void showProgressBar();

        void hideProgressBar();

        void onTripUpdate(@NonNull Trip trip);
    }
}
