package com.hypertrack.live.ui.places;

import android.content.Context;

import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.BaseState;
import com.hypertrack.live.utils.OnDeviceGeofence;
import com.hypertrack.sdk.HyperTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class SearchPlaceState extends BaseState {
    private final String mode;
    private PlaceModel destination;
    private PlaceModel home;
    boolean mapDestinationMode = false;
    private final Set<PlaceModel> recentPlaces;
    private String mHypertrackDeviceID;

    SearchPlaceState(Context context, String mode) {
        super(context);
        String hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();
        this.mode = mode;
        home = sharedHelper.getHomePlace();
        recentPlaces = sharedHelper.getRecentPlaces();
        mHypertrackDeviceID = HyperTrack.getInstance(hyperTrackPublicKey).getDeviceID();
    }

    String getMode() { return mode; }

    PlaceModel getDestination() { return destination; }

    PlaceModel getHome() { return home; }

    void setDestination(PlaceModel destination) { this.destination = destination; }

    void saveHomePlace(PlaceModel home) {
        sharedHelper.setHomePlace(home);

        if (home != null) {
            OnDeviceGeofence.addGeofence(mContext,
                    home.latLng.latitude, home.latLng.longitude,
                    mHypertrackDeviceID
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
        sharedHelper.setRecentPlaces(recentPlaces);
    }
}
