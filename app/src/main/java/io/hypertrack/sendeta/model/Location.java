package io.hypertrack.sendeta.model;

import java.util.Arrays;

/**
 * Created by piyush on 28/07/16.
 */
public class Location {

    private String type;
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
        return "HTLocation{" +
                "type='" + type + '\'' +
                ", coordinates=" + Arrays.toString(coordinates) +
                '}';
    }

    public Location (double latitude, double longitude) {
        this.type = "Point";
        this.coordinates = new double[]{longitude, latitude};
    }

    public Location (android.location.Location location) {
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
}
