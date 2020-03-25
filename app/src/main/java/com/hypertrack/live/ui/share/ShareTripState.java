package com.hypertrack.live.ui.share;

import android.content.Context;

import com.hypertrack.live.models.TripModel;
import com.hypertrack.live.ui.BaseState;
import com.hypertrack.sdk.views.dao.Trip;

class ShareTripState extends BaseState {
    private String tripId;
    private String shareUrl;
    private TripModel mTripModel;

    String getCurrentTripId() {
        return tripId;
    }

    void setCurrentTripId(String tripId) {
        this.tripId = tripId;
        preferences.edit().putString("created_trip_id", tripId).apply();
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
        preferences.edit().putString("created_trip_share_url", shareUrl).apply();
    }

    ShareTripState(Context context) {
        super(context);
        tripId = preferences.getString("created_trip_id", null);
        shareUrl = preferences.getString("created_trip_share_url", null);
    }

    void updateTrip(Trip trip) {
        mTripModel = TripModel.fromTrip(trip);
    }

    String getShareMessage() {
        return mTripModel == null ? shareUrl : mTripModel.getShareableMessage();
    }
}
