package io.hypertrack.meta.model;

import com.google.android.gms.location.places.Place;

/**
 * Created by piyush on 10/06/16.
 */
public class MetaPlace {

    private int id;
    private String hyperTrackDestinationID;
    private String googlePlacesID;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHyperTrackDestinationID() {
        return hyperTrackDestinationID;
    }

    public void setHyperTrackDestinationID(String hyperTrackDestinationID) {
        this.hyperTrackDestinationID = hyperTrackDestinationID;
    }

    public String getGooglePlacesID() {
        return googlePlacesID;
    }

    public void setGooglePlacesID(String googlePlacesID) {
        this.googlePlacesID = googlePlacesID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "MetaPlace{" +
                "id=" + id +
                ", hyperTrackDestinationID='" + hyperTrackDestinationID + '\'' +
                ", googlePlacesID='" + googlePlacesID + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public MetaPlace(Place place) {

    }
}
