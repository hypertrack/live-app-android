package com.hypertrack.live.ui.share;

import android.content.Context;

import androidx.annotation.Nullable;

import com.hypertrack.live.models.TripModel;
import com.hypertrack.live.ui.BaseState;
import com.hypertrack.sdk.views.dao.Trip;

class ShareTripState extends BaseState {
    @Nullable private String tripId;
    private String shareUrl;
    private TripModel mTripModel;

    String getCurrentTripId() {
        return tripId;
    }

    void setCurrentTripId(@Nullable String tripId) {
        this.tripId = tripId;
        sharedHelper.setCreatedTripId(tripId);
    }

    void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
        sharedHelper.setShareUrl(shareUrl);
    }

    ShareTripState(Context context) {
        super(context);
        tripId = sharedHelper.getCreatedTripId();
        shareUrl = sharedHelper.getShareUrl();
    }

    void updateTrip(Trip trip) {
        mTripModel = TripModel.fromTrip(trip);
    }

    String getShareMessage() {
        return mTripModel == null ? shareUrl : mTripModel.getShareableMessage();
    }
}
