package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.models.ExpandedLocation;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;

/**
 * Created by ulhas on 12/03/16.
 */
public class HTUser {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("photo")
    private String photoURL;

    @SerializedName("vehicle_type")
    private HTUserVehicleType vehicleType;

    @SerializedName("last_known_location")
    private ExpandedLocation lastKnownLocation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public HTUserVehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(HTUserVehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public ExpandedLocation getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(ExpandedLocation lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    @Override
    public String toString() {
        return "HTUser{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", lastKnownLocation='" + lastKnownLocation + '\'' +
                '}';
    }
}
