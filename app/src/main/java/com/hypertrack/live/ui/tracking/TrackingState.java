package com.hypertrack.live.ui.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.live.R;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.TripSubscription;

import java.util.HashMap;
import java.util.Map;

public class TrackingState {

    private final SharedPreferences preferences;
    private final String hyperTrackPubKey;
    public final Map<String, Trip> trips = new HashMap<>();
    public final Map<String, TripSubscription> tripSubscription = new HashMap<>();
    private String tripId;
    private LatLng destination;

    public String getHyperTrackPubKey() {
        return hyperTrackPubKey;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
        preferences.edit().putString("trip_id", tripId).apply();
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public TrackingState(Context context, String hyperTrackPubKey) {
        this.hyperTrackPubKey = hyperTrackPubKey;
        preferences = context.getSharedPreferences(context.getString(R.string.app_name), Activity.MODE_PRIVATE);
        tripId = preferences.getString("trip_id", null);
    }
}
