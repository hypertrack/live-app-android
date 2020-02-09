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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.hypertrack.sdk.views.QueryResultHandler;
import com.hypertrack.sdk.views.dao.Location;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.GpsLocationProvider;
import com.hypertrack.sdk.views.maps.HyperTrackMap;
import com.hypertrack.sdk.views.maps.TripSubscription;
import com.hypertrack.trips.ResultHandler;
import com.hypertrack.trips.ShareableTrip;
import com.hypertrack.trips.TripConfig;
import com.hypertrack.trips.TripsManager;

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

    private Marker destinationMarker;

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
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(final LatLng latLng) {
                if (!mapDestinationMode) {
                    updateDestination(latLng);
                }
            }
        });

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
        hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter)
                .bind(new GpsLocationProvider(context))
                .bind(hyperTrackViews, hyperTrack.getDeviceID());
        if (hyperTrack.isRunning()) {
            onTrackingStateChangeListener.onTrackingStart();
        } else {
            onTrackingStateChangeListener.onTrackingStop();
        }

        if (TextUtils.isEmpty(state.getTripId())) {
            hyperTrackMap.setLocationUpdatesListener(new SimpleLocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    hyperTrackMap.moveToMyLocation();
                    hyperTrackMap.setLocationUpdatesListener(null);
                }
            });
        } else {
            // CRUTCH it should be hyperTrackViews.subscribeToTripUpdates(tripId, this);
            hyperTrackViews.getTrip(state.getTripId(), new QueryResultHandler<Trip>() {
                @Override
                public void onQueryResult(Trip trip) {
                    onTripUpdateReceived(trip);
                }

                @Override
                public void onQueryFailure(Exception e) {
                    Log.e(TAG, "getTrip failure", e);
                }
            });
        }
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
            if (TextUtils.isEmpty(state.getTripId())) {
                hyperTrackMap.moveToMyLocation();
            } else {
                hyperTrackMap.adapter().setCameraFixedEnabled(enabled);
            }
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
            updateDestination(googleMap.getCameraPosition().target);
            view.popBackStack();
            startTrip();
            return;
        }

        if (state.getDestination() == null) {
            view.addFragment(new SearchPlaceFragment());
        } else {
            startTrip();
        }
    }

    public void shareHyperTrackUrl() {
        String url = state.getShareableUrl();
        if (!TextUtils.isEmpty(url)) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, url);
            sendIntent.setType("text/plain");
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sendIntent);
        }
    }

    public void startTrip() {
        view.showProgressBar();

        ResultHandler<ShareableTrip> resultHandler = new ResultHandler<ShareableTrip>() {
            @Override
            public void onResult(@NonNull ShareableTrip trip) {
                view.hideProgressBar();

                state.setTripId(trip.getTripId());
                state.setShareableUrl(trip.getShareUrl());
                shareHyperTrackUrl();
                updateDestination(null);
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

        tripsManager.completeTrip(state.getTripId(), new ResultHandler<String>() {

            @Override
            public void onResult(@NonNull String result) {
                view.hideProgressBar();
            }

            @Override
            public void onError(@NonNull Exception error) {
                view.hideProgressBar();
                Log.e(TAG, "complete trip failure", error);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public void removeTrip() {
        if (state.getOuterTrips().containsKey(state.getTripId())) {
            state.getOuterTrips().remove(state.getTripId()).remove();
        }
        state.setTripId(null);
        state.setShareableUrl(null);
        hyperTrackMap.adapter().notifyDataSetChanged();
        view.dismissTrip();
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
                    updateDestination(place.getLatLng());
                    if (hyperTrackMap != null && place.getLatLng() != null) {
                        hyperTrackMap.moveToLocation(place.getLatLng().latitude, place.getLatLng().longitude);
                    }
                } else {
                    updateDestination(null);
                    startTrip();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                if (status.getStatusMessage() != null) Log.i(TAG, status.getStatusMessage());
            }
        } else if (requestCode == TrackingFragment.SET_ON_MAP_REQUEST_CODE) {
            startMapDestinationMode();
        }
    }

    private void updateDestination(LatLng latLng) {
        state.setDestination(latLng);
        if (latLng == null) {
            if (destinationMarker != null) {
                destinationMarker.remove();
            }
        } else {
            if (destinationMarker == null) {
                destinationMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination))
                );
            } else {
                destinationMarker.setPosition(latLng);
            }
        }
    }

    private void startMapDestinationMode() {
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

    private void stopMapDestinationMode() {
        mapDestinationMode = false;
        googleMap.setOnCameraMoveCanceledListener(null);
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

        boolean isNewTrip = !state.getOuterTrips().containsKey(trip.getTripId());
        boolean isActive = !trip.getStatus().equals("completed");

        if (trip.getTripId().equals(state.getTripId())) {
            if (isNewTrip) {
                TripSubscription tripSubscription = hyperTrackMap.subscribeTrip(state.getTripId());
                state.getOuterTrips().put(trip.getTripId(), tripSubscription);
                hyperTrackMap.moveToTrip(trip);
            }

            if (isActive) {
                view.showTripInfo(trip);
            } else {
                hyperTrackViews.getTrip(trip.getTripId(), new QueryResultHandler<Trip>() {
                    @Override
                    public void onQueryResult(Trip trip) {
                        view.showTripSummaryInfo(trip);
                    }

                    @Override
                    public void onQueryFailure(Exception e) {
                        Log.e(TAG, "complete trip failure", e);
                    }
                });
            }
        } else {
            if (!isNewTrip && !isActive) {
                state.getOuterTrips().remove(trip.getTripId()).remove();
            }
        }

        hyperTrack.syncDeviceSettings();
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

        void showTripInfo(@NonNull Trip trip);

        void showTripSummaryInfo(@NonNull Trip trip);

        void dismissTrip();

        void showProgressBar();

        void hideProgressBar();

        void addFragment(@NonNull Fragment fragment);

        void popBackStack();
    }
}
