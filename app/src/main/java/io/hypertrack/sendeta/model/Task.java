package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import io.hypertrack.lib.common.model.HTDriverVehicleType;

/**
 * Created by piyush on 28/07/16.
 */
public class Task {

    private String id;

    private Destination destination;

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("vehicle_type")
    private HTDriverVehicleType vehicleType;

    @SerializedName("publishable_key")
    private String publishableKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public HTDriverVehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(HTDriverVehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public void setPublishableKey(String publishableKey) {
        this.publishableKey = publishableKey;
    }

    public Task(String id, Destination destination, String driverId, String publishableKey) {
        this.id = id;
        this.destination = destination;
        this.driverId = driverId;
        this.vehicleType = HTDriverVehicleType.CAR;
        this.publishableKey = publishableKey;
    }

    public Task(String id, Destination destination, String driverId, HTDriverVehicleType vehicleType, String publishableKey) {
        this.id = id;
        this.destination = destination;
        this.driverId = driverId;
        this.vehicleType = vehicleType;
        this.publishableKey = publishableKey;
    }
}
