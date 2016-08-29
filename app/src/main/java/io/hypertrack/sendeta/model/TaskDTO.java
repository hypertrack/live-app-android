package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import io.hypertrack.lib.common.model.HTDriverVehicleType;
import io.hypertrack.lib.common.model.HTLocation;

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
    private HTLocation startLocation;

    @SerializedName("vehicle_type")
    private HTDriverVehicleType vehicleType;

    @SerializedName("task_id")
    private String taskID;

    private String name;

    private String address;

    private HTLocation location;

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

    public HTLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(HTLocation startLocation) {
        this.startLocation = startLocation;
    }

    public HTDriverVehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(HTDriverVehicleType vehicleType) {
        this.vehicleType = vehicleType;
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

    public HTLocation getLocation() {
        return location;
    }

    public void setLocation(HTLocation location) {
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
        this.location = new HTLocation(place.getLatitude(), place.getLongitude());
    }

    public TaskDTO(MetaPlace place, int accountId, HTLocation startLocation, HTDriverVehicleType vehicleType) {
        this(place, accountId);
        this.startLocation = startLocation;
        this.vehicleType = vehicleType;
    }

    public TaskDTO(String taskID, int accountId, HTLocation startLocation, HTDriverVehicleType vehicleType) {
        this.taskID = taskID;
        this.accountId = accountId;
        this.startLocation = startLocation;
        this.vehicleType = vehicleType;
    }
}
