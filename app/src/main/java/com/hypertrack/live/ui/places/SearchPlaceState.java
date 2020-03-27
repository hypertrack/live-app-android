package com.hypertrack.live.ui.places;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.BaseState;
import com.hypertrack.live.utils.SharedHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SearchPlaceState extends BaseState {
    private final String mode;
    private PlaceModel destination;
    private Gson gson = new Gson();
    boolean mapDestinationMode = false;
    private final Set<PlaceModel> recentPlaces;

    SearchPlaceState(Context context, String mode) {
        super(context);
        this.mode = mode;
        String recentJson = preferences().getString("recent", "[]");
        Type listType = new TypeToken<HashSet<PlaceModel>>() {}.getType();
        recentPlaces = gson.fromJson(recentJson, listType);
    }

    public String getMode() {
        return mode;
    }

    PlaceModel getDestination() {
        return destination;
    }

    void setDestination(PlaceModel destination) {
        this.destination = destination;
    }

    void saveHomePlace(PlaceModel home) {
        String homeJson = gson.toJson(home);
        preferences().edit()
                .putString(SharedHelper.HOME_PLACE, homeJson)
                .apply();
        hyperTrack.setDeviceMetadata(sharedHelper.getDeviceMetadata());
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
