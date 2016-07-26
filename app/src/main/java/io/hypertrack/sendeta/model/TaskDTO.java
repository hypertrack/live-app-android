package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import io.hypertrack.lib.common.model.HTLocation;

/**
 * Created by piyush on 26/07/16.
 */
public class TaskDTO{

    @SerializedName("place_id")
    private int placeId;

    @SerializedName("account_id")
    private String accountId;

    @SerializedName("google_places_id")
    private String googlePlacesID;

    private String name;

    private String address;

    private HTLocation location;

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

    public TaskDTO(int placeId, String accountId) {
        this.placeId = placeId;
        this.accountId = accountId;
    }
}
