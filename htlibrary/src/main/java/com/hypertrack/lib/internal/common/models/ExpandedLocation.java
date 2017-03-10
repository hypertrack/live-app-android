package com.hypertrack.lib.internal.common.models;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by piyush on 23/07/16.
 */
public class ExpandedLocation implements Serializable {

    @SerializedName("geojson")
    private GeoJSONLocation location;

    @SerializedName("accuracy")
    private Float accuracy;

    @SerializedName("speed")
    private Float speed;

    @SerializedName("bearing")
    private Float bearing;

    @SerializedName("altitude")
    private Double altitude;

    public ExpandedLocation() {
    }

    public ExpandedLocation(Location location) {
        if (location == null) {
            return;
        }

        this.location = new GeoJSONLocation(location);
        this.accuracy = location.getAccuracy();

        // Include this parameters only if Location object has them
        if (location.hasAltitude())
            this.altitude = location.getAltitude();

        if (location.hasSpeed())
            this.speed = location.getSpeed();

        if (location.hasBearing())
            this.bearing = location.getBearing();
    }

    public GeoJSONLocation getGeoJSONLocation() {
        return location;
    }

    public void setLocation(GeoJSONLocation location) {
        this.location = location;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Float getBearing() {
        return bearing;
    }

    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpandedLocation that = (ExpandedLocation) o;

        if (location != null ? !location.equals(that.location) : that.location != null)
            return false;
        if (accuracy != null ? !accuracy.equals(that.accuracy) : that.accuracy != null)
            return false;
        if (speed != null ? !speed.equals(that.speed) : that.speed != null) return false;
        if (bearing != null ? !bearing.equals(that.bearing) : that.bearing != null) return false;
        return altitude != null ? altitude.equals(that.altitude) : that.altitude == null;

    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (accuracy != null ? accuracy.hashCode() : 0);
        result = 31 * result + (speed != null ? speed.hashCode() : 0);
        result = 31 * result + (bearing != null ? bearing.hashCode() : 0);
        result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExpandedLocation{" +
                "location=" + location +
                ", accuracy=" + accuracy +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", altitude=" + altitude +
                '}';
    }
}
