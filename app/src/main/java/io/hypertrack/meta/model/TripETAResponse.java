package io.hypertrack.meta.model;

/**
 * Created by ulhas on 19/06/16.
 */
public class TripETAResponse {
    private double duration;

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "TripETAResponse{" +
                "duration=" + duration +
                '}';
    }
}
