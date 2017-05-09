package io.hypertrack.sendeta.model;

/**
 * Created by piyush on 15/08/16.
 */
public class ETAResponse {
    private double duration;

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "ETAResponse{" +
                "duration=" + duration +
                '}';
    }
}
