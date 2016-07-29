package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import io.hypertrack.lib.common.model.HTLocation;

/**
 * Created by ulhas on 19/06/16.
 */
public class PlaceDTO {

    private int id;

    @SerializedName("google_places_id")
    private String googlePlacesID;

    private String name;

    private String address;

    private HTLocation location;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public HTLocation getLocation() {
        return location;
    }

    public void setLocation(HTLocation location) {
        this.location = location;
    }

    public PlaceDTO(MetaPlace place) {
        this.id = place.getId();
        this.address = place.getAddress();
        this.googlePlacesID = place.getGooglePlacesID();
        this.name = place.getName();
        this.location = new HTLocation(place.getLatitude(), place.getLongitude());
    }
}
