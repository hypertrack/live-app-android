package io.hypertrack.meta.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by suhas on 09/12/15.
 */
public class MetaLocation {

    private String type;
    private double[] coordinates;
    private static byte LONGITUDE_INDEX =  1;
    private static byte LATITUDE_INDEX =  0;

    @SerializedName("google_places_id")
    private String googlePlacesId;

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

    public LatLng getLatLng() {
        return new LatLng(coordinates[LONGITUDE_INDEX],coordinates[LATITUDE_INDEX]);
    }

    public String getGooglePlacesId() {
        return googlePlacesId;
    }

    public void setGooglePlacesId(String googlePlacesId) {
        this.googlePlacesId = googlePlacesId;
    }

    @Override
    public String toString() {
        return "MetaLocation{" +
                "type='" + type + '\'' +
                ", coordinates=" + Arrays.toString(coordinates) +
                ", googlePlacesId='" + googlePlacesId + '\'' +
                '}';
    }

}
