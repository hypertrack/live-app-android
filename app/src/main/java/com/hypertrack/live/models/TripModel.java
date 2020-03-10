package com.hypertrack.live.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.trips.ShareableTrip;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

public class TripModel {
    @NonNull public final String tripId;
    @NonNull public final String shareableUrl;
    @Nullable private Integer mRemainingDuration;
    @Nullable private Trip mTrip;
    @Nullable private LocalTime tripReceived;

    public static TripModel fromShareableTrip(@NonNull ShareableTrip shareableTrip) {
        TripModel model = new TripModel(shareableTrip.getTripId(), shareableTrip.getShareUrl());
        Integer remainingDuration = shareableTrip.getRemainingDuration();
        if (null != remainingDuration) {
            model.mRemainingDuration = remainingDuration;
            model.tripReceived = LocalTime.now();
        }
        return model;

    }

    public static TripModel fromTrip(@NonNull Trip trip) {
        Trip.Views views = trip.getViews();
        if (views.getSharedUrl() == null) return null;
        TripModel model = new TripModel(trip.getTripId(), views.getSharedUrl());
        model.update(trip);
        return model;
    }

    private TripModel(@NonNull String tripId, @NonNull String shareableUrl) {
        this.tripId = tripId;
        this.shareableUrl = shareableUrl;

    }

    @NonNull
    public String getShareableMessage() {
        return new ShareableMessage(shareableUrl, mRemainingDuration, tripReceived).getShareMessage();
    }

    void update(@Nullable Trip trip) {
        if (trip == null || !tripId.equals(trip.getTripId())) return;
        mTrip = trip;
        tripReceived = LocalTime.now();
        mRemainingDuration = trip.getEstimate() == null
                ? null
                : (trip.getEstimate().getRoute() == null
                    ? null
                    : trip.getEstimate().getRoute().getRemainingDuration()
                );
    }

    static class ShareableMessage {

        private final String mShareableUrl;
        private final Integer mRemainingDuration;
        private final LocalTime mDurationAdjustmentTime;

        ShareableMessage(String shareableUrl, Integer remainingDuration, LocalTime durationAdjustmentTime) {
            mShareableUrl = shareableUrl;
            mRemainingDuration = remainingDuration;
            mDurationAdjustmentTime = durationAdjustmentTime;
        }

        @NonNull
        String getShareMessage() {
            if (mRemainingDuration == null) {
                return String.format("Track my live location here %s", mShareableUrl);
            }
            assert mDurationAdjustmentTime != null;
            LocalTime arriveTime = mDurationAdjustmentTime.plus(mRemainingDuration, ChronoUnit.SECONDS);
            if (arriveTime.isBefore(LocalTime.now())) {
                return String.format("Arriving now. Track my live location here %s", mShareableUrl);
            }

            return String.format("Will be there by %s. Track my live location here %s",
                    arriveTime.format(DateTimeFormatter.ofPattern("h:mma")), mShareableUrl
            );
        }
    }
}
