package com.hypertrack.live.ui.places;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.BaseState;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SearchPlaceState extends BaseState {
    private LatLng destination;
    private Gson gson = new Gson();
    boolean mapDestinationMode = false;
    private final Set<PlaceModel> recentPlaces;

    SearchPlaceState(Context context) {
        super(context);
        String recentJson = preferences.getString("recent", "[]");
        Type listType = new TypeToken<HashSet<PlaceModel>>() {}.getType();
        recentPlaces = gson.fromJson(recentJson, listType);
    }

    LatLng getDestination() {
        return destination;
    }

    void setDestination(LatLng destination) {
        this.destination = destination;
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
        preferences.edit().putString("recent", recentJson).apply();
    }
}
