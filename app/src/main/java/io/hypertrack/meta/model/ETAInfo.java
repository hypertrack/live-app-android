package io.hypertrack.meta.model;

/**
 * Created by suhas on 26/11/15.
 */
public class ETAInfo {

    private Integer duration;
    private Integer distance;

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "ETAInfo{" +
                "duration=" + duration +
                ", distance=" + distance +
                '}';
    }
}
