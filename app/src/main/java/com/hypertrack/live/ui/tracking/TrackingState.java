package com.hypertrack.live.ui.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.live.R;
import com.hypertrack.sdk.views.maps.TripSubscription;
import com.hypertrack.sdk.views.maps.models.MapTrip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrackingState {

    private final SharedPreferences preferences;
    private final String hyperTrackPubKey;
    private final Map<String, TripSubscription> outerTrips = new HashMap<>();
    private String tripId;
    private LatLng destination;
    private String shareableUrl;

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

    public Map<String, TripSubscription> getOuterTrips() {
        return outerTrips;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public String getShareableUrl() {
        return shareableUrl;
    }

    public void setShareableUrl(String shareableUrl) {
        this.shareableUrl = shareableUrl;
    }

    public TrackingState(Context context, String hyperTrackPubKey) {
        this.hyperTrackPubKey = hyperTrackPubKey;
        preferences = context.getSharedPreferences(context.getString(R.string.app_name), Activity.MODE_PRIVATE);
        tripId = preferences.getString("trip_id", null);
    }
}
