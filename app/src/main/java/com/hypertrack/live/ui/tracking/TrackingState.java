package com.hypertrack.live.ui.tracking;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hypertrack.live.models.TripModel;
import com.hypertrack.live.ui.BaseState;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.sdk.views.maps.TripSubscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TrackingState extends BaseState {
    private String tripId;
    private TripModel mTripModel;
    final Map<String, Trip> trips = new HashMap<>();
    final Map<String, TripSubscription> tripSubscription = new HashMap<>();

    String getSelectedTripId() {
        return tripId;
    }

    void setSelectedTrip(Trip trip) {
        if (trip != null) {
            this.tripId = trip.getTripId();
            preferences.edit().putString("selected_trip_id", tripId).apply();
            mTripModel = TripModel.fromTrip(trip);
        } else {
            tripId = null;
            mTripModel = null;
            preferences.edit().remove("selected_trip_id").apply();
        }
    }

    TrackingState(Context context) {
        super(context);
        tripId = preferences.getString("selected_trip_id", null);
    }

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
