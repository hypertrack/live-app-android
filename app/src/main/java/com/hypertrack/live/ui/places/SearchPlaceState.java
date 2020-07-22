package com.hypertrack.live.ui.places;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.backend.AbstractBackendProvider;
import com.hypertrack.backend.models.GeofenceLocation;
import com.hypertrack.backend.ResultHandler;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.BaseState;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class SearchPlaceState extends BaseState {
    private static final String TAG = "SearchPlaceState";
    private final String mode;
    private PlaceModel destination;
    private PlaceModel home;
    boolean mapDestinationMode = false;
    private final Set<PlaceModel> recentPlaces;
    @NonNull private final AbstractBackendProvider mAbstractBackendProvider;

    SearchPlaceState(Context context, String mode, @NonNull AbstractBackendProvider abstractBackendProvider) {
        super(context);
        mAbstractBackendProvider = abstractBackendProvider;
        this.mode = mode;
        home = sharedHelper.getHomePlace();
        if (home == null) {
            mAbstractBackendProvider.getHomeGeofenceLocation(new ResultHandler<GeofenceLocation>() {
                @Override public void onResult(GeofenceLocation result) {
                    PlaceModel newPlace = new PlaceModel();
                    newPlace.latLng = new LatLng(result.getLatitude(), result.getLongitude());
                    home = newPlace;
                    sharedHelper.setHomePlace(home);
                }
                @Override public void onError(@NotNull Exception error) { }
            });
        }
        recentPlaces = sharedHelper.getRecentPlaces();
    }

    String getMode() { return mode; }

    PlaceModel getDestination() { return destination; }

    PlaceModel getHome() { return home; }

    void setDestination(PlaceModel destination) { this.destination = destination; }

    void saveHomePlace(PlaceModel home) {
        sharedHelper.setHomePlace(home);
        if (home != null) createGeofenceOnPlatform(home);
    }

    private void createGeofenceOnPlatform(final PlaceModel home) {
        mAbstractBackendProvider.updateHomeGeofence(new GeofenceLocation(home.latLng.latitude, home.latLng.longitude),
                new ResultHandler<Void>() {
                    @Override
                    public void onResult(Void result) { Log.d(TAG, "Geofence was created"); }

                    @Override
                    public void onError(@NonNull Exception ignored) {
                        final Handler handler = new Handler(mContext.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                createGeofenceOnPlatform(home);
                            }
                        }, TimeUnit.SECONDS.toMillis(5));
                    }
                }
        );
    }

    List<PlaceModel> getRecentPlaces() {
        List<PlaceModel> list = new ArrayList<>(recentPlaces);
        Collections.reverse(list);
        return list;
    }

    void addPlaceToRecent(PlaceModel placeModel) {
        recentPlaces.remove(placeModel);

        placeModel.isRecent = true;
        recentPlaces.add(placeModel);
        sharedHelper.setRecentPlaces(recentPlaces);
    }
}
