package com.hypertrack.live.ui.places;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.wrappers.InstantApps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.hypertrack.backend.AbstractBackendProvider;
import com.hypertrack.backend.ResultHandler;
import com.hypertrack.backend.models.ShareableTrip;
import com.hypertrack.backend.models.TripConfig;
import com.hypertrack.live.App;
import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.live.utils.MapUtils;
import com.hypertrack.sdk.HyperTrack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

@SuppressWarnings("WeakerAccess")
class SearchPlacePresenter {
    private static final String TAG = App.TAG + "SPlacePresenter";

    private final Context context;
    private final View view;
    private final SearchPlaceState state;

    private final String mHyperTrackDeviceId;
    private final PlacesClient placesClient;
    private final AbstractBackendProvider mBackendProvider;

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isOffline = !AppUtils.isNetworkConnected(context);
            view.updateConnectionStatus(isOffline);
        }
    };

    private GoogleMap googleMap;
    private AutocompleteSessionToken token;

    private final Handler handler = new Handler();
    private final CompositeDisposable disposables = new CompositeDisposable();

    public SearchPlacePresenter(Context context, String mode, View view, @NonNull AbstractBackendProvider backendProvider) {
        this.context = context.getApplicationContext() == null ? context : context.getApplicationContext();
        this.view = view;
        this.state = new SearchPlaceState(this.context, mode, backendProvider);

        mHyperTrackDeviceId = HyperTrack.getInstance(state.getHyperTrackPubKey()).getDeviceID();
        placesClient = Places.createClient(context);
        mBackendProvider = backendProvider;

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.context.registerReceiver(connectivityReceiver, intentFilter);
    }

    public void initMap(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        if ("home".equals(state.getMode())) {
            if (state.getHome() == null) {
                state.saveHomePlace(null);
            }
            view.hideHomeAddress();
        } else if ("search".equals(state.getMode())) {
            view.updateHomeAddress(state.getHome());
        }
    }

    public void setMapDestinationModeEnable(boolean enable) {
        if (state.mapDestinationMode != enable) {
            state.mapDestinationMode = enable;
            if (enable) {
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (googleMap != null) {
                            state.setDestination(null);
                            view.updateAddress(context.getString(R.string.searching_));
                            disposables.add(MapUtils.getAddress(context, googleMap.getCameraPosition().target)
                                    .subscribe(new io.reactivex.functions.Consumer<String>() {
                                        @Override
                                        public void accept(String s) {
                                            PlaceModel destination = new PlaceModel();
                                            destination.address = s;
                                            destination.latLng = googleMap.getCameraPosition().target;
                                            state.setDestination(destination);
                                            view.updateAddress(s);
                                        }
                                    }));
                        }
                    }
                };
                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 500);
                    }
                });
                runnable.run();

                view.updateList(Collections.<PlaceModel>emptyList());
                view.showSetOnMap();
            } else {
                googleMap.setOnCameraMoveListener(null);
                state.setDestination(null);
                view.updateAddress("");
                view.hideSetOnMap();
            }
        }
    }

    public void search(String query) {
        if (!state.mapDestinationMode) {
            if (TextUtils.isEmpty(query)) {

                view.updateList(state.getRecentPlaces());
            } else {
                // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
                // and once again when the user makes a selection (for example when calling selectPlace()).
                token = AutocompleteSessionToken.newInstance();

                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.GEOCODE)
                        .setSessionToken(token)
                        .setQuery(query)
                        .build();

                placesClient.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onSuccess(FindAutocompletePredictionsResponse response) {
                        view.updateList(PlaceModel.from(response.getAutocompletePredictions()));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                });
            }
        }
    }

    public void confirm() {
        if (state.getDestination() != null) {
            providePlace(state.getDestination());
        }
    }

    public void selectHome() {
        providePlace(state.getHome());
    }

    public void selectItem(PlaceModel placeModel) {
        view.showProgressBar();

        if (!placeModel.isRecent) {
            state.addPlaceToRecent(placeModel);
        }

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeModel.placeId, fields)
                .setSessionToken(token)
                .build();
        placesClient.fetchPlace(request)
                .addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        view.hideProgressBar();

                        PlaceModel destination = new PlaceModel();
                        destination.address = fetchPlaceResponse.getPlace().getAddress();
                        destination.latLng = fetchPlaceResponse.getPlace().getLatLng();
                        state.setDestination(destination);
                        providePlace(destination);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                view.hideProgressBar();
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            }
        });
    }

    public void skip() {
        state.saveHomePlace(null);
        view.finish();
    }

    public void providePlace(PlaceModel placeModel) {

        switch (state.getMode()) {
            case "home":
                state.saveHomePlace(placeModel);
                view.finish();
                break;
            case "search":
                startTrip(placeModel);
                break;
            default:
        }
    }

    private void startTrip(PlaceModel destination) {
        if (!AppUtils.isNetworkConnected(context)) {
            return;
        }

        view.showProgressBar();

        ResultHandler<ShareableTrip> resultHandler = new ResultHandler<ShareableTrip>() {
            @Override
            public void onResult(@NonNull ShareableTrip trip) {
                Log.d(TAG, "trip is created: " + trip);
                view.hideProgressBar();

                view.addShareTripFragment(trip.getTripId(), trip.getShareUrl());
            }

            @Override
            public void onError(@NonNull Exception error) {
                Log.e(TAG, "Trip start failure", error);
                view.hideProgressBar();
                Toast.makeText(context, "Trip start failure", Toast.LENGTH_SHORT).show();

            }

        };
        TripConfig tripRequest;
        if (destination == null) {
            tripRequest = new TripConfig.Builder()
                    .setDeviceId(mHyperTrackDeviceId)
                    .build();
        } else {
            tripRequest = new TripConfig.Builder()
                    .setDestinationLatitude(destination.latLng.latitude)
                    .setDestinationLongitude(destination.latLng.longitude)
                    .setDeviceId(mHyperTrackDeviceId)
                    .build();
        }
        mBackendProvider.createTrip(tripRequest, resultHandler);
        if (!AppUtils.isGpsProviderEnabled(context)) {
            actionLocationSourceSettings();
        }
    }

    private void actionLocationSourceSettings() {
        if (!InstantApps.isInstantApp(context)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void destroy() {
        googleMap.setOnCameraMoveListener(null);
        googleMap = null;
        disposables.clear();
        context.unregisterReceiver(connectivityReceiver);
    }

    public interface View {

        void updateConnectionStatus(boolean offline);

        void updateAddress(String address);

        void updateHomeAddress(PlaceModel home);

        void updateList(List<PlaceModel> list);

        void showHomeAddress();

        void hideHomeAddress();

        void showSetOnMap();

        void hideSetOnMap();

        void showProgressBar();

        void hideProgressBar();

        void addShareTripFragment(String tripId, String shareUrl);

        void finish();
    }
}
