package com.hypertrack.live.ui.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.live.R;
import com.hypertrack.live.models.TripModel;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.TripSubscription;
import com.hypertrack.trips.ShareableTrip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TrackingState {

    private final SharedPreferences preferences;
    private final String hyperTrackPubKey;
    private final Map<String, Trip> trips = new HashMap<>();
    final Map<String, TripSubscription> tripSubscription = new HashMap<>();
    private String tripId;
    private LatLng destination;
    private TripModel mTripModel;

    String getHyperTrackPubKey() {
        return hyperTrackPubKey;
    }

    String getCurrentTripId() {
        return tripId;
    }

    void setCurrentTrip(ShareableTrip trip) {
        this.tripId = trip.getTripId();
        preferences.edit().putString("trip_id", tripId).apply();
        mTripModel = TripModel.fromShareableTrip(trip);
    }

    void setCurrentTrip(Trip trip) {
        this.tripId = trip.getTripId();
        preferences.edit().putString("trip_id", tripId).apply();
        mTripModel = TripModel.fromTrip(trip);
    }

    LatLng getDestination() {
        return destination;
    }

    void setDestination(LatLng destination) {
        this.destination = destination;
    }

    TrackingState(Context context, String hyperTrackPubKey) {
        this.hyperTrackPubKey = hyperTrackPubKey;
        preferences = context.getSharedPreferences(context.getString(R.string.app_name), Activity.MODE_PRIVATE);
        tripId = preferences.getString("trip_id", null);
    }

    Trip getCurrentTrip() { return trips.get(tripId); }

    void addTrip(Trip trip) {
        trips.put(trip.getTripId(), trip);
        mTripModel = TripModel.fromTrip(trip);
    }

    void delete(Trip trip) { trips.remove(trip.getTripId()); }

    List<Trip> getAllTripsStartingFromLatest() {
        ArrayList<Trip> result = new ArrayList<>(this.trips.values());
        Collections.sort(result, new Comparator<Trip>() {
            @Override
            public int compare(@NonNull Trip trip1, Trip trip2) {
                if (trip1.getStartDate() == null) return 1;
                return trip1.getStartDate().compareTo(trip2.getStartDate());
            }
        });
        return result;
    }

    String getShareMessage() {
        return mTripModel == null ? "" : mTripModel.getShareableMessage();
    }
}
