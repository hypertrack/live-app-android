package io.hypertrack.sendeta.model;

import android.location.Location;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;


/**
 * Created by ulhas on 19/06/16.
 */
public class PlaceDTO {

    private String id;

    @SerializedName("google_places_id")
    private String googlePlacesID;

    private String name;

    private String address;

    private HyperTrackLocation location;

    public PlaceDTO(UserPlace place) {
        this.id = place.getId();
        this.address = place.getAddress();
        this.googlePlacesID = place.getGooglePlacesID();
        this.name = place.getName();
        android.location.Location newLoc = new Location("");
        newLoc.setLatitude(place.getLocation().getLatitude());
        newLoc.setLongitude(place.getLocation().getLongitude());
        this.location = new HyperTrackLocation(newLoc);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public HyperTrackLocation getLocation() {
        return location;
    }

    public void setLocation(HyperTrackLocation location) {
        this.location = location;
    }
}
