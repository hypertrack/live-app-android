package com.hypertrack.live.ui.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.hypertrack.live.App;
import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.R;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.live.utils.MapUtils;
import com.hypertrack.maps.google.widget.GoogleMapAdapter;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.views.DeviceUpdatesHandler;
import com.hypertrack.sdk.views.HyperTrackViews;
import com.hypertrack.sdk.views.dao.Location;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.GpsLocationProvider;
import com.hypertrack.sdk.views.maps.HyperTrackMap;
import com.hypertrack.sdk.views.maps.Predicate;
import com.hypertrack.sdk.views.maps.TripSubscription;
import com.hypertrack.trips.ResultHandler;
import com.hypertrack.trips.TripsManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("WeakerAccess")
class TrackingPresenter implements DeviceUpdatesHandler {
    private static final String TAG = App.TAG + "TrackingPresenter";

    private final Handler handler = new Handler();

    private final View view;
    private final TrackingState state;

    private final Context context;
    private final HyperTrack hyperTrack;
    private final HyperTrackViews hyperTrackViews;
    private HyperTrackMap hyperTrackMap;
    private final TripsManager tripsManager;

    private Timer tripInfoUpdater;

    public TrackingPresenter(@NonNull Context context, @NonNull final View view) {
        this.context = context.getApplicationContext() == null ? context : context.getApplicationContext();
        this.view = view;
        state = new TrackingState(context);

        hyperTrack = HyperTrack.getInstance(context, state.getHyperTrackPubKey());
        hyperTrackViews = HyperTrackViews.getInstance(context, state.getHyperTrackPubKey());

        tripsManager = HTMobileClient.getTripsManager(context);
    }

    public void subscribeUpdates(@NonNull GoogleMap googleMap) {

        if (hyperTrackMap == null) {
            GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, MapUtils.getBuilder(context).build());
            mapAdapter.addTripFilter(new Predicate<Trip>() {
                @Override
                public boolean apply(Trip trip) {
                    return trip.getTripId().equals(state.getSelectedTripId());
                }
            });
            hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter)
                    .bind(new GpsLocationProvider(context));
        }
        hyperTrackMap.bind(hyperTrackViews, hyperTrack.getDeviceID());
        hyperTrackViews.subscribeToDeviceUpdates(hyperTrack.getDeviceID(), this);
        hyperTrack.syncDeviceSettings();

        Trip selectedTrip = state.trips.get(state.getSelectedTripId());
        if (selectedTrip == null) {
            view.showSearch();
            hyperTrackMap.moveToMyLocation();
        } else {
            hyperTrackMap.moveToTrip(selectedTrip);
        }
    }

    public void pause() {
        hyperTrackMap.unbindHyperTrackViews();
        hyperTrackViews.unsubscribeFromDeviceUpdates(this);
    }

    public void setCameraFixedEnabled(boolean enabled) {
        if (hyperTrackMap != null) {
            if (enabled) {
                Trip selectedTrip = state.trips.get(state.getSelectedTripId());
                if (selectedTrip == null) {
                    view.showSearch();
                    hyperTrackMap.moveToMyLocation();
                } else {
                    hyperTrackMap.moveToTrip(selectedTrip);
                }
            }
            hyperTrackMap.adapter().setCameraFixedEnabled(enabled);
        }
    }

    public void shareTrackMessage() {
        AppUtils.shareTrackMessage(context, state.getShareMessage());
    }

    public void selectTrip(Trip trip) {
        state.setSelectedTrip(trip);

        if (trip.getStatus().equals("completed")) {
            view.showTripSummaryInfo(trip);
        } else {
            view.showTripInfo(trip);
        }
        hyperTrackMap.adapter().notifyDataSetChanged();
        hyperTrackMap.moveToTrip(trip);
    }

    public void endTrip() {
        if (!TextUtils.isEmpty(state.getSelectedTripId())) {
            view.showProgressBar();

            tripsManager.completeTrip(state.getSelectedTripId(), new ResultHandler<String>() {

                @Override
                public void onResult(@NonNull String result) {
                    Log.d(TAG, "trip is ended: " + result);
                    view.hideProgressBar();
                }

                @Override
                public void onError(@NonNull Exception error) {
                    Log.e(TAG, "complete trip failure", error);
                    view.hideProgressBar();
                    Toast.makeText(context, "Complete trip failure", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void startTripInfoUpdating(final Trip trip) {

        stopTripInfoUpdating();

        tripInfoUpdater = new Timer();
        tripInfoUpdater.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        view.showTripInfo(trip);
                    }
                });
            }
        }, 60000, 60000);
    }

    public void stopTripInfoUpdating() {
        if (tripInfoUpdater != null) {
            tripInfoUpdater.cancel();
            tripInfoUpdater = null;
        }
    }

    public void destroy() {
        stopTripInfoUpdating();
        if (hyperTrackMap != null) {
            hyperTrackMap.destroy();
            hyperTrackMap = null;
        }
        hyperTrackViews.unsubscribeFromDeviceUpdates(this);
    }

    @Override
    public void onLocationUpdateReceived(@NonNull Location location) {
    }

    @Override
    public void onBatteryStateUpdateReceived(int i) {
    }

    @Override
    public void onStatusUpdateReceived(@NonNull StatusUpdate statusUpdate) {
        String status;
        switch (statusUpdate.value) {
            case StatusUpdate.STOPPED:
                status = context.getString(R.string.status_stopped);
                break;
            default:
                status = "unknown";
        }
        view.onStatusUpdateReceived(String.format(context.getString(R.string.tracking_is), status));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onTripUpdateReceived(@NonNull Trip trip) {
        Log.d(TAG, "onTripUpdateReceived: " + trip);

        state.trips.put(trip.getTripId(), trip);
        boolean isNewTrip = !state.tripSubscription.containsKey(trip.getTripId());
        boolean isActive = !trip.getStatus().equals("completed");

        if (isActive) {
            if (isNewTrip) {
                TripSubscription tripSubscription = hyperTrackMap.subscribeTrip(trip.getTripId());
                state.tripSubscription.put(trip.getTripId(), tripSubscription);
                hyperTrackMap.moveToTrip(trip);
            }
            if (trip.getTripId().equals(state.getSelectedTripId())) {
                view.showTripInfo(trip);
            }
        } else {

            state.trips.remove(trip.getTripId());
            if (!isNewTrip) {
                state.tripSubscription.remove(trip.getTripId()).remove();
            }
        }

        List<Trip> trips = state.getAllTripsStartingFromLatest();
        int selectedTripIndex = 0;
        if (!trips.isEmpty()) {
            Trip selectedTrip = state.trips.get(state.getSelectedTripId());
            if (selectedTrip == null) {
                selectedTrip = trips.get(0);
                selectTrip(selectedTrip);
            } else {
                state.setSelectedTrip(selectedTrip);
            }
            selectedTripIndex = trips.indexOf(selectedTrip);
        } else {
            state.setSelectedTrip(null);
        }
        view.updateTripsMenu(trips, selectedTripIndex);
    }

    @Override
    public void onError(@NonNull Exception e, @NonNull String s) {
    }

    @Override
    public void onCompleted(@NonNull String s) {
    }

    public interface View {

        void onStatusUpdateReceived(@NonNull String statusText);

        void showSearch();

        void updateTripsMenu(@NonNull List<Trip> trips, int selectedTripIndex);

        void showTripInfo(@NonNull Trip trip);

        void showTripSummaryInfo(@NonNull Trip trip);

        void showProgressBar();

        void hideProgressBar();
    }
}
