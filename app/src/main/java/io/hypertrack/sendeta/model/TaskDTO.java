package io.hypertrack.sendeta.model;

import android.location.*;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;


/**
 * Created by piyush on 26/07/16.
 */
public class TaskDTO{

    @SerializedName("place_id")
    private int placeId;

    @SerializedName("account_id")
    private int accountId;

    @SerializedName("google_places_id")
    private String googlePlacesID;

    @SerializedName("start_location")
    private HyperTrackLocation startLocation;



    @SerializedName("task_id")
    private String taskID;

    private String name;

    private String address;

    private HyperTrackLocation location;

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getGooglePlacesID() {
        return googlePlacesID;
    }

    public void setGooglePlacesID(String googlePlacesID) {
        this.googlePlacesID = googlePlacesID;
    }

    public HyperTrackLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(HyperTrackLocation startLocation) {
        this.startLocation = startLocation;
    }



    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
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

    public TaskDTO(int placeId, int accountId) {
        this.placeId = placeId;
        this.accountId = accountId;
    }

    public TaskDTO(MetaPlace place, int accountId) {
        this(place.getId(), accountId);
        this.address = place.getAddress();
        this.googlePlacesID = place.getGooglePlacesID();
        this.name = place.getName();

        android.location.Location newLoc = new android.location.Location("");
        newLoc.setLatitude(place.getLatitude());
        newLoc.setLongitude(place.getLongitude());
        this.location = new HyperTrackLocation(newLoc);
    }

    public TaskDTO(MetaPlace place, int accountId, HyperTrackLocation startLocation) {
        this(place, accountId);
        this.startLocation = startLocation;

    }


}
