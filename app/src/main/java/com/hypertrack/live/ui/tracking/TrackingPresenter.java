package com.hypertrack.live.ui.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

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
import com.hypertrack.live.R;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.live.utils.MapUtils;
import com.hypertrack.live.utils.SimpleLocationListener;
import com.hypertrack.maps.google.widget.GoogleMapAdapter;
import com.hypertrack.maps.google.widget.GoogleMapConfig;
import com.hypertrack.sdk.AsyncResultHandler;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;
import com.hypertrack.sdk.trip.CreateTripRequest;
import com.hypertrack.sdk.views.DeviceUpdatesHandler;
import com.hypertrack.sdk.views.HyperTrackViews;
import com.hypertrack.sdk.views.QueryResultHandler;
import com.hypertrack.sdk.views.dao.Location;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.GpsLocationProvider;
import com.hypertrack.sdk.views.maps.HyperTrackMap;
import com.hypertrack.sdk.views.maps.Predicate;

import io.reactivex.disposables.CompositeDisposable;

@SuppressWarnings("WeakerAccess")
public class TrackingPresenter implements DeviceUpdatesHandler {
    private static final String TAG = "TrackingPresenter";

    private final View view;
    private final TrackingState state;

    private final Context context;
    private final HyperTrack hyperTrack;
    private final HyperTrackViews hyperTrackViews;
    private GoogleMap googleMap;
    private HyperTrackMap hyperTrackMap;
    private GoogleMapConfig mapConfig;
    private TrackingStateObserver.OnTrackingStateChangeListener onTrackingStateChangeListener;

    private Marker destinationMarker;

    protected final CompositeDisposable disposables = new CompositeDisposable();

    public TrackingPresenter(@NonNull Context context, @NonNull final View view, @NonNull String hyperTrackPubKey) {
        this.context = context.getApplicationContext() == null ? context : context.getApplicationContext();
        this.view = view;
        state = new TrackingState(this.context, hyperTrackPubKey);

        hyperTrack = HyperTrack.getInstance(context, hyperTrackPubKey);
        hyperTrackViews = HyperTrackViews.getInstance(context, state.getHyperTrackPubKey());
        mapConfig = GoogleMapConfig.newBuilder(context).build();
    }

    public void initMap(@NonNull GoogleMap googleMap) {

        this.googleMap = googleMap;
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(final LatLng latLng) {
                updateDestination(latLng);
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
        hyperTrackViews.subscribeToDeviceUpdates(hyperTrack.getDeviceID(), this);

        GoogleMapAdapter mapAdapter = new GoogleMapAdapter(googleMap, mapConfig);
        mapAdapter.addTripFilter(new Predicate<Trip>() {
            @Override
            public boolean apply(Trip trip) {
                return trip.getTripId().equals(state.getTripId());
            }
        });
        hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter)
                .bind(new GpsLocationProvider(context))
                .bind(hyperTrackViews, hyperTrack.getDeviceID());

        if (TextUtils.isEmpty(state.getTripId())) {
            hyperTrackMap.setLocationUpdatesListener(new SimpleLocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    hyperTrackMap.moveToMyLocation();
                    hyperTrackMap.setLocationUpdatesListener(null);
                }
            });
        } else {
            hyperTrackViews.getTrip(state.getTripId(), new com.hypertrack.sdk.views.QueryResultHandler<Trip>() {
                @Override
                public void onQueryResult(Trip trip) {
                    state.setShareableUrl(trip.getViews().getSharedUrl());
                    hyperTrackMap.moveToTrip(trip);
                    if (trip.getStatus().equals("active")) {
                        hyperTrackMap.subscribeTrip(state.getTripId());
                        view.onTripChanged(trip);
                    }
                }

                @Override
                public void onQueryFailure(Exception e) {
                    Log.e(TAG, "get trip failure", e);
                }
            });
        }
    }

    public void resume() {
        if (hyperTrack.isRunning()) {
            view.onTrackingStart();
        } else {
            view.onTrackingStop();
        }
    }

    public void moveToMyLocation() {
        if (hyperTrackMap != null) {
            hyperTrackMap.moveToMyLocation();
        }
    }

    public void setMyLocationEnabled(boolean enabled) {
        if (hyperTrackMap != null) {
            hyperTrackMap.setMyLocationEnabled(enabled);
        }
    }

    public void performTracking() {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), Activity.MODE_PRIVATE)
                .edit();
        if (hyperTrack.isRunning()) {
            hyperTrack.stop();
            editor.putBoolean("is_tracking", false).apply();
        } else {
            if (AppUtils.isGpsProviderEnabled(context)) {
                hyperTrack.start();
                editor.putBoolean("is_tracking", true).apply();
            } else {
                actionLocationSourceSettings();
            }
        }
    }

    public void startTrip() {
        view.showProgressBar();

        AsyncResultHandler<com.hypertrack.sdk.trip.Trip> resultHandler = new AsyncResultHandler<com.hypertrack.sdk.trip.Trip>() {
            @Override
            public void onResultReceived(@NonNull com.hypertrack.sdk.trip.Trip trip) {
                view.hideProgressBar();

                state.setTripId(trip.getTripId());
                hyperTrackMap.subscribeTrip(trip.getTripId());
                hyperTrackViews.getTrip(trip.getTripId(), new QueryResultHandler<Trip>() {
                    @Override
                    public void onQueryResult(Trip trip) {
                        if (destinationMarker != null) {
                            destinationMarker.remove();
                        }
                        state.setShareableUrl(trip.getViews().getSharedUrl());
                        if (state.getDestination() == null) {
                            shareUrl(trip.getViews().getSharedUrl());
                        }
                        view.onTripChanged(trip);
                        hyperTrackMap.moveToTrip(trip);
                    }

                    @Override
                    public void onQueryFailure(Exception e) {
                        Log.e(TAG, "start trip failure", e);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Error error) {
                Log.e(TAG, "start trip failure", error);
            }
        };
        CreateTripRequest tripRequest;
        if (state.getDestination() == null) {
            tripRequest = new CreateTripRequest.Builder().build();
        } else {
            tripRequest = new CreateTripRequest.Builder()
                    .setDestinationLatitude(state.getDestination().latitude)
                    .setDestinationLongitude(state.getDestination().longitude)
                    .build();
        }
        hyperTrack.createTrip(tripRequest, resultHandler);
    }

    public void endTrip() {
        view.showProgressBar();

        hyperTrack.completeTrip(state.getTripId(), new AsyncResultHandler<String>() {
            @Override
            public void onResultReceived(@NonNull String s) {
                view.hideProgressBar();

                view.onTripChanged(null);
            }

            @Override
            public void onFailure(@NonNull Error error) {
                Log.e(TAG, "complete trip failure", error);
            }
        });
    }

    public void shareTracking() {
        if (TextUtils.isEmpty(state.getTripId())) {

            startTrip();
        } else {

            final String url = state.getShareableUrl();
            if (!TextUtils.isEmpty(url)) {
                shareUrl(url);
            }
        }
    }

    private void shareUrl(String url) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);
    }

    public void actionLocationSourceSettings() {
        if (!InstantApps.isInstantApp(context)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TrackingFragment.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                Place place = Autocomplete.getPlaceFromIntent(data);
                if (place.getLatLng() != null) {
                    updateDestination(place.getLatLng());
                    if (hyperTrackMap != null) {
                        hyperTrackMap.moveToLocation(place.getLatLng().latitude, place.getLatLng().longitude);
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                if (status.getStatusMessage() != null) Log.i(TAG, status.getStatusMessage());
            }
        }
    }

    private void updateDestination(LatLng latLng) {
        state.setDestination(latLng);
        if (latLng == null) {
            if (destinationMarker != null) {
                destinationMarker.remove();
            }
            view.onDestinationChanged("");
        } else {
            if (destinationMarker == null) {
                destinationMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_myplaces))
                );
            } else {
                destinationMarker.setPosition(latLng);
            }
            disposables.add(MapUtils.getAddress(context, latLng).subscribe(new io.reactivex.functions.Consumer<String>() {
                @Override
                public void accept(String s) {
                    view.onDestinationChanged(s);
                }
            }));
        }
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

    }

    @Override
    public void onTripUpdateReceived(@NonNull Trip trip) {
        if (trip.getTripId().equals(state.getTripId())) {
            state.setShareableUrl(trip.getViews().getSharedUrl());
            view.onTripChanged(trip);
        }
    }

    @Override
    public void onError(Exception e, String s) {

    }

    @Override
    public void onCompleted(String s) {

    }

    public interface View extends TrackingStateObserver.OnTrackingStateChangeListener {

        void onDestinationChanged(String address);

        void onTripChanged(Trip trip);

        void showProgressBar();

        void hideProgressBar();
    }
}
