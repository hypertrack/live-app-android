package com.hypertrack.live.ui.places;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.backend.BackendProvider;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.BaseState;
import com.hypertrack.live.utils.OnDeviceGeofence;
import com.hypertrack.live.utils.SharedHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SearchPlaceState extends BaseState {
    private final String mode;
    private final BackendProvider mBackendProvider;
    private PlaceModel destination;
    private PlaceModel home;
    private Gson gson = new Gson();
    boolean mapDestinationMode = false;
    private final Set<PlaceModel> recentPlaces;

    SearchPlaceState(Context context, String mode, @NonNull BackendProvider backendProvider) {
        super(context);
        this.mode = mode;
        String homeJson = preferences().getString(SharedHelper.HOME_PLACE_KEY, null);
        Type homeType = new TypeToken<PlaceModel>() {}.getType();
        home = gson.fromJson(homeJson, homeType);
        String recentJson = preferences().getString("recent", "[]");
        Type listType = new TypeToken<HashSet<PlaceModel>>() {}.getType();
        recentPlaces = gson.fromJson(recentJson, listType);
        mBackendProvider = backendProvider;
    }

    String getMode() { return mode; }

    PlaceModel getDestination() { return destination; }

    PlaceModel getHome() { return home; }

    void setDestination(PlaceModel destination) { this.destination = destination; }

    void saveHomePlace(PlaceModel home) {
        String homeJson = gson.toJson(home);
        preferences().edit()
                .putString(SharedHelper.HOME_PLACE_KEY, homeJson)
                .apply();
        hyperTrack.setDeviceMetadata(sharedHelper.getDeviceMetadata());

        if (home != null) {
            OnDeviceGeofence.addGeofence(mContext,
                    home.latLng.latitude, home.latLng.longitude,
                    hyperTrack.getDeviceID()
            );
        }
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
        String recentJson = gson.toJson(recentPlaces);
        preferences().edit().putString("recent", recentJson).apply();
    }
}
