package com.hypertrack.lib.internal.common.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ulhas on 09/03/16.
 */
public enum HTUserVehicleType {
    @SerializedName("walking")
    WALK("walk"),

    @SerializedName("bicycle")
    BICYCLE("bicycle"),

    @SerializedName("motorcycle")
    MOTORCYCLE("motorcycle"),

    @SerializedName("car")
    CAR("car"),

    @SerializedName("3-wheeler")
    THREE_WHEELER("three-wheeler"),

    @SerializedName("van")
    VAN("van");

    private String vehicleType;

    HTUserVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String toString() {
        return this.vehicleType;
    }
}
