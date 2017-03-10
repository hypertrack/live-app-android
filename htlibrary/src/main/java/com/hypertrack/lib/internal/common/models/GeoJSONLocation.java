package com.hypertrack.lib.internal.common.models;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

public class GeoJSONLocation implements Serializable {

    @SerializedName("type")
    private String type;

    @SerializedName("coordinates")
    private double[] coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "GeoJSONLocation{" +
                "type='" + type + '\'' +
                ", coordinates=" + Arrays.toString(coordinates) +
                '}';
    }

    public GeoJSONLocation(double latitude, double longitude) {
        this.type = "Point";
        this.coordinates = new double[]{longitude, latitude};
    }

    public GeoJSONLocation(Location location) {
        if (location != null) {
            this.type = "Point";
            this.coordinates = new double[]{location.getLongitude(), location.getLatitude()};
        }
    }

    public double getLatitude() {
        return this.coordinates[1];
    }

    public double getLongitude() {
        return this.coordinates[0];
    }

    public String getDisplayString() {
        if (this.getCoordinates() == null)
            return null;

        double[] coordinate = this.coordinates;
        return String.format("%.1f° N, %.1f° E", coordinate[1], coordinate[0]);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + Arrays.hashCode(coordinates);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        if (this.coordinates == null) {
            return false;
        }

        GeoJSONLocation location = (GeoJSONLocation) o;

        if (location.coordinates == null) {
            return false;
        }

        return ((this.coordinates[0] == location.coordinates[0])
                && (this.coordinates[1] == this.coordinates[1]));
    }
}
