package com.hypertrack.live.ui.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.wrappers.InstantApps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.places.SearchPlaceFragment;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.live.utils.MapUtils;
import com.hypertrack.live.utils.SimpleLocationListener;
import com.hypertrack.maps.google.widget.GoogleMapAdapter;
import com.hypertrack.maps.google.widget.GoogleMapConfig;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;
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
import com.hypertrack.trips.ShareableTrip;
import com.hypertrack.trips.TripConfig;
import com.hypertrack.trips.TripsManager;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

@SuppressWarnings("WeakerAccess")
class TrackingPresenter implements DeviceUpdatesHandler {
    private static final String TAG = "TrackingPresenter";

    private final View view;
    private final TrackingState state;

    private final Handler handler = new Handler();
    private final Context context;
    private final HyperTrack hyperTrack;
    private final TripsManager tripsManager;
    private final HyperTrackViews hyperTrackViews;
    private GoogleMap googleMap;
    private HyperTrackMap hyperTrackMap;
    private GoogleMapConfig mapConfig;
    private TrackingStateObserver.OnTrackingStateChangeListener onTrackingStateChangeListener;

    private boolean mapDestinationMode = false;

    protected final CompositeDisposable disposables = new CompositeDisposable();

    public TrackingPresenter(@NonNull Context context, @NonNull final View view, @NonNull String hyperTrackPubKey) {
        this.context = context.getApplicationContext() == null ? context : context.getApplicationContext();
        this.view = view;
        state = new TrackingState(this.context, hyperTrackPubKey);

        hyperTrack = HyperTrack.getInstance(context, hyperTrackPubKey);
        hyperTrackViews = HyperTrackViews.getInstance(context, state.getHyperTrackPubKey());
        tripsManager = HTMobileClient.getTripsManager(context);

        mapConfig = MapUtils.getBuilder(context).build();
    }

    public void initMap(@NonNull GoogleMap googleMap) {

        this.googleMap = googleMap;

        onTrackingStateChangeListener = new TrackingStateObserver.OnTrackingStateChangeListener() {
            @Override
            public void onError(TrackingError trackingError) {
                view.onError(trackingError);
            }

            @Override
            public void onTrackingStart() {
                view.onTrackingStart();
            }

            @Override
            public void onTrackingStop() {
                view.onTrackingStop();
            }
        };
        hyperTrack.addTrackingListener(onTrackingStateChangeListener);
        hyperTrack.syncDeviceSettings();
        hyperTrackViews.subscribeToDeviceUpdates(hyperTrack.getDeviceID(), this);
        GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, mapConfig);
        mapAdapter.addTripFilter(new Predicate<Trip>() {
            @Override
            public boolean apply(Trip trip) {
                return trip.getTripId().equals(state.getCurrentTripId());
            }
        });
        hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter)
                .bind(new GpsLocationProvider(context))
                .bind(hyperTrackViews, hyperTrack.getDeviceID());
        if (hyperTrack.isRunning()) {
            onTrackingStateChangeListener.onTrackingStart();
        } else {
            onTrackingStateChangeListener.onTrackingStop();
        }

        view.showSearch();
        hyperTrackMap.setLocationUpdatesListener(new SimpleLocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                hyperTrackMap.moveToMyLocation();
                hyperTrackMap.setLocationUpdatesListener(null);
            }
        });
    }

    public void resume() {
        if (AppUtils.isGpsProviderEnabled(context)) {
            view.onActive();
        } else {
            view.onDisabled();
        }
    }

    public void setCameraFixedEnabled(boolean enabled) {
        if (hyperTrackMap != null) {
            if (TextUtils.isEmpty(state.getCurrentTripId())) {
                hyperTrackMap.moveToMyLocation();
            }
            hyperTrackMap.adapter().setCameraFixedEnabled(enabled);
        }
    }

    public void setMyLocationEnabled(boolean enabled) {
        if (hyperTrackMap != null) {
            hyperTrackMap.setMyLocationEnabled(enabled);
        }
    }

    public void share() {
        if (mapDestinationMode) {
            stopMapDestinationMode();
            state.setDestination(googleMap.getCameraPosition().target);
            view.popBackStack();
        }

        if (state.getDestination() != null) {
            startTrip();
        }
    }

    public void shareTrackMessage() {
        String message = state.getShareMessage();
        if (message.isEmpty()) return;
        shareTrackMessage(message);
    }

    public void shareTrackMessage(String shareableMessage) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareableMessage);
        sendIntent.setType("text/plain");
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);
    }

    public void selectTrip(Trip trip) {
        state.setCurrentTrip(trip);

        if (trip.getStatus().equals("completed")) {
            view.showTripSummaryInfo(trip);
        } else {
            view.showTripInfo(trip);
        }
        hyperTrackMap.adapter().notifyDataSetChanged();
        hyperTrackMap.moveToTrip(trip);
    }

    public void startTrip() {
        view.showProgressBar();

        ResultHandler<ShareableTrip> resultHandler = new ResultHandler<ShareableTrip>() {
            @Override
            public void onResult(@NonNull ShareableTrip trip) {
                Log.d(TAG, "trip is created: " + trip);
                view.hideProgressBar();

                state.setCurrentTrip(trip);
                shareTrackMessage(state.getShareMessage());
                state.setDestination(null);

                hyperTrackMap.adapter().notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Exception error) {
                Log.e(TAG, "start trip failure", error);
                view.hideProgressBar();

            }

        };
        TripConfig tripRequest;
        if (state.getDestination() == null) {
            tripRequest = new TripConfig.Builder()
                    .setDeviceId(hyperTrack.getDeviceID())
                    .build();
        } else {
            tripRequest = new TripConfig.Builder()
                    .setDestinationLatitude(state.getDestination().latitude)
                    .setDestinationLongitude(state.getDestination().longitude)
                    .setDeviceId(hyperTrack.getDeviceID())
                    .build();
        }
        tripsManager.createTrip(tripRequest, resultHandler);
        if (!AppUtils.isGpsProviderEnabled(context)) {
            actionLocationSourceSettings();
        }
    }

    public void endTrip() {
        view.showProgressBar();

        tripsManager.completeTrip(state.getCurrentTripId(), new ResultHandler<String>() {

            @Override
            public void onResult(@NonNull String result) {
                Log.d(TAG, "trip is ended: " + result);
                view.hideProgressBar();
            }

            @Override
            public void onError(@NonNull Exception error) {
                view.hideProgressBar();
                Log.e(TAG, "complete trip failure", error);
            }
        });
    }

    public void actionLocationSourceSettings() {
        if (!InstantApps.isInstantApp(context)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TrackingFragment.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                if (data.getExtras() != null && data.getExtras().get(SearchPlaceFragment.SELECTED_PLACE_KEY) != null) {
                    Place place = (Place) data.getExtras().get(SearchPlaceFragment.SELECTED_PLACE_KEY);
                    state.setDestination(place.getLatLng());
                } else {
                    state.setDestination(null);
                }
                startTrip();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                if (status.getStatusMessage() != null) Log.i(TAG, status.getStatusMessage());
            }
        } else if (requestCode == TrackingFragment.SET_ON_MAP_REQUEST_CODE) {
            startMapDestinationMode();
        } else if (requestCode == TrackingFragment.SHARE_REQUEST_CODE) {
            share();
        }
    }

    public void startMapDestinationMode() {
        mapDestinationMode = true;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                view.onDestinationChanged(context.getString(R.string.searching_));
                disposables.add(MapUtils.getAddress(context, googleMap.getCameraPosition().target)
                        .subscribe(new io.reactivex.functions.Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                view.onDestinationChanged(s);
                            }
                        }));
            }
        };
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 500);
            }
        });
    }

    public void stopMapDestinationMode() {
        mapDestinationMode = false;
        googleMap.setOnCameraMoveListener(null);
    }

    public void destroy() {
        if (hyperTrackMap != null) {
            hyperTrackMap.destroy();
            hyperTrackMap = null;
        }

        hyperTrack.removeTrackingListener(onTrackingStateChangeListener);
        hyperTrackViews.stopAllUpdates();
        disposables.clear();
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

        state.addTrip(trip);
        boolean isNewTrip = !state.tripSubscription.containsKey(trip.getTripId());
        boolean isActive = !trip.getStatus().equals("completed");

        if (isActive) {
            if (isNewTrip) {
                TripSubscription tripSubscription = hyperTrackMap.subscribeTrip(trip.getTripId());
                state.tripSubscription.put(trip.getTripId(), tripSubscription);
                hyperTrackMap.moveToTrip(trip);
            }
            if (trip.getTripId().equals(state.getCurrentTripId())) {
                view.showTripInfo(trip);
            }
        } else {
            state.delete(trip);
            if (!isNewTrip) {
                state.tripSubscription.remove(trip.getTripId()).remove();
            }
        }

        List<Trip> trips = state.getAllTripsStartingFromLatest();
        int selectedTripIndex = 0;
        if (!trips.isEmpty()) {
            Trip selectedTrip = state.getCurrentTrip();
            if (selectedTrip == null) {
                selectedTrip = trips.get(0);
                selectTrip(selectedTrip);
            }
            selectedTripIndex = trips.indexOf(selectedTrip);
        } else {
//            state.setCurrentTrip(null);
        }
        view.updateTripsMenu(trips, selectedTripIndex);
    }

    @Override
    public void onError(@NonNull Exception e, @NonNull String s) {
    }

    @Override
    public void onCompleted(@NonNull String s) {
    }

    public interface View extends TrackingStateObserver.OnTrackingStateChangeListener {

        void onActive();

        void onDisabled();

        void onStatusUpdateReceived(@NonNull String statusText);

        void onDestinationChanged(@NonNull String address);

        void showSearch();

        void updateTripsMenu(@NonNull List<Trip> trips, int selectedTripIndex);

        void showTripInfo(@NonNull Trip trip);

        void showTripSummaryInfo(@NonNull Trip trip);

        void showProgressBar();

        void hideProgressBar();

        void addFragment(@NonNull Fragment fragment);

        void popBackStack();
    }
}
